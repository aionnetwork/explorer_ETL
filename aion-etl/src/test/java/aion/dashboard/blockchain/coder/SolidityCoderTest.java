package aion.dashboard.blockchain.coder;

import org.aion.base.type.AionAddress;
import org.aion.util.bytes.ByteUtil;
import org.aion.vm.api.interfaces.Address;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class SolidityCoderTest {


    @Test
    void addressTest() {
        SolidityCoder coder = new SolidityCoder();

        Address address = assertDoesNotThrow(()-> coder.encodeAddress(AionAddress.ZERO_ADDRESS()).decodeAddress());
        assertEquals(AionAddress.ZERO_ADDRESS(), address);
    }

    @Test
    void stringTest() {
        SolidityCoder coder = new SolidityCoder();
        String string = assertDoesNotThrow(()-> coder.encodeString("Unity is the future").decodeString());

        assertEquals("Unity is the future", string);
    }

    @Test
    void stringTest2(){
        SolidityCoder coder = SolidityCoder.from(ByteUtil.hexStringToBytes("0x0000000000000000000000000000001000000000000000000000000000000004504c4159000000000000000000000000"));
        System.out.println(coder.decodeString());
    }
    @Test
    void intTest() {
        SolidityCoder coder = new SolidityCoder();
        BigInteger uint = assertDoesNotThrow(() -> coder.encodeInt(BigInteger.ONE, true).decodeInt(true));
        BigInteger posInt = assertDoesNotThrow(() -> coder.encodeInt(BigInteger.ONE, false).decodeInt(false));
        BigInteger negInt = assertDoesNotThrow(() -> coder.encodeInt(BigInteger.ONE.negate(), false).decodeInt(false));

        assertEquals(BigInteger.ONE, uint);
        assertEquals(BigInteger.ONE, posInt);
        assertEquals(BigInteger.ONE.negate(), negInt);


    }

    @Test
    void bool() {

        SolidityCoder coder = new SolidityCoder();
        boolean trueVal = assertDoesNotThrow(()->coder.encodeBool(true).decodeBool());
        boolean falseVal = assertDoesNotThrow(()->coder.encodeBool(false).decodeBool());


        assertTrue(trueVal);
        assertFalse(falseVal);
    }

    @Test
    void encodeMultiple(){
        SolidityCoder coder = new SolidityCoder();
        byte[] bytes = new byte[32];
        Arrays.fill(bytes, (byte)0);
        coder.encodeBool(true)
                .encodeAddress(AionAddress.ZERO_ADDRESS())
                .encode32Bytes(bytes)
                .encodeInt(BigInteger.ONE, true)
                .encodeInt(BigInteger.ONE, false)
                .encodeString("Unity is the future");
        System.out.println(ByteUtil.toHexString(coder.toBytes()));
        assertTrue(coder.decodeBool());
        assertEquals(AionAddress.ZERO_ADDRESS(), coder.decodeAddress());
        assertArrayEquals(bytes, coder.decode32Bytes());
        assertEquals(BigInteger.ONE, coder.decodeInt(true));
        assertEquals(BigInteger.ONE, coder.decodeInt(false));
        assertEquals("Unity is the future", coder.decodeString());

    }

    @Test
    void encode32Bytes() {

        SolidityCoder coder = new SolidityCoder();
        byte[] bytes = new byte[32];
        Arrays.fill(bytes, (byte)0);
        byte[] decodedBytes = assertDoesNotThrow(()->coder.encode32Bytes(bytes).decode32Bytes());
        assertArrayEquals(bytes, decodedBytes);
    }
}