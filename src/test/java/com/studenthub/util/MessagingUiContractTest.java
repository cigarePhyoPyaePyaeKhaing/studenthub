package com.studenthub.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class MessagingUiContractTest {
    private static String source(String relativePath) throws IOException {
        return Files.readString(Path.of(relativePath));
    }

    @Test
    void chatAttachmentInputsHaveOneIdempotentPreviewOwner() throws IOException {
        String main = source("src/main/webapp/assets/js/main.js");
        String composer = source("src/main/webapp/assets/js/message-composer.js");
        String privateChat = source("src/main/webapp/assets/js/private-chat.js");
        String discussion = source("src/main/webapp/assets/js/discussion-chat.js");
        assertFalse(main.contains("input[type=\"file\"][name=\"attachment\"]"));
        assertFalse(main.contains("flex:1 0 100%"));
        assertTrue(composer.contains("const initialized = new WeakSet()"));
        assertTrue(composer.contains("input.addEventListener(\"change\""));
        assertFalse(privateChat.contains("fileInput.addEventListener(\"change\""));
        assertFalse(discussion.contains("input.addEventListener(\"change\""));
    }

    @Test
    void bothComposerStructuresPutPreviewBeforeControls() throws IOException {
        for (String path : new String[]{
                "src/main/webapp/WEB-INF/views/messages/index.jsp",
                "src/main/webapp/WEB-INF/views/discussions/index.jsp"}) {
            String jsp = source(path);
            assertTrue(jsp.indexOf("attachment-preview-area") < jsp.indexOf("composer-controls-row"));
            assertTrue(jsp.contains("class=\"composer-input\""));
            assertTrue(jsp.contains("assets/js/message-composer.js"));
        }
    }

    @Test
    void chatWidthAndScrollOwnershipArePageScoped() throws IOException {
        String css = source("src/main/webapp/assets/css/dashboard.css");
        assertTrue(css.contains(".dashboard-shell.private-chat-shell,.dashboard-shell.discussions-shell"));
        assertTrue(css.contains("max-width:none!important"));
        assertTrue(css.contains(".composer-input{display:block;min-width:0;flex:1 1 auto}"));
        assertTrue(css.contains(".message-composer textarea::-webkit-scrollbar"));
    }

    @Test
    void academicDiscussionsLayoutMaintainsScrollOwnershipAndComposerReachability() throws IOException {
        String css = source("src/main/webapp/assets/css/dashboard.css");
        String discussionJs = source("src/main/webapp/assets/js/discussion-chat.js");
        String discussionsJsp = source("src/main/webapp/WEB-INF/views/discussions/index.jsp");

        // Chat panel must not force height:100% which pushes composer out of bounds
        assertTrue(css.contains(".chat-panel{display:flex;flex-direction:column;flex:1 1 0;min-height:0;height:auto"));
        // Message list is the single vertical scroll owner
        assertTrue(css.contains(".message-list{flex:1 1 auto;min-height:0;overflow-y:auto;overflow-x:hidden;-webkit-overflow-scrolling:touch"));
        // Room tabs horizontal scrolling without wrapping or clipping
        assertTrue(css.contains(".room-tabs{display:flex;gap:6px;padding:6px 10px;border-inline:1px solid var(--border-glass);background:var(--surface-glass-strong);overflow-x:auto;overflow-y:hidden;overscroll-behavior-inline:contain;scrollbar-width:none;-ms-overflow-style:none;-webkit-overflow-scrolling:touch;touch-action:pan-x;flex:0 0 auto;white-space:nowrap}"));
        assertTrue(css.contains(".room-tabs a{flex:0 0 auto!important;min-width:max-content!important"));
        // Active scope tab auto-scrolled smoothly into view
        assertTrue(discussionJs.contains("activeRoomTab.scrollIntoView({ behavior: \"auto\", block: \"nearest\", inline: \"center\" })"));
        assertTrue(discussionsJsp.contains("activeTab.scrollIntoView({ behavior: 'auto', block: 'nearest', inline: 'center' })"));
        // Telegram bubble incoming and outgoing corner radius
        assertTrue(css.contains(".message-bubble{display:flex;flex-direction:column;width:fit-content;max-width:min(80%,36rem);min-width:100px;padding:8px 12px;border-radius:5px 18px 18px 18px"));
        assertTrue(css.contains(".message-bubble.outgoing{border-radius:18px 5px 18px 18px"));
        // Date separator pill
        assertTrue(css.contains(".chat-date-separator"));
        // Responsive 100dvh height bounds for tablet and mobile
        assertTrue(css.contains("height:calc(100dvh - 68px - 84px - env(safe-area-inset-bottom))!important"));
        assertTrue(css.contains("height:calc(100dvh - 68px - 68px - env(safe-area-inset-bottom))!important"));
        // Safe gap above bottom navigation via padding containment
        assertTrue(css.contains("padding:4px 8px 0 8px!important"));
        // Body padding containment for discussions and private chat shells
        assertTrue(css.contains(".dashboard-body:has(.discussions-shell),.dashboard-body:has(.private-chat-shell){padding-bottom:0!important;overflow:hidden;height:100dvh}"));
    }

    @Test
    void deleteDiagnosticsUseStructuredSafeCodes() throws IOException {
        String servlet = source("src/main/java/com/studenthub/controller/DeletePrivateConversationServlet.java");
        String service = source("src/main/java/com/studenthub/service/PrivateConversationDeletionService.java");
        assertTrue(servlet.contains("DELETE_INVALID_ID"));
        assertTrue(service.contains("DELETE_FORBIDDEN"));
        assertTrue(service.contains("DELETE_NOT_FOUND"));
        assertTrue(servlet.contains("DELETE_DB_ERROR"));
        assertTrue(servlet.contains("SQLState="));
        assertFalse(servlet.contains("csrfToken="));
        assertFalse(servlet.contains("sessionId"));
    }
}
