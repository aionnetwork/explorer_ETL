package aion.dashboard.blockchain;

import aion.dashboard.blockchain.coder.SolidityCoder;
import aion.dashboard.blockchain.interfaces.APIService;
import aion.dashboard.blockchain.interfaces.ContractHandler;
import aion.dashboard.blockchain.interfaces.Web3Service;
import aion.dashboard.blockchain.type.CallObject;
import aion.dashboard.exception.FailedContractCallException;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIStreamingEncoder;
import org.aion.base.type.AionAddress;
import org.aion.crypto.HashUtil;
import org.aion.util.bytes.ByteUtil;
import org.aion.vm.api.interfaces.Address;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.aion.api.ITx.*;

/**
 * This implementation provides a naive implementation of AVM and FVM function calls.
 */
public class ContractHandlerImpl extends ContractHandler {
    private ContractType type;
    private String signature;
    private List<Object> params;
    private String sender;
    private String recipient;
    private Long nrgPrice;
    private Long nrgLimit;
    private APIService service;
    private String returnType;
    private ContractHandlerImpl(APIService service) {
        this.service = service;
    }

    private static ContractHandler initialize(){
        return new ContractHandlerImpl(Web3Service.getInstance());
    }


    public static final ThreadLocal<ContractHandler> INSTANCE = ThreadLocal.withInitial(ContractHandlerImpl::initialize);


    /**
     * First the contract type needs to be
     * @param type
     * @return
     */
    @Override
    public ContractHandler prepareCallForType(ContractType type) {
        this.type=type;
        return this;
    }

    @Override
    public ContractHandler withSignature(String signature) {
        this.signature =signature;
        return this;
    }


    @Override
    public ContractHandler withParams(Object... params) {
        if (params == null || params.length == 0){
            this.params = Collections.emptyList();
        }else {
            this.params = Arrays.asList(params);
        }
        return this;
    }

    @Override
    public ContractHandler withSender(String sender) {
        this.sender = sender;
        return this;
    }

    @Override
    public ContractHandler withRecipient(String recipient) {
        this.recipient = recipient;
        return this;
    }

    @Override
    public ContractHandler withNrgPrice(long nrgPrice) {
        this.nrgPrice = nrgPrice;
        return this;
    }

    @Override
    public ContractHandler withNrigLimit(long nrgLimit) {
        this.nrgLimit = nrgLimit;
        return this;
    }

    @Override
    public ContractHandler withReturnType(String returnType) {
        this.returnType = returnType;
        return this;
    }

    @Override
    public <T> Optional<T> executeFunction() {
        try {
            switch (type) {
                case AVM:
                    return Optional.ofNullable(executeForAVM());
                case DEFAULT:
                    return Optional.ofNullable(executeForFVM());
                default:
                    throw new FailedContractCallException();
            }
        }catch (RuntimeException e){
            throw new FailedContractCallException("Caught a runtime exception when executing call", e);
        }finally {
            reset();
        }
    }

