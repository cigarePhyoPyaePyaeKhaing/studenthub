package com.studenthub.model;

public enum DiscussionScope {
    SECTION, SEMESTER, ALL, CR_SEMESTER, CR_ALL;

    public boolean isCrOnly() {
        return this == CR_SEMESTER || this == CR_ALL;
    }

    public static DiscussionScope fromRequest(String value) {
        if (value == null || value.isBlank()) return SECTION;
        try {
            return valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ignored) {
            return SECTION;
        }
    }
}
