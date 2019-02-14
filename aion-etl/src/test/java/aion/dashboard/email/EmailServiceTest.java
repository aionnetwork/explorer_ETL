package aion.dashboard.email;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailServiceTest {

    @Test
    void send() throws InterruptedException {

        assertTrue(EmailService.getInstance().send("Test", "This is a test email please disregard "));

        Thread.sleep(20000);

        assertFalse(EmailService.getInstance().send("Test", "This is a test email please disregard "));

    }
}