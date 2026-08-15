package com.studenthub.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Set;

public final class DeadlineValidation {
    private DeadlineValidation() {
    }

    public static String validate(String title, String subject, String dueInput, String scope) {
        if (title == null || title.isBlank() || title.length() > 200) return "Title is required and cannot exceed 200 characters.";
        if (subject == null || subject.isBlank() || subject.length() > 100) return "Subject is required and cannot exceed 100 characters.";
        if (!Set.of("SEMESTER", "SECTION").contains(scope)) return "Select a valid deadline scope.";
        LocalDateTime due = parseDueDate(dueInput);
        if (due == null) return "Enter a valid due date and time.";
        if (!due.isAfter(LocalDateTime.now())) return "Due date must be in the future.";
        return null;
    }

    public static LocalDateTime parseDueDate(String value) {
        if (value == null || value.isBlank()) return null;
        try { return LocalDateTime.parse(value.trim()); }
        catch (DateTimeParseException exception) { return null; }
    }
}
