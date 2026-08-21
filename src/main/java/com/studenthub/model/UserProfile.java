package com.studenthub.model;

public record UserProfile(long userId, String studentId, String fullName, String email,
                          Role role, boolean emailVerified, Integer semester, String sectionName,
                          Long universityId, String universityName, String universityShortName,
                          boolean universityLocked, boolean academicInfoLocked) {
    public long getUserId() { return userId; }
    public String getStudentId() { return studentId; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public Role getRole() { return role; }
    public boolean isEmailVerified() { return emailVerified; }
    public boolean getEmailVerified() { return emailVerified; }
    public Integer getSemester() { return semester; }
    public String getSectionName() { return sectionName; }
    public Long getUniversityId() { return universityId; }
    public String getUniversityName() { return universityName; }
    public String getUniversityShortName() { return universityShortName; }
    public boolean isUniversityLocked() { return universityLocked; }
    public boolean getUniversityLocked() { return universityLocked; }
    public boolean isAcademicInfoLocked() { return academicInfoLocked; }
    public boolean getAcademicInfoLocked() { return academicInfoLocked; }
    public UserProfile(long userId, String studentId, String fullName, String email, Role role, boolean emailVerified, Integer semester, String sectionName) {
        this(userId, studentId, fullName, email, role, emailVerified, semester, sectionName, null, null, null, false, false);
    }
    public String getInitial() {
        return fullName == null || fullName.isBlank() ? "S" : fullName.substring(0, 1).toUpperCase();
    }
}
