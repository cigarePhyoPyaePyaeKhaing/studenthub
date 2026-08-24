package com.studenthub.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record Notification(long notificationId, String type, String title, String message,
                           String linkUrl, boolean read, LocalDateTime createdAt,
                           Long actorUserId, String actorName, String actorAvatarUrl) {
    public Notification(long notificationId, String type, String title, String message,
                        String linkUrl, boolean read, LocalDateTime createdAt) {
        this(notificationId, type, title, message, linkUrl, read, createdAt, null, null, null);
    }
    public long getNotificationId() {
        return notificationId;
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public String getLinkUrl() {
        return linkUrl;
    }

    public boolean isRead() {
        return read;
    }

    public String getCreatedLabel() {
        return createdAt == null ? "" : createdAt.format(DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a"));
    }

    public Long getActorUserId() { return actorUserId; }
    public String getActorName() { return actorName; }
    public String getActorAvatarUrl() { return actorAvatarUrl; }
    public boolean isActorAvailable() { return actorUserId != null && actorName != null && !actorName.isBlank(); }
    public String getActorInitial() { return isActorAvailable() ? actorName.trim().substring(0, 1).toUpperCase() : "S"; }
    public String getActorAction() {
        if ("REACTION".equals(type)) return "liked your announcement.";
        if ("COMMENT".equals(type)) return "commented on your announcement.";
        return message;
    }

    public String getIconType() {
        return switch (type == null ? "" : type) {
            case "REACTION" -> "heart";
            case "COMMENT", "DISCUSSION" -> "comment";
            case "ANNOUNCEMENT" -> "megaphone";
            case "DEADLINE" -> "calendar";
            default -> "system";
        };
    }
}
