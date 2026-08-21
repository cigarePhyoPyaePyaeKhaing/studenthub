package com.studenthub.model;

public record University(long universityId, String name, String shortName, String status) {

    public long getUniversityId() {
        return universityId;
    }

    public String getName() {
        return name;
    }

    public String getShortName() {
        return shortName;
    }

    public String getStatus() {
        return status;
    }

    public String getDisplayName() {
        if (shortName != null && !shortName.isBlank()) {
            return name + " (" + shortName + ")";
        }
        return name;
    }
}
