package com.studenthub.util;

public final class CommentValidation {
    public static final int MAX_LENGTH = 5000;
    private CommentValidation() {}
    public static String validate(String content) {
        if (content == null || content.trim().isEmpty()) return "Enter a comment before posting.";
        if (content.trim().length() > MAX_LENGTH) return "Comments must be 5000 characters or fewer.";
        return null;
    }
    public static String normalize(String content) { return content == null ? "" : content.trim(); }
}
