package com.studenthub.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public record DiscussionMessage(
        long messageId,
        long senderId,
        String authorName,
        String authorRole,
        Integer authorSemester,
        String authorSection,
        String message,
        LocalDateTime createdAt,
        List<Attachment> attachments) {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("MMM d, h:mm a");

    public DiscussionMessage(long messageId, long senderId, String authorName,
                             String authorRole, Integer authorSemester, String authorSection,
                             String message, LocalDateTime createdAt) {
        this(messageId, senderId, authorName, authorRole, authorSemester, authorSection, message, createdAt, List.of());
    }

    public DiscussionMessage(long messageId, long senderId, String authorName, String authorRole,
                             String message, LocalDateTime createdAt) {
        this(messageId, senderId, authorName, authorRole, null, null, message, createdAt, List.of());
    }

    public long getMessageId() { return messageId; }
    public long getSenderId() { return senderId; }
    public String getAuthorName() { return authorName; }
    public String getAuthorRole() { return authorRole; }
    public Integer getAuthorSemester() { return authorSemester; }
    public String getAuthorSection() { return authorSection; }
    public String getMessage() { return message; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getCreatedLabel() { return createdAt == null ? "" : createdAt.format(TIME_FORMAT); }
    public List<Attachment> getAttachments() { return attachments == null ? List.of() : attachments; }
}
