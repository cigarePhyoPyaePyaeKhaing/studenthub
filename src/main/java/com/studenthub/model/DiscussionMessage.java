package com.studenthub.model;

import java.time.LocalDateTime;
import com.studenthub.util.YangonTime;

public record DiscussionMessage(long messageId, long senderId, String authorName,
                                String authorRole, Integer authorSemester, String authorSection,
                                String message, LocalDateTime createdAt, String authorAvatarUrl, Attachment attachment) {

    public long getMessageId() { return messageId; }
    public long getSenderId() { return senderId; }
    public String getAuthorName() { return authorName; }
    public String getAuthorRole() { return authorRole; }
    public Integer getAuthorSemester() { return authorSemester; }
    public String getAuthorSection() { return authorSection; }
    public String getMessage() { return message; }
    public String getAuthorAvatarUrl() { return authorAvatarUrl; }
    public String getCreatedLabel() { return YangonTime.label(createdAt); }

    public DiscussionMessage(long messageId, long senderId, String authorName, String authorRole,
                             String message, LocalDateTime createdAt) {
        this(messageId, senderId, authorName, authorRole, null, null, message, createdAt, null, null);
    }

    public DiscussionMessage(long messageId, long senderId, String authorName, String authorRole,
                             Integer authorSemester, String authorSection, String message, LocalDateTime createdAt) {
        this(messageId, senderId, authorName, authorRole, authorSemester, authorSection, message, createdAt, null, null);
    }
    public DiscussionMessage(long messageId,long senderId,String authorName,String authorRole,Integer authorSemester,String authorSection,String message,LocalDateTime createdAt,String authorAvatarUrl){this(messageId,senderId,authorName,authorRole,authorSemester,authorSection,message,createdAt,authorAvatarUrl,null);}
    public DiscussionMessage withAttachment(Attachment value){return new DiscussionMessage(messageId,senderId,authorName,authorRole,authorSemester,authorSection,message,createdAt,authorAvatarUrl,value);} public Attachment getAttachment(){return attachment;}
}
