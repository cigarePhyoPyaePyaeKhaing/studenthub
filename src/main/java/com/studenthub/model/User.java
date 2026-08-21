package com.studenthub.model;

public record User(
        long userId,
        String studentId,
        String fullName,
        String email,
        String passwordHash,
        Role role,
        boolean emailVerified,
        String googleSub) {

    public long getUserId() { return userId; }
    public String getStudentId() { return studentId; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public Role getRole() { return role; }
    public boolean isEmailVerified() { return emailVerified; }
    public String getGoogleSub() { return googleSub; }
}
