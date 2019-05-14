package aion.dashboard.util;

import aion.dashboard.exception.DecodeException;
import org.aion.api.sol.ISolidityArg;
import org.aion.api.sol.impl.*;
import org.aion.api.type.ContractAbiEntry;
import org.aion.api.type.ContractAbiIOParam;
import org.aion.api.type.TxLog;
import org.aion.util.bytes.ByteUtil;
import org.aion.crypto.HashUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * This contract holds the event signatures and topics for use within the parser
 */
@SuppressWarnings("WeakerAccess")
public class ContractEvents {
    private static final Logger GENERAl = LoggerFactory.getLogger("logger_general");

    static {

        IntegerTypeMatcher = Pattern.compile("^[Ii]nt(128|120|112|104|96|88|80|72|64|56|48|40|32|24|16|8)?$");
        UnsignedIntegerTypeMatcher = Pattern.compile("^[Uu]int(128|120|112|104|96|88|80|72|64|56|48|40|32|24|16|8)?$");
        ArrayTypeMatcher = Pattern.compile("\\[[1-9]?[0-9]*]");
        DynamicBytesMatcher = Pattern.compile("^[Bb]ytes(\\[([0-9]|[1-2][0-9]|3[0-2])])?$");
        StaticBytesMatcher = Pattern.compile("^[Bb]ytes([0-9]|[1-2][0-9]|3[0-2])$");

    }
    /*
    Obtained from the ATS draft proposal accurate as of 21-Aug
     */
    public static final String CreatedEvent="Created(uint128,address)";
    public static final String SentEvent = "Sent(address,address,address,uint128,bytes,bytes)";
    public static final String BurnedEvent = "Burned(address,address,uint128,bytes)";
    public static final String MintedEvent = "Minted(address,address,uint128,bytes)";
    public static final String AuthorizedOperatorEvent ="AuthorizedOperator(address,address)";
    public static final String RevokedOperatorEvent="RevokedOperator(address,address)";


    /*
     * A topic is the hash of the event signature.
     */
    public static final byte[] CreatedTopic= HashUtil.h256(CreatedEvent.getBytes());
    public static final byte[] SentTopic = HashUtil.h256(SentEvent.getBytes());
    public static final byte[] BurnedTopic = HashUtil.h256(BurnedEvent.getBytes());
    public static final byte[] MintedTopic = HashUtil.h256(MintedEvent.getBytes());
    public static final byte[] AuthorizedOperatorTopic = HashUtil.h256(AuthorizedOperatorEvent.getBytes());
    public static final byte[] RevokedOperatorTopic = HashUtil.h256(RevokedOperatorEvent.getBytes());



    /*
     * A topic is the hash of the event signature.
     */
    public static final byte[] CreatedBloomHash= HashUtil.keccak256(CreatedEvent.getBytes());
    public static final byte[] SentBloomHash = HashUtil.keccak256(SentEvent.getBytes());
    public static final byte[] BurnedBloomHash = HashUtil.keccak256(BurnedEvent.getBytes());
    public static final byte[] MintedBloomHash = HashUtil.keccak256(MintedEvent.getBytes());
    public static final byte[] AuthorizedOperatorBloomHash = HashUtil.keccak256(AuthorizedOperatorEvent.getBytes());
    public static final byte[] RevokedOperatorBloomHash = HashUtil.keccak256(RevokedOperatorEvent.getBytes());

    static final Pattern IntegerTypeMatcher;
    static final Pattern UnsignedIntegerTypeMatcher;
    static final Pattern ArrayTypeMatcher;
    static final Pattern StaticBytesMatcher;
    static final Pattern DynamicBytesMatcher;

    private static class Tuple2<T,U>{
        final T x;
        final U y;

