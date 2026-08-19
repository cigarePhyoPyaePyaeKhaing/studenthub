package com.studenthub.filter;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

class RoleAuthorizationFilterTest {
    @Test void exactAdminRootRequiresAdminClassification() {
        assertTrue(RoleAuthorizationFilter.isAdminPath("/studenthub", "/studenthub/admin"));
    }

    @Test void nestedAdminRoutesRequireAdminClassification() {
        assertTrue(RoleAuthorizationFilter.isAdminPath("/studenthub", "/studenthub/admin/users"));
    }

    @Test void similarPrefixIsNotAnAdminRoute() {
        assertFalse(RoleAuthorizationFilter.isAdminPath("/studenthub", "/studenthub/administrator"));
    }

    @Test void unreadCountIsSkippedForMutationsAndNonSidebarForms() {
        assertFalse(AuthenticationFilter.needsUnreadCount("POST", "/studenthub", "/studenthub/discussions/messages"));
        assertFalse(AuthenticationFilter.needsUnreadCount("GET", "/studenthub", "/studenthub/posts/create"));
    }

    @Test void unreadCountIsLoadedOnceForSidebarPages() {
        assertTrue(AuthenticationFilter.needsUnreadCount("GET", "/studenthub", "/studenthub/home"));
        assertTrue(AuthenticationFilter.needsUnreadCount("GET", "/studenthub", "/studenthub/admin/users"));
    }

    @Test void allPasswordRecoveryRoutesAreExplicitlyPublic() {
        assertTrue(AuthenticationFilter.isPublicPath("/forgot-password"));
        assertTrue(AuthenticationFilter.isPublicPath("/verify-reset-code"));
        assertTrue(AuthenticationFilter.isPublicPath("/reset-password"));
    }

    @Test void sensitiveApplicationRoutesAreNotPublic() {
        for (String path : new String[]{"/admin/users", "/profile", "/users", "/academic-requests", "/discussions/messages"}) {
            assertFalse(AuthenticationFilter.isPublicPath(path));
            assertTrue(AuthenticationFilter.isProtectedPath(path));
        }
    }

    @Test void loggedOutForgotPasswordRequestContinuesWithout403() throws Exception {
        AtomicInteger chained = new AtomicInteger();
        AtomicInteger status = new AtomicInteger(200);
        HttpServletRequest request = request("/forgot-password");
        HttpServletResponse response = (HttpServletResponse) Proxy.newProxyInstance(
                HttpServletResponse.class.getClassLoader(), new Class[]{HttpServletResponse.class},
                (proxy, method, args) -> { if (method.getName().equals("sendError")) status.set((Integer) args[0]); return primitive(method.getReturnType()); });
        FilterChain chain = (input, output) -> chained.incrementAndGet();

        new AuthenticationFilter().doFilter(request, response, chain);

        assertEquals(1, chained.get());
        assertEquals(200, status.get());
    }

    @Test void loggedOutAdminRequestRemainsProtected() throws Exception {
        AtomicInteger chained = new AtomicInteger();
        AtomicReference<String> redirect = new AtomicReference<>();
        HttpServletResponse response = (HttpServletResponse) Proxy.newProxyInstance(
                HttpServletResponse.class.getClassLoader(), new Class[]{HttpServletResponse.class},
                (proxy, method, args) -> { if (method.getName().equals("sendRedirect")) redirect.set((String) args[0]); return primitive(method.getReturnType()); });

        new AuthenticationFilter().doFilter(request("/admin/users"), response,
                (input, output) -> chained.incrementAndGet());

        assertEquals(0, chained.get());
        assertEquals("/login", redirect.get());
    }

    private static HttpServletRequest request(String uri) {
        return (HttpServletRequest) Proxy.newProxyInstance(HttpServletRequest.class.getClassLoader(),
                new Class[]{HttpServletRequest.class}, (proxy, method, args) -> switch (method.getName()) {
                    case "getContextPath" -> "";
                    case "getRequestURI" -> uri;
                    case "getMethod" -> "GET";
                    case "getSession" -> null;
                    default -> primitive(method.getReturnType());
                });
    }

    private static Object primitive(Class<?> type) {
        if (type == boolean.class) return false;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        return null;
    }
}
