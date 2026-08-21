package com.studenthub.model;

import java.time.LocalDateTime;

public record University(
        long universityId,
        String name,
        String shortName,
        String status,
        Long requestedBy,
        Long approvedBy,
        LocalDateTime createdAt,
        LocalDateTime approvedAt) {

    public University(long universityId, String name, String shortName, String status, Long approvedBy, LocalDateTime approvedAt) {
        this(universityId, name, shortName, status, null, approvedBy, null, approvedAt);
    }

    public long getUniversityId() { return universityId; }
    public String getName() { return name; }
    public String getShortName() { return shortName; }
    public String getStatus() { return status; }
    public Long getRequestedBy() { return requestedBy; }
    public Long getApprovedBy() { return approvedBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getApprovedAt() { return approvedAt; }

    public boolean isApproved() {
        return "APPROVED".equalsIgnoreCase(status);
    }

    public boolean isActive() {
        return "APPROVED".equalsIgnoreCase(status);
    }

    public String getDisplayName() {
        if (shortName != null && !shortName.isBlank()) {
            return name + " (" + shortName + ")";
        }
        return name;
    }
}
