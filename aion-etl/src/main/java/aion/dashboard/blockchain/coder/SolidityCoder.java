package aion.dashboard.blockchain.coder;

import org.aion.base.type.AionAddress;
import org.aion.solidity.SolidityType;
import org.aion.util.bytes.ByteUtil;
import org.aion.vm.api.interfaces.Address;
import org.spongycastle.util.Arrays;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class SolidityCoder {

    private static SolidityType.AddressType addressType;
    private static SolidityType.IntType uintType;
    private static SolidityType.StringType stringType;
    private static SolidityType.IntType intType;
    private static SolidityType.BoolType boolType;
    private static SolidityType.Bytes32Type bytes32Type;
    private static SolidityType.Bytes32Type bytes4Type;
    private byte[] buffer;
    private int decoderPosition;
    private StringBuilder builder;
    private static final SolidityType.BytesType dynamicBytes;

    static {
        addressType = new SolidityType.AddressType();
        uintType = new SolidityType.IntType("uint");
        intType = new SolidityType.IntType("int");
        stringType = new SolidityType.StringType();
        boolType = new SolidityType.BoolType();
        bytes32Type = new SolidityType.Bytes32Type("bytes32");
        bytes4Type = new SolidityType.Bytes32Type("bytes4");
        dynamicBytes =new SolidityType.BytesType();

    }


    public SolidityCoder() {
        this(allocate(0));
    }

    private SolidityCoder(byte[] buffer) {
        decoderPosition = 0;
        this.buffer = buffer;
    }

    private static byte[] allocate(int size) {
        return new byte[size];
    }

    public static SolidityCoder from(byte[] bytes) {
        return new SolidityCoder(bytes);
    }

    public SolidityCoder encodeAddress(Address address) {
        Objects.requireNonNull(address);
        buffer = Arrays.concatenate(buffer, addressType.encode(address.toBytes()));
        return this;
    }

    public SolidityCoder encodeString(String string) {
        Objects.requireNonNull(string);
        buffer = Arrays.concatenate(buffer, stringType.encode(string));
        return this;
    }

    public SolidityCoder encodeInt(BigInteger bigInteger, boolean unsigned) {
        Objects.requireNonNull(bigInteger);
        if (unsigned) {
            buffer = Arrays.concatenate(buffer, uintType.encode(bigInteger));
        } else {
            buffer = Arrays.concatenate(buffer, intType.encode(bigInteger));

        }
        return this;
    }

    public SolidityCoder encodeBool(boolean bool){
        buffer = Arrays.concatenate(buffer, boolType.encode(bool));
        return this;
    }

    public SolidityCoder encode32Bytes(byte[] bytes){

        buffer = Arrays.concatenate(buffer, bytes32Type.encode(bytes));
        return this;
    }
    public SolidityCoder encodeSignature(byte[] bytes){
        var encodedBytes = ByteUtil.hexStringToBytes(ByteUtil.toHexString(bytes32Type.encode(bytes)).substring(0,8));
        buffer = Arrays.concatenate(buffer,encodedBytes );
        return this;
    }
    public byte[] toBytes(){
        return Arrays.copyOf(buffer, buffer.length);
    }

    public Address decodeAddress() {

        byte[] res = (byte[]) addressType.decode(buffer, decoderPosition);

        decoderPosition += res.length;

        return AionAddress.wrap(res);
    }


    public String decodeString() {
        String string = (String) stringType.decode(buffer, decoderPosition);
        System.out.println(ByteUtil.toHexString(string.getBytes()));
        decoderPosition += string.getBytes(StandardCharsets.UTF_8).length;
        return string;
    }


    public BigInteger decodeInt(boolean unsigned) {
        final BigInteger bigInteger;
        if (unsigned) {
            bigInteger = (BigInteger) uintType.decode(buffer, decoderPosition);
        } else {
            bigInteger = (BigInteger) intType.decode(buffer, decoderPosition);

        }
        decoderPosition += 16;

        return bigInteger;
    }
    public void trim(){
        var bufferHex = ByteUtil.toHexString(buffer);

        buffer = ByteUtil.hexStringToBytes( bufferHex.replaceFirst("^0*",""));
        System.out.println(buffer.length);
    }
    public boolean decodeBool(){
        boolean bool = (boolean)boolType.decode(buffer, decoderPosition);
        decoderPosition += 16;
        return bool;
    }


    public byte[] decode32Bytes(){
        byte[] bytes = (byte[]) bytes32Type.decode(buffer, decoderPosition);
        decoderPosition +=32;
        return bytes;
    }


}
