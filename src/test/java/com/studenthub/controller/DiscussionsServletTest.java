package com.studenthub.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Proxy;
import org.junit.jupiter.api.Test;

class DiscussionsServletTest {
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
}
