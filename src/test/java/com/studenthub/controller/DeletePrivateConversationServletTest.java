package com.studenthub.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.studenthub.dao.PrivateMessageDAO;
import com.studenthub.service.PrivateConversationDeletionService;
import com.studenthub.util.CsrfToken;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

class DeletePrivateConversationServletTest {
    @Test
    void authenticatedParticipantCanHideConversationForSelf() throws Exception {
        AtomicLong conversation = new AtomicLong();
        AtomicLong user = new AtomicLong();
        PrivateMessageDAO dao = new PrivateMessageDAO() {
            @Override public HideResult hideWithDiagnostics(long conversationId, long userId) {
                conversation.set(conversationId); user.set(userId); return new HideResult(true, 1);
            }
        };
        Exchange exchange = exchange(41L, "csrf-value", "csrf-value", "77");
        new DeletePrivateConversationServlet(dao).doPost(exchange.request, exchange.response);
        assertEquals(77L, conversation.get());
        assertEquals(41L, user.get());
        assertEquals(HttpServletResponse.SC_OK, exchange.status.get());
        assertTrue(exchange.body.toString().contains("\"success\":true"));
    }

    @Test void invalidConversationIdReturnsBadRequest() throws Exception {
        Exchange exchange = exchange(41L, "csrf-value", "csrf-value", "bad");
        new DeletePrivateConversationServlet(new PrivateMessageDAO()).doPost(exchange.request, exchange.response);
        assertError(exchange, 400, "DELETE_INVALID_ID");
    }

    @Test void unauthenticatedRequestReturnsUnauthorized() throws Exception {
        Exchange exchange = exchange(null, "csrf-value", "csrf-value", "77");
        new DeletePrivateConversationServlet(new PrivateMessageDAO()).doPost(exchange.request, exchange.response);
        assertError(exchange, 401, "DELETE_UNAUTHENTICATED");
    }

    @Test void invalidCsrfReturnsForbidden() throws Exception {
        Exchange exchange = exchange(41L, "csrf-value", "wrong", "77");
        new DeletePrivateConversationServlet(new PrivateMessageDAO()).doPost(exchange.request, exchange.response);
        assertError(exchange, 403, "DELETE_CSRF_INVALID");
    }

    @Test void nonMemberReturnsForbidden() throws Exception {
        PrivateConversationDeletionService service = serviceReturning(
                new PrivateConversationDeletionService.DeleteResult("DELETE_FORBIDDEN", false, 0));
        Exchange exchange = exchange(41L, "csrf-value", "csrf-value", "77");
        new DeletePrivateConversationServlet(service).doPost(exchange.request, exchange.response);
        assertError(exchange, 403, "DELETE_FORBIDDEN");
    }

    @Test void databaseFailureReturnsSafeError() throws Exception {
        PrivateConversationDeletionService service = new PrivateConversationDeletionService(new PrivateMessageDAO()) {
            @Override public DeleteResult deleteForUser(long conversationId, long userId) throws SQLException {
                throw new SQLException("sensitive detail", "42000", 1146);
            }
        };
        Exchange exchange = exchange(41L, "csrf-value", "csrf-value", "77");
        new DeletePrivateConversationServlet(service).doPost(exchange.request, exchange.response);
        assertError(exchange, 500, "DELETE_DB_ERROR");
        assertFalse(exchange.body.toString().contains("sensitive"));
        assertFalse(exchange.body.toString().contains("42000"));
    }

    @Test void unexpectedRuntimeFailureStillReturnsStructuredJson() throws Exception {
        PrivateConversationDeletionService service = new PrivateConversationDeletionService(new PrivateMessageDAO()) {
            @Override public DeleteResult deleteForUser(long conversationId, long userId) {
                throw new IllegalStateException("sensitive runtime detail");
            }
        };
        Exchange exchange = exchange(41L, "csrf-value", "csrf-value", "77");
        new DeletePrivateConversationServlet(service).doPost(exchange.request, exchange.response);
        assertError(exchange, 500, "DELETE_SERVER_ERROR");
        assertFalse(exchange.body.toString().contains("sensitive"));
    }

    private static PrivateConversationDeletionService serviceReturning(PrivateConversationDeletionService.DeleteResult result) {
        return new PrivateConversationDeletionService(new PrivateMessageDAO()) {
            @Override public DeleteResult deleteForUser(long conversationId, long userId) { return result; }
        };
    }

    private static void assertError(Exchange exchange, int status, String code) {
        assertEquals(status, exchange.status.get());
        assertTrue(exchange.body.toString().contains("\"success\":false"));
        assertTrue(exchange.body.toString().contains(code));
    }

    private static Exchange exchange(Long userId, String sessionCsrf, String requestCsrf, String conversationId) {
        Map<String,Object> values = new HashMap<>();
        if (userId != null) values.put("userId", userId);
        values.put(CsrfToken.SESSION_KEY, sessionCsrf);
        HttpSession session = (HttpSession) Proxy.newProxyInstance(HttpSession.class.getClassLoader(),
                new Class[]{HttpSession.class}, (proxy, method, args) ->
                        "getAttribute".equals(method.getName()) ? values.get(args[0]) : null);
        HttpServletRequest request = (HttpServletRequest) Proxy.newProxyInstance(HttpServletRequest.class.getClassLoader(),
                new Class[]{HttpServletRequest.class}, (proxy, method, args) -> switch (method.getName()) {
                    case "getSession" -> session;
                    case "getParameter" -> "csrfToken".equals(args[0]) ? requestCsrf : conversationId;
                    default -> null;
                });
        AtomicInteger status = new AtomicInteger(200);
        StringWriter body = new StringWriter();
        PrintWriter writer = new PrintWriter(body);
        HttpServletResponse response = (HttpServletResponse) Proxy.newProxyInstance(HttpServletResponse.class.getClassLoader(),
                new Class[]{HttpServletResponse.class}, (proxy, method, args) -> {
                    if ("setStatus".equals(method.getName())) status.set((Integer) args[0]);
                    if ("getWriter".equals(method.getName())) return writer;
                    return null;
                });
        return new Exchange(request, response, status, body);
    }

    private record Exchange(HttpServletRequest request, HttpServletResponse response,
                            AtomicInteger status, StringWriter body) {}
}
