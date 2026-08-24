package com.studenthub.model;
import com.studenthub.util.YangonTime;import java.time.LocalDateTime;
public record UserSearchResult(long userId,String studentId,String fullName,String role,String avatarUrl,LocalDateTime lastActive){public long getUserId(){return userId;}public String getStudentId(){return studentId;}public String getFullName(){return fullName;}public String getRole(){return role;}public String getAvatarUrl(){return avatarUrl;}public String getPresenceLabel(){return YangonTime.presence(lastActive);}public boolean isActiveNow(){return YangonTime.active(lastActive);}}
