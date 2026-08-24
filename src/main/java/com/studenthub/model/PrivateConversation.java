package com.studenthub.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record PrivateConversation(long conversationId,long otherUserId,String otherName,String otherAvatar,
                                  LocalDateTime lastActive,String preview,LocalDateTime updatedAt,long unread){
 public long getConversationId(){return conversationId;} public long getOtherUserId(){return otherUserId;}
 public String getOtherName(){return otherName;} public String getOtherAvatar(){return otherAvatar;}
 public String getPreview(){return preview==null?"No messages yet":preview;} public long getUnread(){return unread;}
 public boolean isActiveNow(){return lastActive!=null&&lastActive.isAfter(LocalDateTime.now().minusMinutes(3));}
 public String getPresenceLabel(){if(isActiveNow())return "Active now";return "Last seen recently";}
 public String getUpdatedLabel(){return updatedAt==null?"":updatedAt.format(DateTimeFormatter.ofPattern("MMM d, h:mm a"));}
}
