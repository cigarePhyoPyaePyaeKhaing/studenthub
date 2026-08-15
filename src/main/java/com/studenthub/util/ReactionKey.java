package com.studenthub.util;

public record ReactionKey(long postId, long authenticatedUserId, String type) {
    public static ReactionKey like(long postId, long authenticatedUserId) {
        return new ReactionKey(postId, authenticatedUserId, "LIKE");
    }
}
