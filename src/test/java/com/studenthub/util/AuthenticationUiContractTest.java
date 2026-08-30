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
        assertTrue(login.contains("class=\"auth-page auth-page-refined auth-experience-page auth-login-page\""));
        assertTrue(register.contains("class=\"auth-page auth-page-refined auth-experience-page auth-register-page\""));
        assertTrue(login.contains("<jsp:include page=\"auth-brand-panel.jsp\">"));
        assertTrue(register.contains("<jsp:include page=\"auth-brand-panel.jsp\">"));
        assertTrue(panel.contains("<jsp:include page=\"../partials/logo.jsp\" />"));
        assertTrue(css.contains(".auth-experience{"));
        assertTrue(css.contains("@media(max-width:900px)"));
        assertTrue(css.contains("@media(max-width:600px)"));
        assertTrue(css.contains("@media(prefers-reduced-motion:reduce)"));
        assertTrue(css.contains(".auth-login-page .auth-card{order:1}"));
        assertTrue(css.contains(".auth-login-page .auth-brand-panel{order:2"));
        assertTrue(css.contains(".auth-register-page .auth-brand-panel{order:1"));
        assertTrue(css.contains("clip-path:polygon"));
        assertTrue(css.contains("min-height:calc(100svh - 74px)"));
        assertTrue(css.contains("grid-template-columns:minmax(0,46%) minmax(0,54%);gap:0"));
        assertTrue(css.contains(".auth-experience-page .auth-brand-panel{min-height:0;height:100%"));
        assertTrue(css.contains(".auth-experience-page .auth-brand-panel{min-height:190px;height:auto"));
        assertFalse(css.contains(".auth-experience-page .auth-brand-panel{min-height:620px"));
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
        assertTrue(login.contains("Access your announcements, deadlines, discussions, and messages."));
        assertTrue(register.contains("Get started with StudentHub"));
    }
}
