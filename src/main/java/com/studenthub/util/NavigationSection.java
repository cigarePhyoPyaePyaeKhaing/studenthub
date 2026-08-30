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
        if (path.equals("/admin") || path.equals("/admin/dashboard")) return "ADMIN_DASHBOARD";
        if (path.equals("/admin/users") || path.startsWith("/admin/users/")) return "ADMIN_USERS";
        if (path.equals("/admin/academic-changes") || path.startsWith("/admin/academic-changes/")) {
            return "ADMIN_ACADEMIC_REQUESTS";
        }
        return "NONE";
    }
}
