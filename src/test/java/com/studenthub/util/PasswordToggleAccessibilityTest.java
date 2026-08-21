package com.studenthub.util;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class PasswordToggleAccessibilityTest {

    @Test
    void testMainJsContainsAccessiblePasswordToggleWithSvgEye() throws IOException {
        Path mainJs = Paths.get("src/main/webapp/assets/js/main.js");
        assertTrue(Files.exists(mainJs));
        String jsContent = Files.readString(mainJs);

        // Check for SVG icon definition and event binding
        assertTrue(jsContent.contains("eyeOpenSvg") || jsContent.contains("<svg"));
        assertTrue(jsContent.contains("data-password-toggle"));
        assertTrue(jsContent.contains("aria-label"));
        assertTrue(jsContent.contains("Hide password") || jsContent.contains("Show password"));
    }

    @Test
    void testRegisterJspUsesPasswordToggleButtons() throws IOException {
        Path registerJsp = Paths.get("src/main/webapp/WEB-INF/views/auth/register.jsp");
        assertTrue(Files.exists(registerJsp));
        String content = Files.readString(registerJsp);

        assertTrue(content.contains("class=\"password-toggle\""));
        assertTrue(content.contains("data-password-toggle=\"password\""));
        assertTrue(content.contains("data-password-toggle=\"confirmPassword\""));
    }

    @Test
    void testLoginJspUsesPasswordToggleButton() throws IOException {
        Path loginJsp = Paths.get("src/main/webapp/WEB-INF/views/auth/login.jsp");
        assertTrue(Files.exists(loginJsp));
        String content = Files.readString(loginJsp);

        assertTrue(content.contains("class=\"password-toggle\""));
        assertTrue(content.contains("data-password-toggle=\"password\""));
    }

    @Test
    void testResetPasswordJspUsesPasswordToggleButtons() throws IOException {
        Path resetJsp = Paths.get("src/main/webapp/WEB-INF/views/auth/reset-password.jsp");
        assertTrue(Files.exists(resetJsp));
        String content = Files.readString(resetJsp);

        assertTrue(content.contains("class=\"password-toggle\""));
        assertTrue(content.contains("data-password-toggle=\"password\""));
        assertTrue(content.contains("data-password-toggle=\"confirmPassword\""));
    }
}
