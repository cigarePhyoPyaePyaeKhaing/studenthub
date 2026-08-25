package com.studenthub.util;

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
    void searchAndModeNavigationRenderBeforeConversationState() throws IOException {
        String jsp = source("src/main/webapp/WEB-INF/views/messages/index.jsp");
        assertTrue(jsp.indexOf("class=\"people-search\"") < jsp.indexOf("<c:choose>"));
        assertTrue(jsp.contains("Private Messages"));
        assertTrue(jsp.contains("Academic Discussions"));
        assertTrue(jsp.contains("data-search-url"));
        assertTrue(jsp.contains("data-start-url"));
    }

    @Test
    void searchInitializesBeforeChatRequiresAnActiveConversation() throws IOException {
        String javascript = source("src/main/webapp/assets/js/private-chat.js");
        assertTrue(javascript.indexOf("initializePeopleSearch(panel)")
                < javascript.indexOf("if (!form || !list) return"));
        assertTrue(javascript.contains("setTimeout(async () =>"));
        assertTrue(javascript.contains("query = input.value.trim()"));
    }

    @Test
    void composerAndDeleteControlsExposeRequiredStates() throws IOException {
        String javascript = source("src/main/webapp/assets/js/private-chat.js");
        String css = source("src/main/webapp/assets/css/dashboard.css");
        assertTrue(javascript.contains("form.requestSubmit()"));
        assertTrue(javascript.contains("renderPreview(null)"));
        assertTrue(javascript.contains("Deleting..."));
        assertTrue(css.contains(".private-composer textarea::-webkit-scrollbar"));
        assertTrue(css.contains(".delete-conversation-dialog button.danger:disabled"));
        assertTrue(css.contains(".discussions-shell{width:100%;max-width:none}"));
        assertTrue(css.contains(".discussion-mode-switch{margin:14px 0}"));
        assertTrue(css.contains("-ms-overflow-style:none"));
        assertTrue(source("src/main/webapp/assets/js/discussion-chat.js").contains("textarea.scrollHeight"));
    }
}
