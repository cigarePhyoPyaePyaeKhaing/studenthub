package com.studenthub.dao;

import com.studenthub.model.University;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UniversityDAOTest {

    @Test
    void testUniversityModelEqualityAndAttributes() {
        LocalDateTime now = LocalDateTime.now();
        University u1 = new University(1L, "University of Information Technology", "UIT", "APPROVED", 10L, now);
        University u2 = new University(1L, "University of Information Technology", "UIT", "APPROVED", 10L, now);

        assertEquals(u1, u2);
        assertEquals("University of Information Technology (UIT)", u1.getDisplayName());
        assertTrue(u1.isActive());
    }

    @Test
    void testUniversityInactiveStatus() {
        University u = new University(2L, "Testing University", "TU", "INACTIVE", null, null);
        assertFalse(u.isActive());
        assertEquals("Testing University (TU)", u.getDisplayName());
    }
}
