package aion.dashboard.parser.events;

import aion.dashboard.exception.DecodeException;
import aion.dashboard.util.Utils;
import org.aion.api.type.TxLog;
import org.aion.avm.userlib.AionBuffer;
import org.aion.base.type.AionAddress;
import org.aion.base.util.ByteArrayWrapper;
import org.aion.util.bytes.ByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import static aion.dashboard.parser.events.ContractEvents.*;

public class AVMEventDecoder extends EventDecoder {
    static final AVMEventDecoder DECODER_INSTANCE=new AVMEventDecoder();

    private static final Pattern BIG_INTEGER_PATTERN = Pattern.compile("^[Bb]ig[Ii]nt(eger)?$");
    private static final Pattern ADDRESS_PATTERN = Pattern.compile("^[Aa]ddress$");
    private static final Pattern LONG_PATTERN = Pattern.compile("^[Ll]ong$");
    private static final Pattern INT_PATTERN = Pattern.compile("^[Ii]nt(eger)?$");
    private static final Pattern BOOL_PATTERN = Pattern.compile("^[Bb]oolean$");
    private static final Pattern STRING_PATTERN = Pattern.compile("^[Ss]tring$");
    private static final Pattern BYTE_PATTERN=Pattern.compile("^[Bb]yte$");
    private static final Pattern BYTE_ARRAY_PATTERN=Pattern.compile("^[Bb]yte\\[]$");
    private static final Pattern SHORT_PATTERN=Pattern.compile("^[Ss]hort$");
    private static final Pattern CHAR_PATTERN=Pattern.compile("^[Cc]har$");
    private static final Logger GENERAL = LoggerFactory.getLogger("logger_general");


    private final Map<String,List<AVMABIDefinitions.AVMEventSignature>> signatureMap;

    private AVMEventDecoder(){
        AVMABIDefinitions definitions =AVMABIDefinitions.getInstance();
        Map<String,List<AVMABIDefinitions.AVMEventSignature>> temp= new HashMap<>();
        List<AVMABIDefinitions.AVMEventSignature> signatures = definitions.allEvents();

        for (var signature: signatures){

            if (temp.containsKey(signature.hashed)){
                temp.get(signature.hashed).add(signature);
            }
            else {
                temp.put(signature.hashed, Collections.synchronizedList(new ArrayList<>()));
                temp.get(signature.hashed).add(signature);

            }
        }

        this.signatureMap = Collections.unmodifiableMap(temp);

    }


    @Override
    public Optional<ContractEvent> decodeEvent(TxLog txLog) {
        if(!txLog.getTopics().isEmpty()) {
            String hash = Utils.sanitizeHex(txLog.getTopics().get(0));
            try {//attempt to decode event and return as soon as the operation succeeds
                if (signatureMap.containsKey(hash)) {
                    for (var signature : signatureMap.get(hash)) {
                        var res = decodeEvent(txLog, signature);
                        if (res.isPresent()) {
                            return res;
                        }
                    }

                }
            } catch (Exception e) {
                // do nothing
            }
            return Optional.empty();
        }else {
            return Optional.empty();
        }

    }