        Tuple2(T x, U y){
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Tuple2)) return false;
            Tuple2<?, ?> tuple2 = (Tuple2<?, ?>) o;
            return Objects.equals(x, tuple2.x) &&
                    Objects.equals(y, tuple2.y);
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }



    /**
     * extracts the values stored in the topics of the event log and converts them to the correct type
     * TODO fix bug in which the event's inputs may be returned in the incorrect order.
     *
     * @param txLog
     * @param contractAbiEntry
     * @return
     */
    public static Optional<ContractEvent> decodeEventLog(TxLog txLog, ContractAbiEntry contractAbiEntry) throws DecodeException {
        if (txLog.getTopics().get(0).replace("0x", "").equals(contractAbiEntry.getHashed().replace("0x", ""))) {
            try {
                ContractEvent.ContractEventBuilder builder = ContractEvent.builder();

                builder.setAddress(txLog.getAddress().toString());
                builder.setEventName(contractAbiEntry.name);
                builder.setSignatureHash(contractAbiEntry.getHashed());


                List<Tuple2<Integer, ContractAbiIOParam>> zippedParam = IntStream
                        .range(0, contractAbiEntry.inputs.size())
                        .mapToObj(i-> new Tuple2<>(i, contractAbiEntry.inputs.get(i)))
                        .collect(Collectors.toList());

                List<Tuple2<Integer, ContractAbiIOParam>> zippedIndexedParam = zippedParam.stream()
                        .filter(e-> e.y.isIndexed())
                        .collect(Collectors.toUnmodifiableList());

                List<Tuple2<Integer, ContractAbiIOParam>> zippedNonIndexedParam = zippedParam.stream()
                        .filter(e-> !e.y.isIndexed())
                        .collect(Collectors.toUnmodifiableList());



                List<Class> types = new ArrayList<>(Collections.nCopies(contractAbiEntry.inputs.size(), Object.class));
                List<Object> inputs = new ArrayList<>(Collections.nCopies(contractAbiEntry.inputs.size(), new Object()));
                List<String> typeNames = new ArrayList<>(Collections.nCopies(contractAbiEntry.inputs.size(), ""));
                List<String> names = new ArrayList<>(Collections.nCopies(contractAbiEntry.inputs.size(), ""));

                if (zippedIndexedParam.size() > 0) {
                    for (int i = 1; i < txLog.getTopics().size() && i-1 < zippedIndexedParam.size(); i++) {


                        var tuple2 = zippedIndexedParam.get(i-1);

                        String topic = txLog.getTopics().get(i).replace("0x", "");//ignore the first topic
                        typeNames.set(tuple2.x, tuple2.y.getType());
                        names.set(tuple2.x,tuple2.y.getName());
                        if (IntegerTypeMatcher.matcher(tuple2.y.getType()).find()) {
                            types.set(tuple2.x, BigInteger.class);
                            inputs.set(tuple2.x, new BigInteger(ByteUtil.hexStringToBytes(topic)));
                        } else if (UnsignedIntegerTypeMatcher.matcher(tuple2.y.getType()).find()) {
                            types.set(tuple2.x,BigInteger.class);
                            //
                            inputs.set(tuple2.x,ByteUtil.bytesToBigInteger(ByteUtil.hexStringToBytes(topic)));
                        } else if (tuple2.y.getType().equalsIgnoreCase("string")) {
                            types.set(tuple2.x,String.class);
                            inputs.set(tuple2.x,(topic));
                        } else if (StaticBytesMatcher.matcher(tuple2.y.getType()).find()) {
                            types.set(tuple2.x,String.class);
                            inputs.set(tuple2.x,topic);
                        }
                        else if (DynamicBytesMatcher.matcher(tuple2.y.getType()).find()){
                            types.set(tuple2.x,String.class);
                            inputs.set(tuple2.x, topic);
                        }
                        else if (tuple2.y.getType().equalsIgnoreCase("address")) {
                            types.set(tuple2.x,String.class);

                            inputs.set(tuple2.x,topic);

                        } else if (tuple2.y.getType().equalsIgnoreCase("bool")) {
                            long temp = Long.parseLong(topic, 16);
                            types.set(tuple2.x,Boolean.class);
                            inputs.set(tuple2.x, temp != 0);
                        } else if (ArrayTypeMatcher.matcher(tuple2.y.getType()).find()) {

                            throw new Exception("Found array type: "+ contractAbiEntry.name);
                        }
                    }
                }


                if (!zippedNonIndexedParam.isEmpty()) {

                    List<ISolidityArg> args = new ArrayList<>();

                    for (int i = 0; i < zippedNonIndexedParam.size(); i++) {

                        var tuple2 = zippedNonIndexedParam.get(i);
                        typeNames.set(tuple2.x, tuple2.y.getType());
                        names.set(tuple2.x,tuple2.y.getName());


                        if (IntegerTypeMatcher.matcher(tuple2.y.getType()).find()) {

                            Int intValue = Int.createForDecode();
                            intValue.setType(tuple2.y.getType());
                            intValue.setDynamicParameters(tuple2.y.getParamLengths());
                            args.add(intValue);
                            types.set(tuple2.x, BigInteger.class);

                        } else if (UnsignedIntegerTypeMatcher.matcher(tuple2.y.getType()).find()) {
                            Uint uintValue = Uint.createForDecode();
                            uintValue.setType(tuple2.y.getType());
                            uintValue.setDynamicParameters(tuple2.y.getParamLengths());
                            args.add(uintValue);
                            types.set(tuple2.x, BigInteger.class);


                        } else if (tuple2.y.getType().equalsIgnoreCase("string")) {

                            SString stringVal = SString.createForDecode();
                            stringVal.setType(tuple2.y.getType());
                            stringVal.setDynamicParameters(tuple2.y.getParamLengths());
                            args.add(stringVal);
                            types.set(tuple2.x, String.class);


                        } else if (StaticBytesMatcher.matcher(tuple2.y.getType()).find()) {

                            Bytes bytesVal = Bytes.createForDecode();
                            bytesVal.setType(tuple2.y.getType());
                            bytesVal.setDynamicParameters(tuple2.y.getParamLengths());
                            types.set(tuple2.x, String.class);
                            args.add(bytesVal);

                        }
                        else if(DynamicBytesMatcher.matcher(tuple2.y.getType()).find()){
                            DynamicBytes bytesVal = DynamicBytes.createForDecode();
                            bytesVal.setType(tuple2.y.getType());
                            bytesVal.setDynamicParameters(tuple2.y.getParamLengths());
                            types.set(tuple2.x, String.class);
                            args.add(bytesVal);
                        }
                        else if (tuple2.y.getType().equalsIgnoreCase("address")) {

                            Address addressVal = Address.createForDecode();
                            addressVal.setType(tuple2.y.getType());
                            addressVal.setDynamicParameters(tuple2.y.getParamLengths());

                            types.set(tuple2.x, String.class);
                            args.add(addressVal);

                        } else if (tuple2.y.getType().equalsIgnoreCase("bool")) {

                            Bool boolVal = Bool.createForDecode();
                            boolVal.setType(tuple2.y.getType());
                            boolVal.setDynamicParameters(tuple2.y.getParamLengths());
                            args.add(boolVal);
                            types.set(tuple2.x, Boolean.class);
                        } else if (ArrayTypeMatcher.matcher(tuple2.y.getType()).find()) {
                            throw new Exception("Found array type: "+ contractAbiEntry.name);

                        }


                    }
                    int[] offsets = getOffsets(args);
                    int count = 0;
                    for (int i = 0; i < args.size(); i++) {
                        ISolidityArg arg = args.get(i);
                        int offset = offsets[count];
                        int index = zippedNonIndexedParam.get(i).x;
                        count++;
                        if (arg.isType("address"))
                            inputs.set(index, ByteUtil.toHexString((byte[]) arg.decode(offset, txLog.getData())));
                        else if (arg.isType("uint") || arg.isType("int")) {
                            inputs.set(index, arg.decode(offset, txLog.getData()));
                        } else if (arg.decode(offset, txLog.getData()) instanceof byte[]) {
                            inputs.set(index, ByteUtil.toHexString((byte[]) arg.decode(offset, txLog.getData())));
                        } else
                            inputs.set(index, arg.decode(offset, txLog.getData()));
                    }

                }
                builder.setNames(names);
                builder.setTypeName(typeNames);
                builder.setClassType(types);
                builder.setInputs(inputs);


                return Optional.of(builder.build());
            }catch (IndexOutOfBoundsException ignored){

            } catch (Exception e) {
                GENERAl.debug("Read event threw Exception: ", e);
                throw new DecodeException();
            }

        }
        return Optional.empty();
    }//decodeEventLog

    private static int[] getOffsets(List<ISolidityArg> outputParams) {
        int[] ret = new int[outputParams.size()];

        for (int i = 1; i < outputParams.size(); i++) {
            ret[i] += ret[i - 1] + outputParams.get(i - 1).getStaticPartLength();
        }

        return ret;
    }
}
