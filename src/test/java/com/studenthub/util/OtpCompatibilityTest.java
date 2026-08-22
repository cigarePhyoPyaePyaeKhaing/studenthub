package com.studenthub.util;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class OtpCompatibilityTest {
    @Test void verifyEmailKeepsBackendCodeParameterAndSixBoxes() throws Exception {
        String jsp=Files.readString(Path.of("src/main/webapp/WEB-INF/views/auth/verify-email.jsp"));
        assertTrue(jsp.contains("name=\"code\" data-otp-value"));
        assertEquals(6,jsp.split("class=\"otp-code-input\"",-1).length-1);
        assertTrue(jsp.contains("password-recovery.js"));
    }
}
