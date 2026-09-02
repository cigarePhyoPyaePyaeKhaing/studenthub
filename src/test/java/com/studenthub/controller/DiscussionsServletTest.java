package com.studenthub.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Proxy;
import org.junit.jupiter.api.Test;

class DiscussionsServletTest {
    @Test void adminSectionParametersBecomeCanonicalScopeOnlyWhenComplete() {
        assertEquals("section:4:B", DiscussionsServlet.canonicalModerationScope(requestWith(
                "moderationMode", "section", "sectionSemester", "4", "sectionName", " b ")));
        assertEquals("semester:3", DiscussionsServlet.canonicalModerationScope(requestWith(
                "moderationScope", " semester:3 ")));
        assertThrows(IllegalArgumentException.class, () -> DiscussionsServlet.canonicalModerationScope(
                requestWith("moderationMode", "section", "sectionSemester", "4")));
        assertThrows(IllegalArgumentException.class, () -> DiscussionsServlet.canonicalModerationScope(
                requestWith("moderationMode", "section", "sectionSemester", "not-a-number", "sectionName", "B")));
    }
    @Test void missingSessionRedirectsToLoginInsteadOfThrowing() throws Exception {
        String[] redirect = new String[1];
        HttpServletRequest request = (HttpServletRequest) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{HttpServletRequest.class}, (proxy, method, args) -> switch (method.getName()) {
                    case "getSession" -> null;
                    case "getContextPath" -> "/studenthub";
                    default -> primitiveDefault(method.getReturnType());
                });
        HttpServletResponse response = (HttpServletResponse) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{HttpServletResponse.class}, (proxy, method, args) -> {
                    if ("sendRedirect".equals(method.getName())) redirect[0] = String.valueOf(args[0]);
                    return primitiveDefault(method.getReturnType());
                });

        new DiscussionsServlet().doGet(request, response);

        assertEquals("/studenthub/login", redirect[0]);
        assertNull(request.getSession(false));
    }

    private static Object primitiveDefault(Class<?> type) {
        if (!type.isPrimitive()) return null;
        if (type == boolean.class) return false;
        if (type == char.class) return '\0';
        return 0;
    }

    private static HttpServletRequest requestWith(String... pairs) {
        java.util.Map<String, String> parameters = new java.util.HashMap<>();
        for (int index = 0; index < pairs.length; index += 2) parameters.put(pairs[index], pairs[index + 1]);
        return (HttpServletRequest) Proxy.newProxyInstance(DiscussionsServletTest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class}, (proxy, method, args) ->
                        "getParameter".equals(method.getName()) ? parameters.get(String.valueOf(args[0]))
                                : primitiveDefault(method.getReturnType()));
    }
}
