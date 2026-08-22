package com.studenthub.model;

public record UserProfile(
        long userId,
        String studentId,
        String fullName,
        String email,
        Role role,
        boolean emailVerified,
        Integer semester,
        String sectionName,
        String major,
        String phone,
        String address,
        String bio,
        String avatarUrl,
        Long universityId,
        String universityName,
        String universityShortName,
        boolean universityLocked,
        boolean academicInfoLocked,
        String profileVisibility) {

    public long getUserId() { return userId; }
    public long getId() { return userId; }
    public String getStudentId() { return studentId; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public Role getRole() { return role; }
    public boolean isEmailVerified() { return emailVerified; }
    public boolean getEmailVerified() { return emailVerified; }
    public Integer getSemester() { return semester; }
    public String getSectionName() { return sectionName; }
    public String getSection() { return sectionName; }
    public String getMajor() { return major; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public String getBio() { return bio; }
    public String getAvatarUrl() { return avatarUrl; }
    public Long getUniversityId() { return universityId; }
    public String getUniversityName() { return universityName; }
    public String getUniversityShortName() { return universityShortName; }
    public boolean isUniversityLocked() {
        return universityLocked || (universityId != null && universityId > 0);
    }

    public boolean getUniversityLocked() {
        return isUniversityLocked();
    }

    @Override
    public boolean universityLocked() {
        return isUniversityLocked();
    }

    public boolean isAcademicInfoLocked() {
        return semester != null && sectionName != null && !sectionName.isBlank();
    }

    public boolean getAcademicInfoLocked() {
        return isAcademicInfoLocked();
    }

    public String getProfileVisibility() {
        return "PUBLIC".equalsIgnoreCase(profileVisibility) ? "PUBLIC" : "PRIVATE";
    }

    public boolean isProfilePublic() { return "PUBLIC".equals(getProfileVisibility()); }

    @Override
    public boolean academicInfoLocked() {
        return isAcademicInfoLocked();
    }

    public UserProfile(long userId, String studentId, String fullName, String email,
                       Role role, boolean emailVerified, Integer semester, String sectionName) {
        this(userId, studentId, fullName, email, role, emailVerified, semester, sectionName,
                null, null, null, null, null, null, null, null, false,
                semester != null && sectionName != null && !sectionName.isBlank(), "PRIVATE");
    }

    /** Source-compatible constructor for callers created before profile visibility was persisted. */
    public UserProfile(long userId, String studentId, String fullName, String email,
                       Role role, boolean emailVerified, Integer semester, String sectionName,
                       String major, String phone, String address, String bio, String avatarUrl,
                       Long universityId, String universityName, String universityShortName,
                       boolean universityLocked, boolean academicInfoLocked) {
        this(userId, studentId, fullName, email, role, emailVerified, semester, sectionName,
                major, phone, address, bio, avatarUrl, universityId, universityName,
                universityShortName, universityLocked, academicInfoLocked, "PRIVATE");
    }

    public UserProfile(long userId, String studentId, String fullName, String email,
                       Role role, boolean emailVerified, Integer semester, String sectionName,
                       Long universityId, String universityName, String universityShortName,
                       boolean universityLocked, boolean academicInfoLocked) {
        this(userId, studentId, fullName, email, role, emailVerified, semester, sectionName,
                null, null, null, null, null,
                universityId, universityName, universityShortName, universityLocked,
                semester != null && sectionName != null && !sectionName.isBlank(), "PRIVATE");
    }

    public String getInitial() {
        return fullName == null || fullName.isBlank() ? "S" : fullName.substring(0, 1).toUpperCase();
    }
}
