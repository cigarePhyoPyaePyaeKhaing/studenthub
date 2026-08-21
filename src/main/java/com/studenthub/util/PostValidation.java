package com.studenthub.util;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Set;

public final class PostValidation {
    private static final Set<String> MANDATORY_DEADLINE_CATEGORIES = Set.of("ASSIGNMENT", "TUTORIAL", "EXAM");

    private PostValidation() {
    }

    public static String validate(String title, String content, Long categoryId, String visibility) {
        return validate(title, content, categoryId, null, visibility, null);
    }

    public static String validate(String title, String content, Long categoryId, String categoryName,
                                  String visibility, LocalDateTime dueDateTime) {
        if (title == null || title.isBlank() || title.length() > 200) {
            return "Title is required and cannot exceed 200 characters.";
        }
        if (content == null || content.isBlank() || content.length() > 10_000) {
            return "Content is required and cannot exceed 10,000 characters.";
        }
        if (categoryId == null || categoryId <= 0) {
            return "Select a valid category.";
        }
        if (!Set.of("ALL", "SEMESTER", "SECTION").contains(visibility)) {
            return "Select a valid visibility.";
        }
        if (categoryName != null) {
            String upper = categoryName.trim().toUpperCase(Locale.ROOT);
            if (MANDATORY_DEADLINE_CATEGORIES.contains(upper)) {
                if (dueDateTime == null) {
                    return "A deadline date and time is required for " + categoryName + " announcements.";
                }
            }
        }
        return null;
    }

    public static boolean isDeadlineRequired(String categoryName) {
        if (categoryName == null) return false;
        return MANDATORY_DEADLINE_CATEGORIES.contains(categoryName.trim().toUpperCase(Locale.ROOT));
    }
}
