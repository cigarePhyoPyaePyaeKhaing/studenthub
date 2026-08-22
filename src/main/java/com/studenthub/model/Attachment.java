package com.studenthub.model;

public record Attachment(long attachmentId, Long postId, Long commentId, Long messageId,
                         String originalFilename, String storageKey, String mimeType, long fileSize) {
    public long getAttachmentId(){return attachmentId;} public String getOriginalFilename(){return originalFilename;}
    public String getMimeType(){return mimeType;} public long getFileSize(){return fileSize;}
    public boolean isImage(){return mimeType != null && mimeType.startsWith("image/");}
    public boolean isVideo(){return mimeType != null && mimeType.startsWith("video/");}
    public String getSizeLabel(){if(fileSize<1024)return fileSize+" B"; if(fileSize<1048576)return String.format("%.1f KB",fileSize/1024d);return String.format("%.1f MB",fileSize/1048576d);}
}
