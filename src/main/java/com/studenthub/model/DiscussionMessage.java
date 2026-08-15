package com.studenthub.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record DiscussionMessage(long messageId, long senderId, String authorName,
                                String authorRole, String message, LocalDateTime createdAt) {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("MMM d, h:mm a");

    public long getMessageId() { return messageId; }
    public long getSenderId() { return senderId; }
    public String getAuthorName() { return authorName; }
    public String getAuthorRole() { return authorRole; }
    public String getMessage() { return message; }
    public String getCreatedLabel() { return createdAt.format(TIME_FORMAT); }
}
