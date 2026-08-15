package com.studenthub.util;

import com.studenthub.model.DiscussionMessage;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DiscussionOrderingTest {
    @Test void messagesDisplayChronologically() {
        LocalDateTime now = LocalDateTime.now();
        List<DiscussionMessage> messages = new ArrayList<>(List.of(
                new DiscussionMessage(2, 1, "B", "STUDENT", "later", now.plusMinutes(1)),
                new DiscussionMessage(1, 1, "A", "STUDENT", "first", now)));
        messages.sort(Comparator.comparing(DiscussionMessage::createdAt)
                .thenComparingLong(DiscussionMessage::messageId));
        assertEquals(List.of(1L, 2L), messages.stream().map(DiscussionMessage::messageId).toList());
    }
}
