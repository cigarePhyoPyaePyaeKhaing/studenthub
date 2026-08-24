package com.studenthub.model;

import java.time.LocalDateTime;
import com.studenthub.util.YangonTime;

public record PrivateConversation(long conversationId,long otherUserId,String otherName,String otherAvatar,
                                  LocalDateTime lastActive,String preview,LocalDateTime updatedAt,long unread){
 public long getConversationId(){return conversationId;} public long getOtherUserId(){return otherUserId;}
 public String getOtherName(){return otherName;} public String getOtherAvatar(){return otherAvatar;}
 public String getPreview(){return preview==null?"No messages yet":preview;} public long getUnread(){return unread;}
 public boolean isActiveNow(){return YangonTime.active(lastActive);}
 public String getPresenceLabel(){return YangonTime.presence(lastActive);}
 public String getUpdatedLabel(){return YangonTime.label(updatedAt);}
}
