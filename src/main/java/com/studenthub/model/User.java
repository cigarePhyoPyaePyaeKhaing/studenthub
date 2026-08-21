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
}
