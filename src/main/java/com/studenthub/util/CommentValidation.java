package com.studenthub.util;

public final class CommentValidation {
    public static final int MAX_LENGTH = 5000;
    private CommentValidation() {}
    public static String validate(String content) {
        return validate(content, false);
    }
    public static String validate(String content, boolean hasAttachment) {
        if ((content == null || content.trim().isEmpty()) && !hasAttachment) return "Enter a comment or choose an attachment before posting.";
        if (content.trim().length() > MAX_LENGTH) return "Comments must be 5000 characters or fewer.";
        return null;
    }
    public static String normalize(String content) { return content == null ? "" : content.trim(); }
}
