package com.studenthub.util;

import com.studenthub.model.DiscussionScope;

public record DiscussionTarget(long authorId, DiscussionScope scope, Long universityId, Integer semester, String sectionName) {
    public static DiscussionTarget fromAuthenticatedUser(long authorId, DiscussionScope scope,
                                                         Integer semester, String sectionName) {
        return fromAuthenticatedUser(authorId, scope, null, semester, sectionName);
    }
    public static DiscussionTarget fromAuthenticatedUser(long authorId, DiscussionScope scope, Long universityId,
                                                         Integer semester, String sectionName) {
        return new DiscussionTarget(authorId, scope,
                isGlobal(scope) ? null : universityId,
                isGlobal(scope) ? null : semester,
                scope == DiscussionScope.SECTION ? sectionName : null);
    }
    private static boolean isGlobal(DiscussionScope scope) {
        return scope == DiscussionScope.ALL || scope == DiscussionScope.CR_ALL || scope == DiscussionScope.CR_ADMIN;
    }
}
