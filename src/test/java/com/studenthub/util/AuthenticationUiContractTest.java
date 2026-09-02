package com.studenthub.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class AuthenticationUiContractTest {
    private static String source(String path) throws Exception { return Files.readString(Path.of(path)); }

    @Test void loginAndRegisterUseOneCanonicalResponsiveAuthSystem() throws Exception {
        String login = source("src/main/webapp/WEB-INF/views/auth/login.jsp");
        String register = source("src/main/webapp/WEB-INF/views/auth/register.jsp");
        String css = source("src/main/webapp/assets/css/auth-refined.css");
        assertTrue(login.contains("class=\"account-auth-page\""));
        assertTrue(register.contains("class=\"account-auth-page\""));
        assertTrue(login.contains("<main class=\"account-auth-main\">"));
        assertTrue(register.contains("<main class=\"account-auth-main\">"));
        assertTrue(login.contains("account-auth-surface account-auth-login"));
        assertTrue(register.contains("account-auth-surface account-auth-register"));
        assertFalse(login.contains("class=\"auth-card"));
        assertFalse(register.contains("class=\"auth-card"));
        assertFalse(login.contains("auth-brand-panel.jsp"));
        assertFalse(register.contains("auth-brand-panel.jsp"));
        assertTrue(css.contains(".account-auth-surface{display:grid;width:min(100%,64rem)"));
        assertTrue(css.contains(".account-auth-brand-panel"));
        assertTrue(login.contains("account-auth-brand-panel"));
        assertTrue(register.contains("account-auth-brand-panel"));
        assertTrue(css.contains("@media(max-width:600px)"));
        assertTrue(css.contains(".account-auth-page .auth-form-grid{grid-template-columns:minmax(0,1fr)"));
        assertFalse(css.contains("clip-path"));
        assertFalse(css.contains(".auth-experience"));
        assertFalse(css.contains("grid-template-columns:minmax(0,46%)"));
        assertTrue(css.contains("@media(max-width:600px){.account-auth-brand-panel{display:grid"));
        assertTrue(css.contains("@keyframes studenthub-auth-float"));
        assertTrue(css.contains("@media(prefers-reduced-motion:reduce)"));
        assertTrue(login.contains("<h2>Welcome Back</h2>"));
        assertTrue(register.contains("<h2>Join StudentHub</h2>"));
        assertTrue(css.contains(".account-auth-login{width:min(100%,64rem)"));
        assertTrue(css.contains(".account-auth-login .account-auth-content{padding:clamp(30px,4vw,46px)"));
        assertTrue(css.contains(".account-auth-login .account-auth-content>*{max-width:28rem}"));
        assertTrue(css.contains("@media(max-width:600px){.account-auth-login .account-auth-content>*,.account-auth-register .account-auth-content>*{max-width:none}"));
        assertTrue(css.contains(".account-auth-surface{display:grid;width:min(100%,64rem);grid-template-columns:minmax(0,1fr) minmax(19rem,.72fr);isolation:isolate;overflow:hidden}"));
        assertTrue(css.contains(".account-auth-register .account-auth-brand-panel::after{position:absolute;top:0;right:-38px;bottom:0"));
        assertTrue(css.contains(".account-auth-login .account-auth-brand-panel::after{display:none}"));
        assertTrue(css.contains(".account-auth-register .account-auth-content>*{max-width:32rem}"));
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
        assertTrue(login.contains("Sign in to continue."));
        assertTrue(register.contains(">Join StudentHub</h1>"));
        assertTrue(register.contains("Create your account to stay connected with announcements, deadlines, discussions, and messages."));
        assertTrue(register.contains("Already have an account?"));
    }
}
