package aion.dashboard.util;

import org.aion.util.bytes.ByteUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UtilsTest {

    @Test
    public void computeAddress(){
        String address = Utils.computeAddress(ByteUtil.hexStringToBytes("95e5ea3fd0b5231129389d26d4f54a92446ab4761e725b5b4bae91c95d0a0a261161ff366632e70a1d6d91126c7e1543a28577ef1fee69db069c75180ff68204"));
        assertTrue(address.matches("^[Aa]0[A-Fa-f0-9]*$"));
        assertEquals(64, address.length());
    }
}
