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

    @Test void profileHeroUsesExplicitResponsiveIdentityGridAndSafeMessageCondition() throws Exception {
        String jsp = source("src/main/webapp/WEB-INF/views/profile.jsp");
        String css = source("src/main/webapp/assets/css/dashboard-refined.css");
        assertTrue(css.contains("grid-template-columns:88px minmax(0,1fr) auto"));
        assertTrue(css.contains(".profile-hero .profile-avatar{width:88px;height:88px;aspect-ratio:1/1"));
        assertTrue(css.contains("@media(max-width:700px)"));
        assertTrue(css.contains("@media(max-width:480px)"));
        assertTrue(css.contains(".profile-message-action .btn{width:100%;min-height:44px}"));
        assertTrue(jsp.contains("<c:if test=\"${messageAllowed}\">"));
    }

    @Test void publicHomeAmbientCardUsesCssOnlyAndHonorsReducedMotion() throws Exception {
        String css = source("src/main/webapp/assets/css/public.css");
        assertTrue(css.contains("@keyframes pulse-ambient-drift"));
        assertTrue(css.contains(".pulse-today::before"));
        assertTrue(css.contains(".pulse-today::after"));
        assertTrue(css.contains("@media(prefers-reduced-motion:reduce)"));
        assertTrue(css.contains(".pulse-today::after{animation:none"));
    }

    @Test void sharedAvatarRendererAlwaysCropsInsideItsParentShape() throws Exception {
        String partial = source("src/main/webapp/WEB-INF/views/partials/avatar.jsp");
        String dashboard = source("src/main/webapp/assets/css/dashboard.css");
        assertTrue(partial.contains("class=\"avatar-safe\""));
        assertTrue(dashboard.contains(".avatar-safe img{position:absolute;inset:0;width:100%;height:100%;border-radius:inherit;object-fit:cover}"));
    }
}
