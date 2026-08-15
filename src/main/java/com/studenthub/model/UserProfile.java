package com.studenthub.model;

public record UserProfile(long userId, String studentId, String fullName, String email,
                          Role role, boolean emailVerified, Integer semester, String sectionName) {
    public long getUserId() { return userId; }
    public String getStudentId() { return studentId; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public Role getRole() { return role; }
    public boolean isEmailVerified() { return emailVerified; }
    public Integer getSemester() { return semester; }
    public String getSectionName() { return sectionName; }
    public String getInitial() {
        return fullName == null || fullName.isBlank() ? "S" : fullName.substring(0, 1).toUpperCase();
    }
}
