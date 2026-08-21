package com.studenthub.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;

public final class PostValidation {
    private static final Set<String> DEADLINE_REQUIRED_CATEGORIES = Set.of(
            "assignment",
            "tutorial",
            "exam"
    );

    private PostValidation() {
    }

    public static boolean isDeadlineRequiredCategory(String categoryName) {
        if (categoryName == null || categoryName.isBlank()) return false;
        return DEADLINE_REQUIRED_CATEGORIES.contains(categoryName.trim().toLowerCase(Locale.ROOT));
    }

    public static LocalDateTime parseDeadline(String input) {
        if (input == null || input.isBlank()) return null;
        String trimmed = input.trim();
        try {
            return LocalDateTime.parse(trimmed);
        } catch (Exception ignored) {
        }
        DateTimeFormatter[] formatters = new DateTimeFormatter[]{
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        };
        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDateTime.parse(trimmed, formatter);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    public static String validate(String title, String content, Long categoryId, String visibility) {
        return validate(title, content, categoryId, null, visibility, null);
    }

    public static String validate(String title, String content, Long categoryId, String categoryName,
                                  String visibility, String deadlineDateInput) {
        if (title == null || title.isBlank() || title.length() > 200) {
            return "Title is required and cannot exceed 200 characters.";
        }
        if (content == null || content.isBlank() || content.length() > 10_000) {
            return "Content is required and cannot exceed 10,000 characters.";
        }
        if (categoryId == null || categoryId <= 0) return "Select a valid category.";
        if (!Set.of("ALL", "SEMESTER", "SECTION").contains(visibility)) {
            return "Select a valid visibility.";
        }
        boolean deadlineRequired = isDeadlineRequiredCategory(categoryName);
        if (deadlineRequired) {
            if (deadlineDateInput == null || deadlineDateInput.isBlank()) {
                String catDisplay = (categoryName != null && !categoryName.isBlank()) ? categoryName : "this category";
                return "Deadline is required for " + catDisplay + " announcements.";
            }
            LocalDateTime parsed = parseDeadline(deadlineDateInput);
            if (parsed == null) {
                return "Please provide a valid deadline date and time.";
            }
        } else if (deadlineDateInput != null && !deadlineDateInput.isBlank()) {
            LocalDateTime parsed = parseDeadline(deadlineDateInput);
            if (parsed == null) {
                return "Please provide a valid deadline date and time.";
            }
        }
        return null;
    }
}
