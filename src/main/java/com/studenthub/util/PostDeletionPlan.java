package com.studenthub.util;

import java.util.List;

public final class PostDeletionPlan {
    private PostDeletionPlan() {}
    public static List<String> relatedStatements() {
        return List.of("UPDATE deadlines SET post_id=NULL WHERE post_id=?",
                "DELETE FROM reactions WHERE post_id=?", "DELETE FROM comments WHERE post_id=?");
    }
}
