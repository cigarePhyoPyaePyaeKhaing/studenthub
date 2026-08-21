package com.studenthub.service;

import com.studenthub.dao.AcademicChangeDAO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AcademicChangeWorkflowTest {

    @Test
    void testAcademicChangeItemRecordProperties() {
        AcademicChangeDAO.Item item = new AcademicChangeDAO.Item(
                1L, 10L, "TNT-0001", "Aung Aung", 3, "A", 4, "B", "Schedule change", "PENDING", null);

        assertEquals(1L, item.requestId());
        assertEquals(10L, item.userId());
        assertEquals("TNT-0001", item.studentId());
        assertEquals("Aung Aung", item.fullName());
        assertEquals(3, item.oldSemester());
        assertEquals("A", item.oldSection());
        assertEquals(4, item.requestedSemester());
        assertEquals("B", item.requestedSection());
        assertEquals("Schedule change", item.reason());
        assertEquals("PENDING", item.status());
        assertNull(item.adminNote());
    }

    @Test
    void testReviewedItemRecord() {
        AcademicChangeDAO.Item approvedItem = new AcademicChangeDAO.Item(
                1L, 10L, "TNT-0001", "Aung Aung", 3, "A", 4, "B", "Schedule change", "APPROVED", "Approved by Dean");

        assertEquals("APPROVED", approvedItem.status());
        assertEquals("Approved by Dean", approvedItem.adminNote());
    }
}
