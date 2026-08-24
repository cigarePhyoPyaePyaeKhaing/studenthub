package com.studenthub.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record PrivateMessage(long messageId,long conversationId,long senderId,String message,
                             LocalDateTime createdAt,Attachment attachment){
 public long getMessageId(){return messageId;} public long getConversationId(){return conversationId;}
 public long getSenderId(){return senderId;} public String getMessage(){return message;}
 public LocalDateTime getCreatedAt(){return createdAt;} public Attachment getAttachment(){return attachment;}
 public String getCreatedLabel(){return createdAt.format(DateTimeFormatter.ofPattern("MMM d, h:mm a"));}
}
