package com.studenthub.util;

public final class DiscussionValidation {
    public static final int MAX_MESSAGE_LENGTH = 2000;

    private DiscussionValidation() {}

    public static String validate(String message) {
        return validate(message, false);
    }
    public static String validate(String message, boolean hasAttachment) {
        if ((message == null || message.trim().isEmpty()) && !hasAttachment) return "Enter a message or choose an attachment before sending.";
        if (message != null && message.trim().length() > MAX_MESSAGE_LENGTH) {
            return "Messages must be " + MAX_MESSAGE_LENGTH + " characters or fewer.";
        }
        return null;
    }

    public static String normalize(String message) { return message == null ? "" : message.trim(); }
}
