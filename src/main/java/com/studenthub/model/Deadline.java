package com.studenthub.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record Deadline(long deadlineId, Long postId, String relatedPostTitle, String title,
                       String subjectName, LocalDateTime dueDate, int semester, String sectionName,
                       long createdBy, String creatorName, LocalDateTime createdAt) {
    private static final DateTimeFormatter DISPLAY_DATE = DateTimeFormatter.ofPattern("MMM d, yyyy, h:mm a");
    private static final DateTimeFormatter INPUT_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    public long getDeadlineId() { return deadlineId; }
    public Long getPostId() { return postId; }
    public String getRelatedPostTitle() { return relatedPostTitle; }
    public String getTitle() { return title; }
    public String getSubjectName() { return subjectName; }
    public LocalDateTime getDueDate() { return dueDate; }
    public int getSemester() { return semester; }
    public String getSectionName() { return sectionName; }
    public long getCreatedBy() { return createdBy; }
    public String getCreatorName() { return creatorName; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public String getDueLabel() { return dueDate == null ? "" : dueDate.format(DISPLAY_DATE); }
    public String getInputDueDate() { return dueDate == null ? "" : dueDate.format(INPUT_DATE); }
    public String getIsoDueDate() { return dueDate == null ? "" : dueDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME); }
    public String getScopeLabel() { return sectionName == null ? "Semester " + semester : "Semester " + semester + " / " + sectionName; }
    public boolean isExpired() { return dueDate != null && dueDate.isBefore(LocalDateTime.now()); }
    public String getStatus() {
        if (isExpired()) return "Expired";
        return Duration.between(LocalDateTime.now(), dueDate).toHours() <= 48 ? "Due soon" : "Upcoming";
    }
}
