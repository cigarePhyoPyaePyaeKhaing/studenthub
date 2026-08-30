package com.studenthub.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class AuthenticationUiContractTest {
    private static String source(String path) throws Exception { return Files.readString(Path.of(path)); }

    @Test void loginAndRegisterShareTheBrandedResponsiveAuthSystem() throws Exception {
        String login = source("src/main/webapp/WEB-INF/views/auth/login.jsp");
        String register = source("src/main/webapp/WEB-INF/views/auth/register.jsp");
        String panel = source("src/main/webapp/WEB-INF/views/auth/auth-brand-panel.jsp");
        String css = source("src/main/webapp/assets/css/auth-refined.css");
        assertTrue(login.contains("class=\"auth-main auth-experience\""));
        assertTrue(register.contains("class=\"auth-main auth-experience\""));
        assertTrue(login.contains("<jsp:include page=\"auth-brand-panel.jsp\"/>"));
        assertTrue(register.contains("<jsp:include page=\"auth-brand-panel.jsp\"/>"));
        assertTrue(panel.contains("<jsp:include page=\"../partials/logo.jsp\" />"));
        assertTrue(css.contains(".auth-experience{"));
        assertTrue(css.contains("@media(max-width:900px)"));
        assertTrue(css.contains("@media(max-width:600px)"));
        assertTrue(css.contains("@media(prefers-reduced-motion:reduce)"));
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
        assertTrue(register.contains("<legend>Account</legend>"));
        assertTrue(register.contains("<legend>Security</legend>"));
        assertFalse(register.contains("name=\"semester\""));
        assertFalse(register.contains("name=\"section"));
    }
}
