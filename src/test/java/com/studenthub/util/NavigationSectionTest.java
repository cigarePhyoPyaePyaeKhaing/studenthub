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
    }

    private String resolve(String uri, String userId) {
        HttpServletRequest request = (HttpServletRequest) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class[]{HttpServletRequest.class}, (proxy, method, args) -> switch (method.getName()) {
                    case "getContextPath" -> "/studenthub";
                    case "getRequestURI" -> uri;
                    case "getParameter" -> "userId".equals(args[0]) ? userId : null;
                    default -> null;
                });
        return NavigationSection.resolve(request);
    }
}
