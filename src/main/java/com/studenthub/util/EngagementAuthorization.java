package com.studenthub.util;

public final class EngagementAuthorization {
    public enum DeleteDecision { ALLOWED, FORBIDDEN, NOT_FOUND }
    private EngagementAuthorization() {}
    public static boolean canDeleteComment(Object role, long authenticatedUserId, long authorId) {
        return role != null && ("ADMIN".equals(role.toString()) || authenticatedUserId == authorId);
    }
    public static DeleteDecision commentDeleteDecision(boolean exists, Object role,
                                                       long authenticatedUserId, long authorId) {
        if (!exists) return DeleteDecision.NOT_FOUND;
        return canDeleteComment(role, authenticatedUserId, authorId)
                ? DeleteDecision.ALLOWED : DeleteDecision.FORBIDDEN;
    }
}
