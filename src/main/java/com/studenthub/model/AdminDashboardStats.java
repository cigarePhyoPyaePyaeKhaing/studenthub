package com.studenthub.model;

public record AdminDashboardStats(long totalUsers, long students, long crs, long admins,
                                  long announcements, long deadlines, long discussionMessages,
                                  long comments, long reactions, long verifiedUsers,
                                  long unverifiedUsers) {
    public long getTotalUsers() { return totalUsers; } public long getStudents() { return students; }
    public long getCrs() { return crs; } public long getAdmins() { return admins; }
    public long getAnnouncements() { return announcements; } public long getDeadlines() { return deadlines; }
    public long getDiscussionMessages() { return discussionMessages; } public long getComments() { return comments; }
    public long getReactions() { return reactions; } public long getVerifiedUsers() { return verifiedUsers; }
    public long getUnverifiedUsers() { return unverifiedUsers; }
}