    private static Optional<ContractEvent> decodeEvent(TxLog txLog, AVMABIDefinitions.AVMEventSignature signature) {

        if (Utils.sanitizeHex(txLog.getTopics().get(0)).equals(signature.getHashed())) {
            try {
                ContractEvent.ContractEventBuilder builder = ContractEvent
                        .builder()
                        .allocate(signature.indexedParameters.size() + signature.nonIndexedParameters.size())
                        .setEventName(signature.getName())
                        .setSignatureHash(signature.hashed)
                        .setAvm(true)
                        .setAddress(txLog.getAddress().toString());
                decodeIndexedTopics(builder, tailList(txLog.getTopics()), signature.indexedParameters);
                decodeNonIndexedData(builder, txLog.getData(), signature.nonIndexedParameters);

                return Optional.of(builder.build());
            } catch (DecodeException | RuntimeException e){
                GENERAL.trace("Failed to parse log:{}\nWith signature:{}\n",txLog, signature);
                GENERAL.trace("Caused by exception: ",e);
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }



    private static <E> List<E> tailList(List<E> list){
        if (list.size() <= 1){
            return Collections.emptyList();
        }
        else {
            return list.subList(1, list.size());
        }
    }

    private static ContractEvent.ContractEventBuilder decodeIndexedTopics(ContractEvent.ContractEventBuilder builder, List<String> topics, List<AVMABIDefinitions.Parameter> indexedParams) throws DecodeException {
       //decode the topics if there are any
        if (topics.isEmpty()){
            return builder;
        }
        else {
            for( var topicsParam: Utils.zip(topics,indexedParams)){

                String topic = topicsParam._1();
                String type = Utils.sanitizeHex(topicsParam._2().type.strip());
                AVMABIDefinitions.Parameter parameter = topicsParam._2();

                builder.setName(topicsParam._2().index, topicsParam._2().name);
                builder.setType(topicsParam._2().index, type);

                if (INT_PATTERN.matcher(type).find()){
                    int val = ByteUtil.byteArrayToInt(ByteUtil.hexStringToBytes(topic));
                    builder.setInput(parameter.index, BigInteger.valueOf(val))
                            .setClass(parameter.index, BigInteger.class);
                } else if (LONG_PATTERN.matcher(type).find()){
                    long val = ByteUtil.byteArrayToLong(ByteUtil.hexStringToBytes(topic));
                    builder.setInput(parameter.index, BigInteger.valueOf(val))
                            .setClass(parameter.index, BigInteger.class);
                }else if (BIG_INTEGER_PATTERN.matcher(type).find()){

                    builder.setInput(parameter.index, ByteUtil.bytesToBigInteger(ByteUtil.hexStringToBytes(topic)))
                            .setClass(parameter.index, BigInteger.class);
                }
                else if ( ADDRESS_PATTERN.matcher(type).find()
                        || BYTE_ARRAY_PATTERN.matcher(type).find()){
                    builder.setInput(parameter.index, topic)
                            .setClass(parameter.index, String.class);

                }
                else if (STRING_PATTERN.matcher(type).find()){
                    builder.setInput(parameter.index, new String(ByteUtil.hexStringToBytes(topic)))
                            .setClass(parameter.index, String.class);

                }
                else if (BOOL_PATTERN.matcher(type).find()){
                    byte val = ByteUtil.toByte(ByteUtil.hexStringToBytes(topic));
                    builder.setInput(parameter.index, val != 0)
                            .setClass(parameter.index, Boolean.class);

                }
                else if (BYTE_PATTERN.matcher(type).find()){

                    byte val = ByteUtil.toByte(ByteUtil.hexStringToBytes(topic));
                    builder.setInput(parameter.index, BigInteger.valueOf(val))
                            .setClass(parameter.index, BigInteger.class);
                }
                else if (CHAR_PATTERN.matcher(type).find()){
                    char val = (char) ByteUtil.bigEndianToShort(ByteUtil.hexStringToBytes(topic));
                    builder.setInput(parameter.index, Character.toString(val))
                            .setClass(parameter.index, String.class);
                }else if (SHORT_PATTERN.matcher(type).find()){
                    short val = ByteUtil.bigEndianToShort(ByteUtil.hexStringToBytes(topic));
                    builder.setInput(parameter.index, BigInteger.valueOf(val))
                            .setClass(parameter.index, BigInteger.class);
                }
                else throw new DecodeException();
            }
        }
        return builder;
    }
    private static final int ADDR_LENGTH = 32;

    private static ContractEvent.ContractEventBuilder decodeNonIndexedData(ContractEvent.ContractEventBuilder builder, ByteArrayWrapper data, List<AVMABIDefinitions.Parameter> nonIndexedParams) throws DecodeException {
        if (data.isEmpty()) {
            return builder;
        } else {
            AionBuffer buffer = AionBuffer.wrap(data.toBytes());

            for( var parameter: nonIndexedParams){

                String type = parameter.type;

                builder.setName(parameter.index, parameter.name);
                builder.setType(parameter.index, type);

                if (INT_PATTERN.matcher(type).find()){
                    int val = buffer.getInt();
                    builder.setInput(parameter.index, BigInteger.valueOf(val))
                            .setClass(parameter.index, BigInteger.class);
                } else if (LONG_PATTERN.matcher(type).find()){
                    long val = buffer.getLong();
                    builder.setInput(parameter.index, BigInteger.valueOf(val))
                            .setClass(parameter.index, BigInteger.class);
                }else if (BIG_INTEGER_PATTERN.matcher(type).find()){
                    BigInteger val = buffer.get32ByteInt();
                    builder.setInput(parameter.index, (val)).setClass(parameter.index, BigInteger.class);
                }
                else if (STRING_PATTERN.matcher(type).find()){
                    String str = new String(decodeBytes(buffer));
                    builder.setInput(parameter.index, str)
                            .setClass(parameter.index, String.class);
                }
                else if (ADDRESS_PATTERN.matcher(type).find()){
                    String addr = ByteUtil.toHexString(readBytes(buffer, ADDR_LENGTH ));
                    builder.setInput(parameter.index, addr)
                            .setClass(parameter.index, String.class);
                }
                else if (BOOL_PATTERN.matcher(type).find()){
                    Boolean bool = buffer.getBoolean();
                    builder.setInput(parameter.index, bool)
                            .setClass(parameter.index, Boolean.class);
                }
                else if (BYTE_ARRAY_PATTERN.matcher(type).find()){
                    String bytes = ByteUtil.toHexString(decodeBytes(buffer));
                    builder.setInput(parameter.index, bytes)
                            .setClass(parameter.index, String.class);

                }else if (BYTE_PATTERN.matcher(type).find()){
                    byte val = buffer.getByte();
                    builder.setInput(parameter.index, BigInteger.valueOf(val))
                            .setClass(parameter.index, BigInteger.class);
                }
                else if (CHAR_PATTERN.matcher(type).find()){
                    char val = buffer.getChar();
                    builder.setInput(parameter.index, Character.toString(val))
                            .setClass(parameter.index, String.class);
                }else if (SHORT_PATTERN.matcher(type).find()){
                    short val = buffer.getShort();
                    builder.setInput(parameter.index, BigInteger.valueOf(val))
                            .setClass(parameter.index, BigInteger.class);
                }
                else throw new DecodeException();
            }


            return builder;
        }
    }


    private static byte[] decodeBytes(AionBuffer buffer) throws DecodeException {
        try {
            int size = buffer.getInt();
            byte[] bytes = new byte[size];
            for (int i = 0; i < size; i++) {
                bytes[i] = buffer.getByte();
            }

            return bytes;
        }catch (Exception e){
            throw new DecodeException();
        }
    }

    private static byte[] readBytes(AionBuffer buffer, int size) throws DecodeException {
        try {
            byte[] bytes = new byte[size];
            for (int i = 0; i < size; i++) {
                bytes[i] = buffer.getByte();
            }

            return bytes;
        } catch (Exception e){
            throw new DecodeException();
        }
    }

}
