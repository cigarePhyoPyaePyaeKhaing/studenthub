package com.studenthub.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record Post(long postId, long authorId, Long categoryId, String authorName, Role authorRole, String categoryName,
                   String title, String content, String imageUrl, String visibility,
                   LocalDateTime createdAt, long reactionCount, long commentCount,
                   boolean reactedByCurrentUser, LocalDateTime deadlineDate) {
    private static final DateTimeFormatter DISPLAY_TIME = DateTimeFormatter.ofPattern("MMM d, yyyy · h:mm a");
    private static final DateTimeFormatter DISPLAY_DUE = DateTimeFormatter.ofPattern("MMM d, yyyy · h:mm a");
    private static final DateTimeFormatter INPUT_DUE = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    public Post(long postId, long authorId, Long categoryId, String authorName, Role authorRole, String categoryName,
                String title, String content, String imageUrl, String visibility,
                LocalDateTime createdAt, long reactionCount, long commentCount,
                boolean reactedByCurrentUser) {
        this(postId, authorId, categoryId, authorName, authorRole, categoryName,
                title, content, imageUrl, visibility, createdAt, reactionCount, commentCount,
                reactedByCurrentUser, null);
    }

    public String getCreatedLabel() {
        return createdAt == null ? "" : createdAt.format(DISPLAY_TIME);
    }
    public String getDueLabel() {
        return deadlineDate == null ? "" : deadlineDate.format(DISPLAY_DUE);
    }
    public String getInputDueDate() {
        return deadlineDate == null ? "" : deadlineDate.format(INPUT_DUE);
    }
    public boolean hasDeadline() {
        return deadlineDate != null;
    }
    public boolean isDeadlinePost() {
        return deadlineDate != null;
    }
    public boolean isExpired() {
        return deadlineDate != null && deadlineDate.isBefore(LocalDateTime.now());
    }
    public String getStatus() {
        if (deadlineDate == null) return "";
        if (isExpired()) return "Expired";
        return java.time.Duration.between(LocalDateTime.now(), deadlineDate).toHours() <= 48 ? "Due soon" : "Upcoming";
    }
    public String getTimeRemainingLabel() {
        if (deadlineDate == null) return "";
        if (isExpired()) return "Expired";
        long hours = java.time.Duration.between(LocalDateTime.now(), deadlineDate).toHours();
        if (hours < 24) {
            long mins = java.time.Duration.between(LocalDateTime.now(), deadlineDate).toMinutes();
            if (hours <= 0) return Math.max(1, mins) + " mins left";
            return hours + "h left";
        }
        long days = hours / 24;
        return days == 1 ? "1 day left" : days + " days left";
    }
    public String getScopeLabel() {
        if ("ALL".equalsIgnoreCase(visibility)) return "All Students";
        if ("SEMESTER".equalsIgnoreCase(visibility)) return "My Semester";
        if ("SECTION".equalsIgnoreCase(visibility)) return "My Section";
        return visibility == null ? "" : visibility;
    }
    public String getSubjectName() { return categoryName; }
    public String getCreatorName() { return authorName; }
    public long getDeadlineId() { return postId; }

    public long getPostId() { return postId; }
    public long getAuthorId() { return authorId; }
    public Long getCategoryId() { return categoryId; }
    public String getAuthorName() { return authorName; }
    public Role getAuthorRole() { return authorRole; }
    public String getCategoryName() { return categoryName; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getImageUrl() { return imageUrl; }
    public String getVisibility() { return visibility; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getDeadlineDate() { return deadlineDate; }
    public long getReactionCount() { return reactionCount; }
    public long getCommentCount() { return commentCount; }
    public boolean isReactedByCurrentUser() { return reactedByCurrentUser; }
}
