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
