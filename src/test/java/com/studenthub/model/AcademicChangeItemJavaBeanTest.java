package com.studenthub.model;

import com.studenthub.dao.AcademicChangeDAO;
import org.junit.jupiter.api.Test;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class AcademicChangeItemJavaBeanTest {

    @Test
    void academicChangeItemExposesExpectedProperties() throws Exception {
        LocalDateTime now = LocalDateTime.of(2026, 8, 21, 18, 0);
        AcademicChangeDAO.Item item = new AcademicChangeDAO.Item(
                10L, 55L, "TNT-0055", "Test Student", "test55@uit.edu",
                4, "C", 5, "B", "Change major section", "PENDING", "Note",
                now, now);

        assertEquals(10L, item.getRequestId());
        assertEquals(55L, item.getUserId());
        assertEquals("TNT-0055", item.getStudentId());
        assertEquals("Test Student", item.getFullName());
        assertEquals("test55@uit.edu", item.getEmail());
        assertEquals(4, item.getOldSemester());
        assertEquals("C", item.getOldSection());
        assertEquals(5, item.getRequestedSemester());
        assertEquals("B", item.getRequestedSection());
        assertEquals("Change major section", item.getReason());
        assertEquals("PENDING", item.getStatus());
        assertEquals("Note", item.getAdminNote());
        assertEquals(now, item.getCreatedAt());
        assertEquals(now, item.getReviewedAt());
        assertEquals("2026-08-21 18:00", item.getCreatedLabel());
        assertEquals("2026-08-21 18:00", item.getReviewedLabel());

        PropertyDescriptor[] descriptors = Introspector.getBeanInfo(AcademicChangeDAO.Item.class).getPropertyDescriptors();
        Set<String> propertyNames = Arrays.stream(descriptors)
                .map(PropertyDescriptor::getName)
                .collect(Collectors.toSet());

        assertTrue(propertyNames.contains("requestId"));
        assertTrue(propertyNames.contains("userId"));
        assertTrue(propertyNames.contains("studentId"));
        assertTrue(propertyNames.contains("fullName"));
        assertTrue(propertyNames.contains("email"));
        assertTrue(propertyNames.contains("oldSemester"));
        assertTrue(propertyNames.contains("oldSection"));
        assertTrue(propertyNames.contains("requestedSemester"));
        assertTrue(propertyNames.contains("requestedSection"));
        assertTrue(propertyNames.contains("reason"));
        assertTrue(propertyNames.contains("status"));
        assertTrue(propertyNames.contains("adminNote"));
        assertTrue(propertyNames.contains("createdAt"));
        assertTrue(propertyNames.contains("reviewedAt"));
        assertTrue(propertyNames.contains("createdLabel"));
        assertTrue(propertyNames.contains("reviewedLabel"));
    }
}