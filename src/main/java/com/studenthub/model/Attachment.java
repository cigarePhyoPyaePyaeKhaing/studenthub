package com.studenthub.model;

import java.time.LocalDateTime;

public record Attachment(
        long attachmentId,
        String entityType,
        long entityId,
        String originalFilename,
        String storedFilename,
        String fileType,
        String mimeType,
        long fileSize,
        long uploaderId,
        LocalDateTime createdAt) {

    public long getAttachmentId() { return attachmentId; }
    public String getEntityType() { return entityType; }
    public long getEntityId() { return entityId; }
    public String getOriginalFilename() { return originalFilename; }
    public String getStoredFilename() { return storedFilename; }
    public String getFileType() { return fileType; }
    public String getMimeType() { return mimeType; }
    public long getFileSize() { return fileSize; }
    public long getUploaderId() { return uploaderId; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public boolean isImage() {
        return "IMAGE".equalsIgnoreCase(fileType);
    }

    public boolean isVideo() {
        return "VIDEO".equalsIgnoreCase(fileType);
    }

    public boolean isDocument() {
        return "DOCUMENT".equalsIgnoreCase(fileType);
    }

    public String getFormattedSize() {
        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.1f KB", fileSize / 1024.0);
        } else {
            return String.format("%.1f MB", fileSize / (1024.0 * 1024.0));
        }
    }
}
