package com.studenthub.util;

public final class DiscussionValidation {
    public static final int MAX_MESSAGE_LENGTH = 2000;

    private DiscussionValidation() {}

    public static String validate(String message) {
        if (message == null || message.trim().isEmpty()) return "Enter a message before sending.";
        if (message.trim().length() > MAX_MESSAGE_LENGTH) {
            return "Messages must be " + MAX_MESSAGE_LENGTH + " characters or fewer.";
        }
        return null;
    }

    public static String normalize(String message) { return message == null ? "" : message.trim(); }
}
