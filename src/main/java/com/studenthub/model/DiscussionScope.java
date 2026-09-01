package com.studenthub.model;

public enum DiscussionScope {
    SECTION, SEMESTER, ALL, CR_SEMESTER, CR_ALL, CR_ADMIN, ALL_STUDENTS_ADMIN;

    public boolean isCrOnly() {
        return this == CR_SEMESTER || this == CR_ALL || this == CR_ADMIN;
    }

    public boolean isAdminTargeted() {
        return this == CR_ADMIN || this == ALL_STUDENTS_ADMIN;
    }

    public static DiscussionScope fromRequest(String value) {
        if (value == null || value.isBlank()) return SECTION;
        try {
            String normalized = value.trim().toUpperCase().replace('-', '_').replace(' ', '_');
            while (normalized.contains("__")) normalized = normalized.replace("__", "_");
            if ("ALL_ADMIN".equals(normalized) || "STUDENT_ADMIN".equals(normalized) || "STUDENTS_ADMIN".equals(normalized)) {
                return ALL_STUDENTS_ADMIN;
            }
            return valueOf(normalized);
        } catch (IllegalArgumentException ignored) {
            return SECTION;
        }
    }
}
