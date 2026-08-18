package com.studenthub.util;

import com.studenthub.model.DiscussionScope;

public record DiscussionTarget(long authorId, DiscussionScope scope, Integer semester, String sectionName) {
    public static DiscussionTarget fromAuthenticatedUser(long authorId, DiscussionScope scope,
                                                         Integer semester, String sectionName) {
        return new DiscussionTarget(authorId, scope,
                scope == DiscussionScope.ALL || scope == DiscussionScope.CR_ALL ? null : semester,
                scope == DiscussionScope.SECTION ? sectionName : null);
    }
}
