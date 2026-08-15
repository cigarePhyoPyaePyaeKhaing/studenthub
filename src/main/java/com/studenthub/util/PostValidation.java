package com.studenthub.util;

public final class PostValidation {
    private PostValidation() {
    }

    public static String validate(String title, String content, Long categoryId, String visibility) {
        if (title == null || title.isBlank() || title.length() > 200) {
            return "Title is required and cannot exceed 200 characters.";
        }
        if (content == null || content.isBlank() || content.length() > 10_000) {
            return "Content is required and cannot exceed 10,000 characters.";
        }
        if (categoryId == null || categoryId <= 0) return "Select a valid category.";
        if (!java.util.Set.of("ALL", "SEMESTER", "SECTION").contains(visibility)) {
            return "Select a valid visibility.";
        }
        return null;
    }
}
