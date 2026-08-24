package com.studenthub.model;

import java.time.LocalDateTime;
import com.studenthub.util.YangonTime;

public record PrivateMessage(long messageId,long conversationId,long senderId,String message,
                             LocalDateTime createdAt,LocalDateTime deliveredAt,LocalDateTime seenAt,Attachment attachment){
 public long getMessageId(){return messageId;} public long getConversationId(){return conversationId;}
 public long getSenderId(){return senderId;} public String getMessage(){return message;}
 public LocalDateTime getCreatedAt(){return createdAt;} public Attachment getAttachment(){return attachment;}
 public String getCreatedLabel(){return YangonTime.label(createdAt);} public String getStatus(){return seenAt!=null?"SEEN":deliveredAt!=null?"DELIVERED":"SENT";}
 public LocalDateTime getDeliveredAt(){return deliveredAt;} public LocalDateTime getSeenAt(){return seenAt;}
}
