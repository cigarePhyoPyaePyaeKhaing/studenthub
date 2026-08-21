package com.studenthub.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UniversityTest {

    @Test
    void testUniversityCreationAndGetters() {
        LocalDateTime now = LocalDateTime.now();
        University uni = new University(1L, "University of Information Technology", "UIT", "APPROVED", 2L, now);

        assertEquals(1L, uni.getUniversityId());
        assertEquals("University of Information Technology", uni.getName());
        assertEquals("UIT", uni.getShortName());
        assertEquals("APPROVED", uni.getStatus());
        assertEquals(2L, uni.getApprovedBy());
        assertEquals(now, uni.getApprovedAt());
        assertTrue(uni.isActive());
        assertEquals("University of Information Technology (UIT)", uni.getDisplayName());
    }

    @Test
    void testUniversityDisplayNameWithoutShortName() {
        University uni = new University(2L, "Yangon University", null, "APPROVED", null, null);
        assertEquals("Yangon University", uni.getDisplayName());
        assertTrue(uni.isActive());
    }

    @Test
    void testInactiveUniversity() {
        University uni = new University(3L, "Inactive College", "IC", "INACTIVE", null, null);
        assertFalse(uni.isActive());
    }
}
