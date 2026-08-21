package com.studenthub.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record PostComment(long commentId, long postId, long authorId, String authorName,
                          Role authorRole, String content, LocalDateTime createdAt) {
    private static final DateTimeFormatter DISPLAY_TIME = DateTimeFormatter.ofPattern("MMM d, yyyy · h:mm a");
    public long getCommentId() { return commentId; }
    public long getPostId() { return postId; }
    public long getAuthorId() { return authorId; }
    public String getAuthorName() { return authorName; }
    public Role getAuthorRole() { return authorRole; }
    public String getContent() { return content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getCreatedLabel() { return createdAt == null ? "" : createdAt.format(DISPLAY_TIME); }
}
