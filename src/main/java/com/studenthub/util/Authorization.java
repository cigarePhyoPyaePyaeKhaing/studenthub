package com.studenthub.util;

import jakarta.servlet.http.HttpSession;
import java.util.Set;

public final class Authorization {
    private static final Set<String> POST_MANAGERS = Set.of("CR", "ADMIN");

    private Authorization() {
    }

    public static boolean isAuthenticated(HttpSession session) {
        return session != null && isAuthenticatedUserId(session.getAttribute("userId"));
    }

    public static boolean isAuthenticatedUserId(Object userId) {
        return userId instanceof Long id && id > 0;
    }

    public static boolean canManagePosts(Object role) {
        return role != null && POST_MANAGERS.contains(role.toString());
    }

    public static boolean canManagePost(Object role, long currentUserId, long authorId) {
        if (role == null) return false;
        return "ADMIN".equals(role.toString()) || ("CR".equals(role.toString()) && currentUserId == authorId);
    }

    public static boolean canManageDeadlines(Object role) { return canManagePosts(role); }

    public static boolean isAdmin(Object role) { return role != null && "ADMIN".equals(role.toString()); }

    public static boolean canManageDeadline(Object role, long currentUserId, long creatorId) {
        return canManagePost(role, currentUserId, creatorId);
    }
}
