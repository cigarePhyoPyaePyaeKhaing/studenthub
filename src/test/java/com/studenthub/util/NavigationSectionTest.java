package com.studenthub.util;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Proxy;
import static org.junit.jupiter.api.Assertions.assertEquals;

class NavigationSectionTest {
    @Test void mapsMainAndChildRoutesWithoutActivatingOtherUserProfile() {
        assertEquals("HOME", resolve("/studenthub/home", null));
        assertEquals("ANNOUNCEMENTS", resolve("/studenthub/announcements", null));
        assertEquals("ANNOUNCEMENTS", resolve("/studenthub/posts/comments", null));
        assertEquals("NOTIFICATIONS", resolve("/studenthub/notifications", null));
        assertEquals("DISCUSSIONS", resolve("/studenthub/discussions/messages", null));
        assertEquals("PROFILE", resolve("/studenthub/profile", null));
        assertEquals("NONE", resolve("/studenthub/profile", "42"));
        assertEquals("ADMIN_DASHBOARD", resolve("/studenthub/admin", null));
        assertEquals("ADMIN_DASHBOARD", resolve("/studenthub/admin/dashboard", null));
        assertEquals("ADMIN_USERS", resolve("/studenthub/admin/users", null));
        assertEquals("ADMIN_USERS", resolve("/studenthub/admin/users/view", null));
        assertEquals("ADMIN_ACADEMIC_REQUESTS", resolve("/studenthub/admin/academic-changes", null));
    }

    @Test void adminRouteSelectionIsIndependentOfQueryParameters() {
        assertEquals("ADMIN_USERS", resolve("/studenthub/admin/users", null, "search=test"));
        assertEquals("ADMIN_USERS", resolve("/studenthub/admin/users", null, "role=STUDENT"));
        assertEquals("ADMIN_ACADEMIC_REQUESTS",
                resolve("/studenthub/admin/academic-changes", null, "status=approved"));
    }

    private String resolve(String uri, String userId) {
        return resolve(uri, userId, null);
    }

    private String resolve(String uri, String userId, String queryString) {
        HttpServletRequest request = (HttpServletRequest) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class[]{HttpServletRequest.class}, (proxy, method, args) -> switch (method.getName()) {
                    case "getContextPath" -> "/studenthub";
                    case "getRequestURI" -> uri;
                    case "getQueryString" -> queryString;
                    case "getParameter" -> "userId".equals(args[0]) ? userId : null;
                    default -> null;
                });
        return NavigationSection.resolve(request);
    }
}
