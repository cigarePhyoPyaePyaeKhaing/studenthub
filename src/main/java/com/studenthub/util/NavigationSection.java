package com.studenthub.util;

import jakarta.servlet.http.HttpServletRequest;

public final class NavigationSection {
    private NavigationSection() {}

    public static String resolve(HttpServletRequest request) {
        String context = request.getContextPath();
        String uri = request.getRequestURI();
        String path = uri.substring(Math.min(context.length(), uri.length()));
        if (path.equals("/home")) return "HOME";
        if (path.equals("/announcements") || path.startsWith("/posts/")) return "ANNOUNCEMENTS";
        if (path.equals("/notifications") || path.startsWith("/notifications/")) return "NOTIFICATIONS";
        if (path.equals("/discussions") || path.startsWith("/discussions/") || path.equals("/messages") || path.startsWith("/messages/")) return "DISCUSSIONS";
        if (path.equals("/profile") && (request.getParameter("userId") == null
                || request.getParameter("userId").isBlank())) return "PROFILE";
        if (path.startsWith("/profile/") && !path.startsWith("/profile/photo/")) return "PROFILE";
        return "NONE";
    }
}
