package com.studenthub.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class AuthenticationUiContractTest {
    private static String source(String path) throws Exception { return Files.readString(Path.of(path)); }

    @Test void loginAndRegisterUseTheStableCenteredResponsiveAuthSystem() throws Exception {
        String login = source("src/main/webapp/WEB-INF/views/auth/login.jsp");
        String register = source("src/main/webapp/WEB-INF/views/auth/register.jsp");
        String css = source("src/main/webapp/assets/css/auth-refined.css");
        assertTrue(login.contains("class=\"auth-page auth-page-refined\""));
        assertTrue(register.contains("class=\"auth-page auth-page-refined\""));
        assertTrue(login.contains("<main class=\"auth-main\">"));
        assertTrue(register.contains("<main class=\"auth-main\">"));
        assertTrue(login.contains("<div class=\"auth-card-brand\">"));
        assertTrue(register.contains("<div class=\"auth-card-brand\">"));
        assertFalse(login.contains("auth-brand-panel.jsp"));
        assertFalse(register.contains("auth-brand-panel.jsp"));
        assertTrue(css.contains(".auth-page-refined .auth-card{width:min(100%,34rem)"));
        assertTrue(css.contains(".auth-page-refined .auth-card-wide{width:min(100%,45rem)}"));
        assertTrue(css.contains("@media(max-width:600px)"));
        assertTrue(css.contains(".auth-form-grid{grid-template-columns:minmax(0,1fr)}"));
        assertFalse(css.contains("clip-path"));
        assertFalse(css.contains(".auth-experience"));
        assertFalse(css.contains("grid-template-columns:minmax(0,46%)"));
    }

    @Test void authFormsPreserveBackendNamesSecurityAndAccessibleErrors() throws Exception {
        String login = source("src/main/webapp/WEB-INF/views/auth/login.jsp");
        String register = source("src/main/webapp/WEB-INF/views/auth/register.jsp");
        assertTrue(login.contains("name=\"login\""));
        assertTrue(login.contains("name=\"password\""));
        assertTrue(login.contains("name=\"csrfToken\""));
        assertTrue(login.contains("/forgot-password"));
        assertTrue(login.contains("role=\"alert\" aria-live=\"polite\""));
        assertTrue(register.contains("name=\"studentId\""));
        assertTrue(register.contains("name=\"fullName\""));
        assertTrue(register.contains("name=\"email\""));
        assertTrue(register.contains("name=\"confirmPassword\""));
        assertTrue(register.contains("name=\"csrfToken\""));
        assertFalse(register.contains("name=\"semester\""));
        assertFalse(register.contains("name=\"section"));
        assertTrue(login.contains("Sign in to continue to your StudentHub account."));
        assertTrue(register.contains("<h1 class=\"h2\">Register</h1>"));
    }
}
