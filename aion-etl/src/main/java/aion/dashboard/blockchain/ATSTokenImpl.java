package aion.dashboard.blockchain;

import aion.dashboard.blockchain.interfaces.ATSToken;
import aion.dashboard.blockchain.interfaces.ContractHandler;
import aion.dashboard.domainobject.Contract;
import aion.dashboard.domainobject.Token;
import aion.dashboard.domainobject.TokenHolders;
import aion.dashboard.util.Utils;
import org.aion.api.type.BlockDetails;
import org.aion.base.type.AionAddress;

import java.math.BigInteger;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import static aion.dashboard.util.Utils.granularityToTknDec;
import static aion.dashboard.util.Utils.truncate;
import static org.aion.api.ITx.NRG_LIMIT_CONTRACT_CREATE_MAX;
import static org.aion.api.ITx.NRG_PRICE_MIN;

public class ATSTokenImpl extends ATSToken {

    //create predicates to be used when identifying a token
    private static final Predicate<String> STRING_RESPONSE_EXISTS = s -> !s.isBlank() && !s.isEmpty();
    private static final Predicate<BigInteger> NUM_RESPONSE_EXISTS = e -> e.compareTo(BigInteger.ZERO) > 0;

    public ATSTokenImpl(String contract,ContractType type) {
        super(contract, type);
    }

    public ATSTokenImpl(Token token, ContractType type) {
        super(token, type);
    }

    //The functions that can be called on an AVM/FVM ats functions
    private String nameFunc() {
        return type.equals(ContractType.DEFAULT) ? "name()" : "name";
    }

    private String granularityFunc() {
        return type.equals(ContractType.DEFAULT) ? "granularity()" : "granularity";
    }

    private String symbolFunc() {
        return type.equals(ContractType.DEFAULT) ? "symbol()" : "symbol";
    }

    private String totalSupplyFunc() {
        return type.equals(ContractType.DEFAULT) ? "totalSupply()" : "totalSupply";
    }

    private String liquidSupplyFunc() {
        return type.equals(ContractType.DEFAULT) ? "liquidSupply()" : "liquidSupply";
    }

    private String balanceOfFunc() {
        return type.equals(ContractType.DEFAULT) ? "balanceOf(address)" : "balanceOf";
    }

    //The possible return types
    private String uintType() {
        return type.equals(ContractType.DEFAULT) ? "uint128" : BigInteger.class.getSimpleName();
    }

    private String uint32Type() {
        return type.equals(ContractType.DEFAULT) ? "uint128" : Integer.class.getSimpleName();
    }

    private String stringType() {
        return type.equals(ContractType.DEFAULT) ? "string" : String.class.getSimpleName();
    }

    @Override
    public BigInteger getBalance(String address) {
        return (BigInteger) executeContractCall(balanceOfFunc(), uintType(), AionAddress.wrap(address)).orElseThrow();
    }

    BigInteger getGranularity() {

        Optional<BigInteger> res = executeContractCall(granularityFunc(), uint32Type()).map(e-> new BigInteger(e.toString()));
        return res.filter(NUM_RESPONSE_EXISTS).orElseThrow();
    }

    String getName() {
        return executeContractCall(nameFunc(), stringType()).map(Object::toString).filter(STRING_RESPONSE_EXISTS).orElseThrow();
    }

    String getSymbol() {
        return executeContractCall(symbolFunc(), stringType()).map(Objects::toString).filter(STRING_RESPONSE_EXISTS).orElseThrow();
    }

    BigInteger getTotalSupply() {
        Optional<BigInteger> res = executeContractCall(totalSupplyFunc(), uintType());
        return res.filter(NUM_RESPONSE_EXISTS).orElseThrow();
    }

    BigInteger getLiquidSupply() {
        Optional<BigInteger> res = executeContractCall(liquidSupplyFunc(), uintType());
        return res.filter(b ->b.compareTo(BigInteger.ZERO)>=0).orElseThrow();
    }

    /**
     * Execute the contract call
     *
     * @param function   the function signature to be called
     * @param returnType the expected return type
     * @param params     the parameters to be passed to the funtion
     * @return an optional containing the result
     */
    private <T> Optional<T> executeContractCall(String function, String returnType, Object... params) {

        return ContractHandler.getInstance().prepareCallForType(type)
                .withSignature(function)
                .withNrgPrice(NRG_PRICE_MIN)
                .withNrigLimit(NRG_LIMIT_CONTRACT_CREATE_MAX)
                .withSender(AionAddress.ZERO_ADDRESS().toString())
                .withRecipient(contract)
                .withParams(params.length==0? null:params)
                .withReturnType(returnType)
                .executeFunction();
    }


    @Override
    public Optional<Token> getDetails(Contract contract) {
        checkState();

        try {

            if (token != null) {
                return Optional.of(token);
            } else {
                var optional = Optional.of(Token.getBuilder().contractAddress(contract.getContractAddr())
                        .creatorAddress(contract.getContractCreatorAddr())
                        .transactionHash(contract.getContractTxHash())
                        .name(truncate(getName(), Utils.NAME_MAX_LENGTH))
                        .symbol(truncate(getSymbol(), Utils.SYMBOLS_MAX_LENGTH))
                        .granularity(getGranularity())
                        .totalSupply(getTotalSupply())
                        .totalLiquidSupply(getLiquidSupply())
                        .timestamp(contract.getTimestamp())
                        .setTokenDecimal(granularityToTknDec(getGranularity()))
                        .build());

                optional.ifPresent(this::setToken);
                return optional;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }


    @Override
    public Optional<TokenHolders> getHolderDetails(String holderAddress, BlockDetails b) {
        checkState();
        try {
            return Optional.ofNullable(TokenHolders.from(holderAddress, b, token, getBalance(holderAddress)));
        }catch (Exception e){
            return Optional.empty();
        }
    }

}
