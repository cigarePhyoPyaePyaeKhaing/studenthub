package com.studenthub.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record Notification(long notificationId, String type, String title, String message,
                           String linkUrl, boolean read, LocalDateTime createdAt) {
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getCreatedLabel() {
        return createdAt == null ? "" : createdAt.format(DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a"));
    }
}