    @Override
    protected <T> T executeForAVM() {

        ABIStreamingEncoder encoder = new ABIStreamingEncoder();

        encodeDataForAVM(encoder);
        try {
            var response = service.call(new CallObject(encoder.toBytes(), sender, recipient, nrgPrice, nrgLimit));
            if (response == null) return null;
            return decodeResultForAVM( response);

        }catch (Exception e){
            throw new FailedContractCallException("Caught a runtime exception while executing the contract call.",e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T decodeResultForAVM(byte[] response) {
        ABIDecoder decoder = new ABIDecoder(response);

        if (returnType.equalsIgnoreCase(BigInteger.class.getSimpleName())){
            return response.length == 0 ? (T) BigInteger.ZERO : (T) ByteUtil.bytesToBigInteger(decoder.decodeOneByteArray());
        }
        else if (returnType.equalsIgnoreCase(Address.class.getSimpleName())){
            return response.length==0 ? (T) AionAddress.EMPTY_ADDRESS() :(T) AionAddress.wrap(decoder.decodeOneAddress().toByteArray());
        }
        else if (returnType.equalsIgnoreCase(String.class.getSimpleName())){
            return response.length ==0 ? (T)"" :(T) decoder.decodeOneString();
        }
        else if (returnType.equalsIgnoreCase(Integer.class.getSimpleName())){
            return response.length==0 ? (T) Integer.valueOf(0):(T) Integer.valueOf(decoder.decodeOneInteger());
        }
        else if (returnType.equalsIgnoreCase(Long.class.getSimpleName())){
            return response.length==0 ? (T) Long.valueOf(0):(T) Long.valueOf(decoder.decodeOneLong());
        }
        else {
            throw new FailedContractCallException("Requested a decode for an unsupported type.\nType: "+returnType);
        }
    }

    private void encodeDataForAVM(ABIStreamingEncoder encoder) {
        encoder.encodeOneString(signature);//encode the function name

        for (var obj: params){//encode the obj based on the type
            if (obj instanceof Address){
                encoder.encodeOneAddress(new avm.Address(((Address) obj).toBytes()));
            }
            else if(obj instanceof Long){
                encoder.encodeOneLong((Long) obj);
            }
            else if (obj instanceof Integer){
                encoder.encodeOneInteger((Integer) obj);
            }
            else if (obj instanceof String){
                encoder.encodeOneString((String) obj);
            }
            else {
                throw new FailedContractCallException("Found an unsupported type. Param:"+obj.getClass().getName());
            }
        }
    }



    private void validateTypesLength(List<String> types){
        if (types.size() != params.size())
            throw new FailedContractCallException("Incorrect number of parameters supplied");

    }

    @Override
    protected <T> T executeForFVM() {
        SolidityCoder coder = new SolidityCoder();
        encodeDataForFVM(coder);
        try {
            var response = service.call(new CallObject(coder.toBytes(), sender, recipient, nrgPrice, nrgLimit));
            if (response == null || response.length==0) return null;
            else return decodeForFVM(response);
        } catch (Exception e) {
            throw new FailedContractCallException("Caught a runtime exception while executing the contract call.",e);
        }
    }

    private void encodeDataForFVM(SolidityCoder coder){
        coder.encodeSignature(HashUtil.keccak256(signature.getBytes()));

        List<String> types = Arrays.stream(
                signature.replaceAll("\\)$","")
                        .replaceAll("^[a-zA-Z]*\\(", "")
                        .split(","))//get all the types from the signature
                .filter(s-> !s.isEmpty() && !s.isBlank()).collect(Collectors.toList());
        validateTypesLength(types);

        for (int i=0; i< types.size(); i++){
            String paramType = types.get(i);
            Object param = params.get(i);
            switch (paramType) {
                case "int128":
                    coder.encodeInt((BigInteger) param, false);
                    break;
                case "uint128":
                    coder.encodeInt((BigInteger) param, true);
                    break;
                case "bool":
                    coder.encodeBool((Boolean) param);
                    break;
                case "address":
                    coder.encodeAddress((AionAddress) param);
                    break;
                case "string":
                    coder.encodeString((String) param);
                    break;
                case "bytes32":
                    coder.encode32Bytes((byte[]) param);
                    break;
                default:
                    throw new FailedContractCallException("Could not encode parameter with type: " + paramType);
            }
        }

    }

    @SuppressWarnings("unchecked")
    private <T> T decodeForFVM(byte[] response){
        SolidityCoder coder = SolidityCoder.from(response);

        switch (returnType) {
            case "int128":
                return (T) coder.decodeInt( false);
            case "uint128":
                return (T) coder.decodeInt( true);
            case "bool":
                return (T) ((Boolean) coder.decodeBool());
            case "address":
                return (T) coder.decodeAddress();
            case "string":
                coder.decodeInt(true);
                return (T) coder.decodeString();
            case "bytes32":
                return (T) coder.decode32Bytes();
            default:
                throw new FailedContractCallException("Could not decode response with type: " + returnType);
        }
    }
    @Override
    protected void reset() {
        this.type=null;
        this.nrgLimit=NRG_LIMIT_TX_MAX;
        this.nrgPrice=NRG_PRICE_MIN;
        this.recipient=null;
        this.sender=null;
        this.signature =null;
        this.returnType = null;
    }
}
