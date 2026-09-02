package com.studenthub.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class UserIdentityUiContractTest {
    private static String source(String path) throws Exception { return Files.readString(Path.of(path)); }

    @Test void searchResultsUseOneNonShrinkingSquareAvatarAndStableCopyLayout() throws Exception {
        String js = source("src/main/webapp/assets/js/private-chat.js");
        String css = source("src/main/webapp/assets/css/dashboard-refined.css");
        assertTrue(js.contains("class=\"search-avatar\""));
        assertTrue(js.contains("class=\"search-result-copy\""));
        assertTrue(js.contains("aria-selected"));
        assertTrue(css.contains("flex:0 0 44px;width:44px;height:44px;aspect-ratio:1/1"));
        assertTrue(css.contains("object-fit:cover;object-position:center"));
        assertTrue(css.contains(".people-result{min-height:64px"));
        assertTrue(css.contains(".search-result-copy strong,.search-result-copy small"));
    }

    @Test void searchAsyncStateRejectsStaleResponsesAndSurfacesFailures() throws Exception {
        String js = source("src/main/webapp/assets/js/private-chat.js");
        assertTrue(js.contains("let searchSequence = 0"));
        assertTrue(js.contains("sequence !== searchSequence"));
        assertTrue(js.contains("showPeopleSearchError"));
        assertTrue(js.contains("aria-busy"));
        assertFalse(js.contains("catch (_ignored) {}"));
        assertTrue(js.contains("return {payload: null, code: \"DELETE_RESPONSE_INVALID\"}"));
    }

    @Test void profileHeroAlignsAvatarToTheCompleteIdentityBlockAndKeepsSafeActions() throws Exception {
        String jsp = source("src/main/webapp/WEB-INF/views/profile.jsp");
        String css = source("src/main/webapp/assets/css/dashboard-refined.css");
        assertTrue(jsp.contains("class=\"eyebrow profile-hero-eyebrow\""));
        assertTrue(jsp.contains("class=\"profile-hero-identity\""));
        assertTrue(jsp.contains("class=\"profile-identity-block\""));
        assertTrue(jsp.indexOf("profile-hero-identity") < jsp.indexOf("profile-hero-eyebrow"));
        assertTrue(jsp.contains("class=\"profile-identity-copy\""));
        assertTrue(css.contains(".profile-hero-identity{display:grid;width:100%;max-width:100%;grid-template-columns:88px minmax(0,1fr);align-items:center;gap:20px}"));
        assertTrue(css.contains(".profile-identity-block{display:flex;min-width:0;grid-column:2;flex-direction:column;justify-content:center}"));
        assertTrue(css.contains(".profile-identity-copy{display:flex;min-width:0;flex-direction:column;justify-content:center}"));
        assertTrue(css.contains("grid-column:1;align-self:center;width:88px;height:88px"));
        assertTrue(css.contains("@media(max-width:700px)"));
        assertTrue(css.contains("@media(max-width:480px)"));
        assertTrue(css.contains(".profile-identity-block .profile-message-action{width:100%!important}"));
        assertTrue(jsp.contains("<c:if test=\"${messageAllowed}\">"));
        assertTrue(jsp.indexOf("class=\"profile-identity\"") < jsp.indexOf("class=\"profile-message-action\""));
        assertTrue(jsp.contains("Academic information has not been configured yet."));
        assertTrue(jsp.contains("${publicProfile and profile.role eq 'ADMIN'}"));
        assertTrue(jsp.contains("${sessionScope.role eq 'ADMIN'}"));
        assertTrue(jsp.contains("/admin/users/view?id=${profile.userId}"));
        assertFalse(css.contains("margin-left:90px"));
        assertFalse(css.contains("margin-left:76px"));
        assertTrue(jsp.contains("class=\"profile-identity-meta\""));
        String hero = jsp.substring(jsp.indexOf("<section class=\"profile-hero\">"),
                jsp.indexOf("</section>", jsp.indexOf("<section class=\"profile-hero\">")));
        assertFalse(hero.contains("profile.studentId"));
        assertFalse(css.contains(".profile-hero .profile-avatar{align-self:start"));
    }

    @Test void publicHomeAmbientCardUsesCssOnlyAndHonorsReducedMotion() throws Exception {
        String css = source("src/main/webapp/assets/css/public.css");
        String home = source("src/main/webapp/WEB-INF/views/public/home.jsp");
        assertTrue(home.contains("class=\"pulse-brand-motion\""));
        assertTrue(home.contains("<jsp:include page=\"../partials/logo.jsp\">"));
        assertTrue(css.contains("@keyframes studenthub-logo-float"));
        assertTrue(css.contains(".pulse-today::before"));
        assertFalse(css.contains("background-size:34px 34px"));
        assertFalse(css.contains("@keyframes pulse-ambient-drift"));
        assertTrue(css.contains("@media(prefers-reduced-motion:reduce)"));
        assertTrue(css.contains(".pulse-brand-motion{animation:none"));
    }

    @Test void sharedAvatarRendererAlwaysCropsInsideItsParentShape() throws Exception {
        String partial = source("src/main/webapp/WEB-INF/views/partials/avatar.jsp");
        String dashboard = source("src/main/webapp/assets/css/dashboard.css");
        assertTrue(partial.contains("class=\"avatar-safe\""));
        assertTrue(dashboard.contains(".avatar-safe img{position:absolute;inset:0;width:100%;height:100%;border-radius:inherit;object-fit:cover}"));
    }
}
