package com.studenthub.util;

import com.studenthub.model.Post;
import com.studenthub.model.Role;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class ReactionBehaviorTest {
    @Test void reactionIdentityUsesAuthenticatedUser() {
        assertEquals(7, ReactionKey.like(10, 7).authenticatedUserId());
    }
    @Test void duplicateReactionKeyIsPreventedByIdentity() {
        Set<ReactionKey> reactions = new HashSet<>();
        assertTrue(reactions.add(ReactionKey.like(10, 7)));
        assertFalse(reactions.add(ReactionKey.like(10, 7)));
        assertEquals(1, reactions.size());
    }
    @Test void ownReactionCanBeRemovedByToggle() {
        assertFalse(ReactionToggle.nextState(true));
    }
    @Test void absentReactionCanBeAddedByToggle() {
        assertTrue(ReactionToggle.nextState(false));
    }
    @Test void feedModelCarriesRealCountsAndCurrentUserState() {
        Post post = post(3, 5, true);
        assertEquals(3, post.getReactionCount());
        assertEquals(5, post.getCommentCount());
        assertTrue(post.isReactedByCurrentUser());
    }
    @Test void postDeletionPlanPreservesCommentAndReactionCleanup() {
        String plan = String.join(" ", PostDeletionPlan.relatedStatements());
        assertTrue(plan.contains("DELETE FROM reactions"));
        assertTrue(plan.contains("DELETE FROM comments"));
        assertTrue(plan.contains("UPDATE deadlines"));
    }
    private Post post(long reactions, long comments, boolean mine) {
        return new Post(1, 2, 3L, "Author", Role.CR, "General News", "Title", "Content",
                null, "ALL", LocalDateTime.now(), reactions, comments, mine);
    }
}
