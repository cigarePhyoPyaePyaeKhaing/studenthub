package com.studenthub.util;

import static org.junit.jupiter.api.Assertions.*;

import com.studenthub.model.DiscussionScope;
import java.util.List;
import org.junit.jupiter.api.Test;

class AcademicGroupPolicyTest {
    @Test void exactSemesterMappingsAreStable() {
        for (int semester = 1; semester <= 10; semester++) {
            assertEquals(List.of("A", "B", "C", "D", "E"), AcademicGroupPolicy.optionsFor(semester));
            assertEquals("Section", AcademicGroupPolicy.groupLabel(semester));
        }
        assertEquals(50, AcademicGroupPolicy.allOptions().size());
    }

    @Test void requiredValidCombinationsAreAccepted() {
        assertValid(1, "A"); assertValid(2, "B"); assertValid(3, "C");
        assertValid(4, "D"); assertValid(5, "E"); assertValid(6, "A");
        assertValid(7, "A"); assertValid(7, "E"); assertValid(8, "A"); assertValid(8, "E");
        assertValid(9, "C"); assertValid(10, "E");
    }

    @Test void requiredInvalidCombinationsAreRejected() {
        assertFalse(AcademicGroupPolicy.isValid(3, "Section 1"));
        assertFalse(AcademicGroupPolicy.isValid(6, "F"));
        assertFalse(AcademicGroupPolicy.isValid(7, "F"));
        assertFalse(AcademicGroupPolicy.isValid(7, "invalid text"));
        assertFalse(AcademicGroupPolicy.isValid(8, "G"));
    }

    @Test void canonicalNormalizationPreservesAcademicGroupDisplayCase() {
        assertEquals("C", AcademicGroupPolicy.normalize(6, " c "));
        assertEquals("A", AcademicGroupPolicy.normalize(7, " a "));
        assertEquals("E", AcademicGroupPolicy.normalize(10, " e "));
        assertEquals("B", AcademicGroupPolicy.normalize(3, " b "));
    }

    @Test void discussionTargetsRemainSeparatedBySemesterAndGroup() {
        DiscussionTarget semester3A = DiscussionTarget.fromAuthenticatedUser(1, DiscussionScope.SECTION, 5L, 3, "A");
        DiscussionTarget semester3B = DiscussionTarget.fromAuthenticatedUser(1, DiscussionScope.SECTION, 5L, 3, "B");
        DiscussionTarget semester7A = DiscussionTarget.fromAuthenticatedUser(1, DiscussionScope.SECTION, 5L, 7, "A");
        DiscussionTarget semester7B = DiscussionTarget.fromAuthenticatedUser(1, DiscussionScope.SECTION, 5L, 7, "B");
        assertNotEquals(semester3A, semester3B);
        assertNotEquals(semester7A, semester7B);
        assertFalse(DiscussionAccess.matches(DiscussionScope.SECTION, 3, "A", 3, "B"));
        assertFalse(DiscussionAccess.matches(DiscussionScope.SECTION, 7, "A", 7, "B"));
    }

    private static void assertValid(int semester, String group) {
        assertTrue(AcademicGroupPolicy.isValid(semester, group), semester + "," + group);
    }
}
