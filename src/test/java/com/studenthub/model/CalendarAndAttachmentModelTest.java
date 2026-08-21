package com.studenthub.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CalendarAndAttachmentModelTest {

    @Test
    void testDeadlineJavaBeanGettersAndProperties() {
        LocalDateTime due = LocalDateTime.of(2026, 9, 15, 14, 30);
        LocalDateTime created = LocalDateTime.of(2026, 9, 1, 10, 0);
        Deadline deadline = new Deadline(
                101L, 201L, "Announcement #1", "Final Project",
                "Advanced Java", due, 5, "Section A", 10L, "Admin User", created);

        assertEquals(101L, deadline.getDeadlineId());
        assertEquals(201L, deadline.getPostId());
        assertEquals("Announcement #1", deadline.getRelatedPostTitle());
        assertEquals("Final Project", deadline.getTitle());
        assertEquals("Advanced Java", deadline.getSubjectName());
        assertEquals(due, deadline.getDueDate());
        assertEquals(5, deadline.getSemester());
        assertEquals("Section A", deadline.getSectionName());
        assertEquals(10L, deadline.getCreatedBy());
        assertEquals("Admin User", deadline.getCreatorName());
        assertEquals(created, deadline.getCreatedAt());
        assertEquals("2026-09-15T14:30", deadline.getInputDueDate());
        assertEquals("Semester 5 / Section A", deadline.getScopeLabel());
        assertFalse(deadline.isExpired());
    }

    @Test
    void testAttachmentModelProperties() {
        LocalDateTime now = LocalDateTime.now();
        Attachment attImg = new Attachment(1L, "POST", 10L, "slide.png", "uuid-1.png",
                "IMAGE", "image/png", 1024 * 500, 5L, now);

        assertEquals(1L, attImg.getAttachmentId());
        assertEquals("POST", attImg.getEntityType());
        assertEquals(10L, attImg.getEntityId());
        assertEquals("slide.png", attImg.getOriginalFilename());
        assertEquals("uuid-1.png", attImg.getStoredFilename());
        assertEquals("IMAGE", attImg.getFileType());
        assertEquals("image/png", attImg.getMimeType());
        assertEquals(1024 * 500, attImg.getFileSize());
        assertEquals(5L, attImg.getUploaderId());
        assertEquals(now, attImg.getCreatedAt());

        assertTrue(attImg.isImage());
        assertFalse(attImg.isVideo());
        assertFalse(attImg.isDocument());
        assertEquals("500.0 KB", attImg.getFormattedSize());

        Attachment attDoc = new Attachment(2L, "MESSAGE", 20L, "syllabus.pdf", "uuid-2.pdf",
                "DOCUMENT", "application/pdf", 1024 * 1024 * 3, 5L, now);
        assertTrue(attDoc.isDocument());
        assertEquals("3.0 MB", attDoc.getFormattedSize());

        Attachment attVid = new Attachment(3L, "POST", 10L, "demo.mp4", "uuid-3.mp4",
                "VIDEO", "video/mp4", 1024 * 1024 * 12, 5L, now);
        assertTrue(attVid.isVideo());
        assertEquals("12.0 MB", attVid.getFormattedSize());
    }

    @Test
    void testPostCarriesAttachmentsList() {
        LocalDateTime now = LocalDateTime.now();
        Attachment att = new Attachment(1L, "POST", 10L, "slide.png", "uuid-1.png",
                "IMAGE", "image/png", 1024, 5L, now);

        Post post = new Post(10L, 5L, 1L, "Author", Role.CR, "Assignment", "HW 1", "Content",
                null, "ALL", now, 2, 3, false, List.of(att));

        assertEquals(1, post.getAttachments().size());
        assertEquals("slide.png", post.getAttachments().get(0).originalFilename());
        assertEquals(now, post.getCreatedAt());
        assertNull(post.getImageUrl());
    }

    @Test
    void testDiscussionMessageCarriesAttachmentsList() {
        LocalDateTime now = LocalDateTime.now();
        Attachment att = new Attachment(1L, "MESSAGE", 100L, "code.zip", "uuid-code.zip",
                "DOCUMENT", "application/zip", 2048, 5L, now);

        DiscussionMessage msg = new DiscussionMessage(100L, 5L, "Student Name", "STUDENT",
                3, "A", "Here is the code", now, List.of(att));

        assertEquals(1, msg.getAttachments().size());
        assertEquals("code.zip", msg.getAttachments().get(0).originalFilename());
        assertEquals(now, msg.getCreatedAt());
    }

    @Test
    void testAdminUserSummaryCarriesUniversityInformation() {
        LocalDateTime now = LocalDateTime.now();
        AdminUserSummary userSummary = new AdminUserSummary(
                1L, "TNT-0001", "Aung Aung", "aung@uit.edu.mm", Role.STUDENT, true,
                4, "B", now, "University of Information Technology", "UIT");

        assertEquals("University of Information Technology", userSummary.getUniversityName());
        assertEquals("UIT", userSummary.getUniversityShortName());
        assertEquals("TNT-0001", userSummary.getStudentId());
        assertEquals("Aung Aung", userSummary.getFullName());
        assertTrue(userSummary.isEmailVerified());
    }
}
