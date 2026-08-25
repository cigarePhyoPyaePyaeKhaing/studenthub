package com.studenthub.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.studenthub.dao.PrivateMessageDAO;
import com.studenthub.util.CsrfToken;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

class DeletePrivateConversationServletTest {
    @Test
    void authenticatedParticipantCanHideConversationForSelf() throws Exception {
        AtomicLong hiddenConversation = new AtomicLong();
        AtomicLong hiddenForUser = new AtomicLong();
        PrivateMessageDAO dao = new PrivateMessageDAO() {
            @Override public HideResult hideWithDiagnostics(long conversation, long user) {
                hiddenConversation.set(conversation);
                hiddenForUser.set(user);
                return new HideResult(true, 1);
            }
        };
        Map<String,Object> sessionValues = new HashMap<>();
        sessionValues.put("userId", 41L);
        sessionValues.put(CsrfToken.SESSION_KEY, "csrf-value");
        HttpSession session = (HttpSession) Proxy.newProxyInstance(HttpSession.class.getClassLoader(), new Class[]{HttpSession.class},
                (proxy, method, args) -> "getAttribute".equals(method.getName()) ? sessionValues.get(args[0]) : null);
        HttpServletRequest request = (HttpServletRequest) Proxy.newProxyInstance(HttpServletRequest.class.getClassLoader(), new Class[]{HttpServletRequest.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getSession" -> session;
                    case "getParameter" -> "csrfToken".equals(args[0]) ? "csrf-value" : "77";
                    default -> null;
                });
        AtomicInteger status = new AtomicInteger(200);
        HttpServletResponse response = (HttpServletResponse) Proxy.newProxyInstance(HttpServletResponse.class.getClassLoader(), new Class[]{HttpServletResponse.class},
                (proxy, method, args) -> { if ("setStatus".equals(method.getName())) status.set((Integer) args[0]); return null; });

        new DeletePrivateConversationServlet(dao).doPost(request, response);

        assertEquals(77L, hiddenConversation.get());
        assertEquals(41L, hiddenForUser.get());
        assertEquals(HttpServletResponse.SC_NO_CONTENT, status.get());
    }
}
