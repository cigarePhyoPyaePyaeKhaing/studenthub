package com.studenthub.util;

import com.studenthub.model.ProfileUpdate;

public final class ProfileValidation {
    public static final int MIN_SEMESTER = 1;
    public static final int MAX_SEMESTER = 10;
    public static final int MAX_SECTION_LENGTH = 20;
    private static final String SECTION_PATTERN = "[A-Za-z0-9][A-Za-z0-9 -]{0,19}";

    private ProfileValidation() {}

    public record Result(ProfileUpdate update, String error) {
        public boolean valid() { return error == null; }
    }

    public static Result validate(String fullNameInput, String semesterInput, String sectionInput) {
        String fullName = fullNameInput == null ? "" : fullNameInput.trim();
        if (fullName.length() < 2 || fullName.length() > 100) {
            return new Result(null, "Enter a full name between 2 and 100 characters.");
        }

        Integer semester = null;
        if (semesterInput != null && !semesterInput.isBlank()) {
            try { semester = Integer.valueOf(semesterInput.trim()); }
            catch (NumberFormatException exception) {
                return new Result(null, "Select a valid semester.");
            }
            if (semester < MIN_SEMESTER || semester > MAX_SEMESTER) {
                return new Result(null, "Semester must be between 1 and 10.");
            }
        }

        String section = sectionInput == null ? null : sectionInput.trim();
        if (section != null && section.isEmpty()) section = null;
        if (section != null && (section.length() > MAX_SECTION_LENGTH || !section.matches(SECTION_PATTERN))) {
            return new Result(null, "Section may contain letters, numbers, spaces, and hyphens only (maximum 20 characters).");
        }
        if (semester == null && section != null) {
            return new Result(null, "Select a semester before setting a section.");
        }
        return new Result(new ProfileUpdate(fullName, semester, section), null);
    }
}
