package com.studenthub.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record AdminUserSummary(
        long userId,
        String studentId,
        String fullName,
        String email,
        Role role,
        boolean emailVerified,
        Integer semester,
        String sectionName,
        LocalDateTime createdAt,
        String universityName,
        String universityShortName) {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy · h:mm a");

    public AdminUserSummary(long userId, String studentId, String fullName, String email,
                            Role role, boolean emailVerified, Integer semester,
                            String sectionName, LocalDateTime createdAt) {
        this(userId, studentId, fullName, email, role, emailVerified, semester, sectionName, createdAt, null, null);
    }

    public long getUserId() { return userId; }
    public String getStudentId() { return studentId; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public Role getRole() { return role; }
    public boolean isEmailVerified() { return emailVerified; }
    public Integer getSemester() { return semester; }
    public String getSectionName() { return sectionName; }
    public String getUniversityName() { return universityName; }
    public String getUniversityShortName() { return universityShortName; }
    public String getCreatedLabel() { return createdAt == null ? "" : createdAt.format(FORMAT); }
}
