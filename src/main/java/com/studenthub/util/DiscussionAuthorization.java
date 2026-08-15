package com.studenthub.util;

public final class DiscussionAuthorization {
    private DiscussionAuthorization() {}

    public static boolean canDelete(Object role, long currentUserId, long senderId) {
        return role != null && ("ADMIN".equals(role.toString()) || currentUserId == senderId);
    }
}
