package aion.dashboard.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VerifierServiceImplTest {

    @Test
    void verify() {
        ((VerifierServiceImpl)VerifierService.getInstance()).enabledVerifier = true;
        assertTrue(VerifierService.getInstance().verify("0000000000000000000000000000000000000000000000000000000000000200", VerifierService.Permission.INTERNAL_TRANSFER));
        assertFalse(VerifierService.getInstance().verify("0000000000000000000000000000000000000000000000000000000000000300", VerifierService.Permission.INTERNAL_TRANSFER));





        ((VerifierServiceImpl)VerifierService.getInstance()).enabledVerifier = false;
        assertTrue(VerifierService.getInstance().verify("0000000000000000000000000000000000000000000000000000000000000200", VerifierService.Permission.INTERNAL_TRANSFER));
        assertTrue(VerifierService.getInstance().verify("0000000000000000000000000000000000000000000000000000000000000300", VerifierService.Permission.INTERNAL_TRANSFER));

    }
}