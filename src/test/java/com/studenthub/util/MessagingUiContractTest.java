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
    void discussionOwnershipLabelAndAttachmentBubbleShareServerOwnedPresentation() throws IOException {
        String jsp = source("src/main/webapp/WEB-INF/views/discussions/index.jsp");
        String refinedCss = source("src/main/webapp/assets/css/dashboard-refined.css");
        assertTrue(jsp.contains("sessionScope.userId eq chatMessage.senderId"));
        assertTrue(jsp.contains("<strong class=\"current-user-message-label\">You<c:if test=\"${sessionScope.role eq 'ADMIN'}\"> (Admin)</c:if><c:if test=\"${sessionScope.role eq 'CR'}\"> (CR)</c:if></strong>"));
        assertTrue(jsp.contains("<c:out value=\"${chatMessage.authorName}\" />"));
        assertTrue(jsp.contains("<jsp:include page=\"../partials/attachment.jsp\"/>"));
        assertTrue(refinedCss.contains("[data-theme=\"dark\"] .message-bubble.outgoing{border-color:#19d8d8"));
        assertTrue(refinedCss.contains(".message-bubble.outgoing .chat-media-card{width:100%}"));
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
        assertTrue(composer.contains("let selectedAttachment = null"));
        assertTrue(composer.contains("function render(file) {\n            releasePreview();"));
        assertFalse(composer.contains("function render(file) {\n            clearAttachment();"));
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
        assertTrue(css.contains("body>.dashboard-shell.private-chat-shell,body>.dashboard-shell.discussions-shell"));
        assertTrue(css.contains("max-width:none!important"));
        assertTrue(css.contains("grid-template-columns:240px minmax(0,1fr)!important"));
        assertTrue(css.contains("body>.private-chat-shell .private-chat-layout{width:100%;max-width:none;min-width:0;grid-template-columns:minmax(280px,320px) minmax(0,1fr)}"));
        assertFalse(css.contains("grid-template-columns:minmax(320px,370px) minmax(0,1fr)"));
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
        // Unified flex layout bounds for tablet and mobile
        assertTrue(css.contains(".dashboard-body:has(.discussions-shell),.dashboard-body:has(.private-chat-shell){display:flex!important;flex-direction:column!important;height:100vh!important;height:100dvh!important;min-height:100dvh!important;max-height:100dvh!important;padding:0!important;margin:0!important;overflow:hidden!important;box-sizing:border-box!important}"));
        assertTrue(css.contains(".dashboard-body:has(.discussions-shell) .mobile-header,.dashboard-body:has(.private-chat-shell) .mobile-header{flex:0 0 auto!important;position:relative!important;top:auto!important;width:100%!important;box-sizing:border-box!important;z-index:20!important}"));
        assertTrue(css.contains(".dashboard-body:has(.discussions-shell) .mobile-bottom-nav,.dashboard-body:has(.private-chat-shell) .mobile-bottom-nav{position:relative!important;flex:0 0 auto!important;top:auto!important;bottom:auto!important;left:auto!important;right:auto!important;z-index:20!important}"));
        assertTrue(css.contains(".discussions-shell,.dashboard-shell.discussions-shell,body>.dashboard-shell.discussions-shell{display:flex!important;flex-direction:column!important;flex:1 1 auto!important;min-height:0!important;height:auto!important;max-height:none!important;width:100%!important;max-width:none!important;margin:0!important;padding:8px 14px 4px 14px!important;overflow:hidden!important;box-sizing:border-box!important;grid-template-columns:none!important}"));
        assertTrue(css.contains(".discussions-shell,.private-chat-shell{flex:1 1 auto!important;min-height:0!important;height:auto!important;max-height:none!important;padding:4px 8px 2px 8px!important}"));
    }

    @Test
    void deleteDiagnosticsUseStructuredSafeCodes() throws IOException {
        String servlet = source("src/main/java/com/studenthub/controller/DeletePrivateConversationServlet.java");
        String service = source("src/main/java/com/studenthub/service/PrivateConversationDeletionService.java");
        String client = source("src/main/webapp/assets/js/private-chat.js");
        assertTrue(servlet.contains("DELETE_INVALID_ID"));
        assertTrue(service.contains("DELETE_FORBIDDEN"));
        assertFalse(service.contains("DELETE_NOT_FOUND"));
        assertTrue(servlet.contains("{\\\"success\\\":true"));
        assertTrue(servlet.contains("DELETE_DB_ERROR"));
        assertTrue(servlet.contains("SQLState="));
        assertFalse(servlet.contains("csrfToken="));
        assertFalse(servlet.contains("sessionId"));
        for (String code : new String[]{"DELETE_CSRF_INVALID", "DELETE_UNAUTHENTICATED",
                "DELETE_INVALID_ID", "DELETE_FORBIDDEN", "DELETE_NOT_FOUND", "DELETE_DB_ERROR", "DELETE_SERVER_ERROR"}) {
            assertTrue(client.contains(code));
        }
        assertTrue(client.contains("errorMessage.textContent = deleteConversationErrorMessage(code)"));
        assertTrue(client.contains("redirect: \"error\""));
        assertTrue(client.contains("DELETE_NETWORK_FAILED"));
        assertTrue(client.contains("DELETE_RESPONSE_INVALID"));
        assertTrue(client.contains("DELETE_HTTP_ERROR"));
        assertTrue(client.contains("DELETE_CLIENT_ERROR"));
        assertTrue(client.contains("window.fetch(deleteUrl"));
        assertTrue(client.contains("let conversationId;"));
        assertTrue(client.contains("conversationId = menu.dataset.conversationId"));
        assertTrue(client.contains("const csrfToken = menu.dataset.csrf"));
        String jsp = source("src/main/webapp/WEB-INF/views/messages/index.jsp");
        assertTrue(jsp.contains("class=\"conversation-menu\""));
        assertTrue(jsp.contains("data-delete-url=\"${pageContext.request.contextPath}/messages/delete\""));
        assertTrue(jsp.contains("data-conversation-id=\"${selectedConversation.conversationId}\""));
        assertFalse(client.contains("document.createElement(\"button\");\n    menu.className = \"conversation-menu\""));
        assertFalse(client.contains("form.querySelector('[name=\"csrfToken\"]')?.value"));
        assertTrue(client.contains("error instanceof ReferenceError ? \"DELETE_CLIENT_ERROR\" : \"DELETE_NETWORK_FAILED\""));
        assertFalse(client.contains("new URL(\"delete\", form.action)"));
        assertTrue(client.contains("contentType.toLowerCase().includes(\"application/json\")"));
        assertTrue(client.contains("const body = await response.text()"));
        assertTrue(client.contains("conversation?.remove()"));
        assertTrue(client.contains("history.replaceState"));
        assertTrue(client.contains("conversationList?.classList.remove(\"has-selection\")"));
        assertFalse(client.contains("window.location.reload"));
        assertTrue(jsp.contains("title=\"Conversation options\""));
        assertTrue(jsp.contains("&#8230;"));
    }

    @Test
    void attachmentAndCaptionShareOneMultipartSubmissionAndClearOnlyAfterSuccess() throws IOException {
        String composer = source("src/main/webapp/assets/js/message-composer.js");
        String privateChat = source("src/main/webapp/assets/js/private-chat.js");
        String discussionJsp = source("src/main/webapp/WEB-INF/views/discussions/index.jsp");
        String discussionClient = source("src/main/webapp/assets/js/discussion-chat.js");
        String privateServlet = source("src/main/java/com/studenthub/controller/SendPrivateMessageServlet.java");
        String discussionServlet = source("src/main/java/com/studenthub/controller/SendDiscussionMessageServlet.java");

        assertTrue(composer.contains("selectedAttachment = file || null"));
        assertTrue(privateChat.contains("const body = new FormData(form)"));
        assertTrue(privateChat.contains("last = Math.max(last, +data.messageId); resetComposer();"));
        assertFalse(privateChat.contains("resetComposer(); sendPending();"));
        assertTrue(discussionJsp.contains("enctype=\"multipart/form-data\""));
        assertTrue(discussionJsp.contains("name=\"attachment\""));
        assertTrue(discussionJsp.contains("name=\"message\""));
        assertTrue(privateServlet.contains("getPart(\"attachment\")"));
        assertTrue(privateServlet.contains("getParameter(\"message\")"));
        assertTrue(discussionServlet.contains("getPart(\"attachment\")"));
        assertTrue(discussionServlet.contains("getParameter(\"message\")"));
        assertTrue(discussionClient.contains("body: new FormData(form)"));
        assertTrue(discussionClient.contains("if (submitting) return"));
        assertTrue(discussionClient.contains("payload?.success !== true"));
        assertTrue(discussionClient.indexOf("payload?.success !== true")
                < discussionClient.indexOf("composer?.clearAttachment()"));
        assertTrue(discussionServlet.contains("acceptsJson(request)"));
        assertTrue(discussionServlet.contains("\"{\\\"success\\\":true"));
    }

    @Test
    void desktopAndTabletDiscussionControlsUseEqualPageScopedColumns() throws IOException {
        String css = source("src/main/webapp/assets/css/dashboard.css");
        String jsp = source("src/main/webapp/WEB-INF/views/discussions/index.jsp");
        // Desktop rules
        assertTrue(css.contains(".discussions-shell .discussion-mode-switch{grid-template-columns:repeat(2,minmax(0,1fr))}"));
        assertTrue(css.contains(".discussions-shell .room-tabs-five{grid-template-columns:repeat(5,minmax(0,1fr))}"));
        assertTrue(css.contains(".discussions-shell .room-tabs-three{grid-template-columns:repeat(3,minmax(0,1fr))}"));
        // Tablet rules
        assertTrue(css.contains(".discussions-shell .discussion-mode-switch{margin:4px 0!important;padding:3px!important;grid-template-columns:repeat(2,minmax(0,1fr))!important;display:grid!important;width:100%!important;max-width:none!important}"));
        assertTrue(css.contains(".discussions-shell .room-tabs-five{grid-template-columns:repeat(5,minmax(0,1fr))!important}"));
        assertTrue(css.contains(".discussions-shell .room-tabs-three{grid-template-columns:repeat(3,minmax(0,1fr))!important}"));
        assertTrue(jsp.contains("room-tabs-five"));
        assertTrue(jsp.contains("room-tabs-three"));
    }

    @Test
    void discussionTabsReflectServerAuthorizedAcademicScopes() throws IOException {
        String jsp = source("src/main/webapp/WEB-INF/views/discussions/index.jsp");
        assertTrue(jsp.contains("room.sectionRoomAvailable"));
        assertTrue(jsp.contains("room.semesterRoomAvailable"));
        assertTrue(jsp.contains("room.crSemesterRoomAvailable"));
        assertTrue(jsp.contains("scope=ALL\">All Students"));
    }

    @Test
    void privateChatMobileAndTabletLayoutMaintainsScrollOwnershipAndComposerReachability() throws IOException {
        String css = source("src/main/webapp/assets/css/dashboard.css");
        String privateChatJs = source("src/main/webapp/assets/js/private-chat.js");
        String messagesJsp = source("src/main/webapp/WEB-INF/views/messages/index.jsp");

        // Base/Desktop rules
        assertTrue(css.contains(".discussions-shell,.private-chat-shell{grid-template-columns:240px minmax(0,1fr);width:min(100%,1536px);max-width:1536px;height:100vh;height:100dvh;max-height:100dvh;min-height:0;padding:24px;box-sizing:border-box;overflow:hidden}"));
        assertTrue(css.contains(".private-thread{display:flex;flex-direction:column;flex:1 1 0;min-width:0;min-height:0;height:100%;overflow:hidden}"));
        assertTrue(css.contains(".private-message-list{display:flex;min-height:0;flex:1 1 auto;flex-direction:column;gap:8px;padding:18px;overflow-y:auto;overflow-x:hidden;-webkit-overflow-scrolling:touch;overscroll-behavior-y:contain;scrollbar-width:none;-ms-overflow-style:none;scroll-padding-bottom:14px}"));
        assertTrue(css.contains(".private-composer{display:block;padding:8px 12px;border-top:1px solid var(--border-glass);background:var(--surface-glass-strong);flex:0 0 auto;min-height:0;position:static;box-sizing:border-box}"));

        // Tablet rules (<=1180px)
        assertTrue(css.contains(".private-chat-shell,.dashboard-shell.private-chat-shell,body>.dashboard-shell.private-chat-shell{display:flex!important;flex-direction:column!important;flex:1 1 auto!important;min-height:0!important;height:auto!important;max-height:none!important;width:100%!important;max-width:none!important;margin:0!important;padding:8px 14px 4px 14px!important;overflow:hidden!important;box-sizing:border-box!important;grid-template-columns:none!important}"));
        assertTrue(css.contains(".private-chat-shell .private-chat-layout{display:grid!important;grid-template-columns:minmax(240px,34%) minmax(0,1fr)!important;flex:1 1 auto!important;min-height:0!important;height:100%!important;width:100%!important;max-width:none!important;overflow:hidden!important;border-radius:18px!important}"));
        assertTrue(css.contains(".private-chat-shell .conversation-list{display:flex!important;flex-direction:column!important;width:100%!important;height:100%!important;min-height:0!important;border-right:1px solid var(--border-glass)!important;overflow-y:auto!important}"));
        assertTrue(css.contains(".private-chat-shell .private-thread{display:flex!important;flex-direction:column!important;flex:1 1 auto!important;width:100%!important;height:100%!important;min-height:0!important;overflow:hidden!important}"));
        assertTrue(css.contains(".private-chat-shell .private-message-list{flex:1 1 auto!important;min-height:0!important;overflow-y:auto!important;overflow-x:hidden!important;-webkit-overflow-scrolling:touch!important;scrollbar-width:none!important;-ms-overflow-style:none!important;scroll-padding-bottom:12px!important}"));
        assertTrue(css.contains(".private-chat-shell .private-composer{position:static!important;bottom:auto!important;flex:0 0 auto!important;width:100%!important;box-sizing:border-box!important}"));

        // Mobile rules (<=600px)
        assertTrue(css.contains(".discussions-shell,.private-chat-shell{flex:1 1 auto!important;min-height:0!important;height:auto!important;max-height:none!important;padding:4px 8px 2px 8px!important}"));
        assertTrue(css.contains(".dashboard-body:has(.private-chat-shell) .mobile-bottom-nav,.dashboard-body:has(.discussions-shell) .mobile-bottom-nav{position:static!important;flex:0 0 auto!important;width:100%!important;max-width:none!important;margin:0!important;padding:3px 2px calc(3px + env(safe-area-inset-bottom))!important;border-radius:0!important;border-top:1px solid var(--border-glass)!important}"));
        assertTrue(css.contains(".private-chat-shell .private-chat-layout{display:flex!important;flex-direction:column!important;height:100%!important;min-height:0!important;width:100%!important;overflow:hidden!important;border-radius:16px!important}"));
        assertTrue(css.contains(".conversation-list.has-selection{display:none!important}"));
        assertTrue(css.contains(".conversation-list:not(.has-selection){display:flex!important;flex-direction:column!important;flex:1 1 auto!important;height:100%!important;min-height:0!important;border-right:0!important;overflow-y:auto!important}"));
        assertTrue(css.contains(".thread-back{display:flex!important;align-items:center;justify-content:center}"));

        // Body containment
        assertTrue(css.contains(".dashboard-body:has(.discussions-shell),.dashboard-body:has(.private-chat-shell){display:flex!important;flex-direction:column!important;height:100vh!important;height:100dvh!important;min-height:100dvh!important;max-height:100dvh!important;padding:0!important;margin:0!important;overflow:hidden!important;box-sizing:border-box!important}"));

        // Auto-scroll contract in JS and structure in JSP
        assertTrue(privateChatJs.contains("list.scrollTop = list.scrollHeight"));
        assertTrue(messagesJsp.contains("private-message-list"));
        assertTrue(messagesJsp.contains("private-composer"));
    }

    @Test
    void mobilePrivateChatImageAndAttachmentRenderingContracts() throws IOException {
        String css = source("src/main/webapp/assets/css/dashboard.css");
        String privateChatJs = source("src/main/webapp/assets/js/private-chat.js");
        String messagesJsp = source("src/main/webapp/WEB-INF/views/messages/index.jsp");

        // Mobile (<=600px) scoped private chat image attachment rules
        assertTrue(css.contains(".private-chat-shell .private-bubble{display:flex!important;flex-direction:column!important;width:fit-content!important;max-width:86%!important;min-width:0!important;padding:7px 11px!important;font-size:.88rem!important;box-sizing:border-box!important;overflow-wrap:anywhere!important;word-break:break-word!important}"));
        assertTrue(css.contains(".private-chat-shell .private-bubble.outgoing{align-self:flex-end!important;margin-left:auto!important;margin-right:0!important}"));
        assertTrue(css.contains(".private-chat-shell .private-bubble.incoming{align-self:flex-start!important;margin-right:auto!important;margin-left:0!important}"));
        assertTrue(css.contains(".private-chat-shell .private-file{order:1!important;display:flex!important;flex-direction:column!important;width:fit-content!important;max-width:100%!important;min-width:0!important;gap:5px!important;margin:0 0 3px 0!important;overflow:hidden!important;box-sizing:border-box!important}"));
        assertTrue(css.contains(".private-chat-shell .private-file img{display:block!important;width:auto!important;height:auto!important;max-width:100%!important;max-height:min(48vh,320px)!important;object-fit:contain!important;border-radius:12px!important;box-sizing:border-box!important}"));
        assertTrue(css.contains(".private-chat-shell .private-file video{display:block!important;width:100%!important;max-width:100%!important;max-height:min(48vh,320px)!important;border-radius:12px!important;object-fit:contain!important}"));
        assertTrue(css.contains(".private-chat-shell .private-file audio{width:100%!important;max-width:100%!important}"));
        assertTrue(css.contains(".private-chat-shell .private-bubble p{order:2!important;margin:0 0 2px 0!important;font-size:.88rem!important;line-height:1.45!important;white-space:pre-wrap!important;overflow-wrap:anywhere!important;word-break:break-word!important}"));
        assertTrue(css.contains(".private-chat-shell .private-bubble p:empty{display:none!important;margin:0!important;padding:0!important}"));
        assertTrue(css.contains(".private-chat-shell .message-meta{order:3!important;display:flex!important;align-items:center!important;justify-content:flex-end!important;gap:4px!important;margin-top:2px!important;white-space:nowrap!important}"));

        // Global responsive media containment
        assertTrue(css.contains(".private-file img,.private-file video,.content-attachment img,.content-attachment video{width:auto;max-width:100%!important;height:auto;max-height:min(56vh,420px);object-fit:contain}"));
        assertTrue(css.contains(".private-file strong,.private-file small{display:block;max-width:100%;overflow-wrap:anywhere;word-break:break-word;line-height:1.3}"));

        // JSP Server-rendered attachment structure
        assertTrue(messagesJsp.contains("<c:when test=\"${chatMessage.attachment.image}\"><img src=\"${fileUrl}\" alt=\"\"><a href=\"${fileUrl}?download=1\">Download image</a></c:when>"));
        assertTrue(messagesJsp.contains("<c:when test=\"${chatMessage.attachment.video}\"><video controls preload=\"metadata\" src=\"${fileUrl}\"></video><a href=\"${fileUrl}?download=1\">Download video</a></c:when>"));
        assertTrue(messagesJsp.contains("<c:when test=\"${chatMessage.attachment.audio}\"><strong><c:out value=\"${chatMessage.attachment.originalFilename}\"/></strong><audio controls preload=\"metadata\" src=\"${fileUrl}\"></audio><a href=\"${fileUrl}?download=1\">Download audio</a></c:when>"));

        // JS Client-rendered attachment & auto-scroll
        assertTrue(privateChatJs.contains("view = document.createElement(\"img\"); view.src = data.previewUrl; view.alt = data.originalFilename || \"Image attachment\";"));
        assertTrue(privateChatJs.contains("view.addEventListener(\"load\", () => { list.scrollTop = list.scrollHeight; });"));
        assertTrue(privateChatJs.contains("link.href = data.downloadUrl; link.textContent = \"Download image\";"));
        assertTrue(privateChatJs.contains("if (data.message) {"));
        assertTrue(privateChatJs.contains("textNode.textContent = data.message;"));
        assertTrue(privateChatJs.contains("textNode.remove();"));
    }
}
