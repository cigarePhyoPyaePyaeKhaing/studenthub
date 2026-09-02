package com.studenthub.util;

import com.studenthub.model.ProfileUpdate;

public final class ProfileValidation {
    public static final int MIN_SEMESTER = 1;
    public static final int MAX_SEMESTER = 10;
    public static final int MAX_SECTION_LENGTH = 20;

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
        if (semester == null && section != null) {
            return new Result(null, "Select a semester before setting a section.");
        }
        if (semester != null && section != null) {
            section = AcademicGroupPolicy.normalize(semester, section);
            if (section == null) {
                return new Result(null, "Select a valid "
                        + AcademicGroupPolicy.groupLabel(semester).toLowerCase(java.util.Locale.ROOT)
                        + " for Semester " + semester + ".");
            }
        }
        return new Result(new ProfileUpdate(fullName, semester, section), null);
    }
}
