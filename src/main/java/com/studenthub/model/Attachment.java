package com.studenthub.model;

public record Attachment(String originalName, String storedName, String mimeType, long size) {
    public String getOriginalName() { return originalName; }
    public String getStoredName() { return storedName; }
    public String getMimeType() { return mimeType; }
    public long getSize() { return size; }
    public boolean isImage() { return mimeType != null && mimeType.startsWith("image/"); }
    public boolean isVideo() { return mimeType != null && mimeType.startsWith("video/"); }
    public boolean isAudio() { return mimeType != null && mimeType.startsWith("audio/"); }
    public String getSizeLabel() { return size < 1048576 ? Math.max(1, size / 1024) + " KB" : String.format("%.1f MB", size / 1048576d); }
}
