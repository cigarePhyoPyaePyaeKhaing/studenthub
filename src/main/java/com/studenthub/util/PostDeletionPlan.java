package com.studenthub.util;

import java.util.List;

public final class PostDeletionPlan {
    private PostDeletionPlan() {}

    public static List<String> relatedStatements() {
        return List.of(
                "DELETE FROM attachments WHERE entity_type='POST' AND entity_id=?",
                "UPDATE deadlines SET post_id=NULL WHERE post_id=?",
                "DELETE FROM reactions WHERE post_id=?",
                "DELETE FROM comments WHERE post_id=?");
    }
}
