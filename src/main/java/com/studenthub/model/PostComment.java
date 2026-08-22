package com.studenthub.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record PostComment(long commentId, long postId, long authorId, String authorName,
                          Role authorRole, String content, LocalDateTime createdAt, String authorAvatarUrl, Attachment attachment) {
    private static final DateTimeFormatter DISPLAY_TIME = DateTimeFormatter.ofPattern("MMM d, yyyy · h:mm a");
    public long getCommentId() { return commentId; }
    public long getPostId() { return postId; }
    public long getAuthorId() { return authorId; }
    public String getAuthorName() { return authorName; }
    public Role getAuthorRole() { return authorRole; }
    public String getContent() { return content; }
    public String getAuthorAvatarUrl() { return authorAvatarUrl; }
    public String getCreatedLabel() { return createdAt == null ? "" : createdAt.format(DISPLAY_TIME); }
    public PostComment(long commentId, long postId, long authorId, String authorName,
                       Role authorRole, String content, LocalDateTime createdAt) {
        this(commentId, postId, authorId, authorName, authorRole, content, createdAt, null, null);
    }
    public PostComment(long commentId,long postId,long authorId,String authorName,Role authorRole,String content,LocalDateTime createdAt,String authorAvatarUrl){this(commentId,postId,authorId,authorName,authorRole,content,createdAt,authorAvatarUrl,null);}
    public PostComment withAttachment(Attachment value){return new PostComment(commentId,postId,authorId,authorName,authorRole,content,createdAt,authorAvatarUrl,value);} public Attachment getAttachment(){return attachment;}
}
