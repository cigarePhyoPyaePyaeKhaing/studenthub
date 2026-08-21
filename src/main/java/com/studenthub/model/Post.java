package com.studenthub.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public record Post(
        long postId,
        long authorId,
        Long categoryId,
        String authorName,
        Role authorRole,
        String categoryName,
        String title,
        String content,
        String imageUrl,
        String visibility,
        LocalDateTime createdAt,
        long reactionCount,
        long commentCount,
        boolean reactedByCurrentUser,
        List<Attachment> attachments) {

    private static final DateTimeFormatter DISPLAY_TIME = DateTimeFormatter.ofPattern("MMM d, yyyy · h:mm a");

    public Post(long postId, long authorId, Long categoryId, String authorName, Role authorRole, String categoryName,
                String title, String content, String imageUrl, String visibility,
                LocalDateTime createdAt, long reactionCount, long commentCount,
                boolean reactedByCurrentUser) {
        this(postId, authorId, categoryId, authorName, authorRole, categoryName, title, content, imageUrl, visibility,
                createdAt, reactionCount, commentCount, reactedByCurrentUser, List.of());
    }

    public String getCreatedLabel() {
        return createdAt == null ? "" : createdAt.format(DISPLAY_TIME);
    }

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
    public long getReactionCount() { return reactionCount; }
    public long getCommentCount() { return commentCount; }
    public boolean isReactedByCurrentUser() { return reactedByCurrentUser; }
    public List<Attachment> getAttachments() { return attachments == null ? List.of() : attachments; }
}
