package com.studenthub.util;

import static org.junit.jupiter.api.Assertions.*;

import com.studenthub.model.DiscussionScope;
import java.util.List;
import org.junit.jupiter.api.Test;

class AcademicGroupPolicyTest {
    @Test void exactSemesterMappingsAreStable() {
        assertEquals(List.of("A", "B", "C", "D", "E"), AcademicGroupPolicy.optionsFor(1));
        assertEquals(List.of("A", "B", "C", "D", "E"), AcademicGroupPolicy.optionsFor(2));
        assertEquals(List.of("A", "B", "C", "D"), AcademicGroupPolicy.optionsFor(3));
        assertEquals(List.of("A", "B", "C", "D"), AcademicGroupPolicy.optionsFor(4));
        assertEquals(List.of("A", "B", "C", "D", "E"), AcademicGroupPolicy.optionsFor(5));
        assertEquals(List.of("A", "B", "C", "D", "E"), AcademicGroupPolicy.optionsFor(6));
        for (int semester = 7; semester <= 10; semester++) {
            assertEquals(List.of("SE", "KE", "BIS", "HPC", "CN", "CSec", "ES"),
                    AcademicGroupPolicy.optionsFor(semester));
            assertEquals("Major", AcademicGroupPolicy.groupLabel(semester));
        }
        assertEquals(56, AcademicGroupPolicy.allOptions().size());
    }

    @Test void requiredValidCombinationsAreAccepted() {
        assertValid(1, "A"); assertValid(1, "E"); assertValid(2, "E");
        assertValid(3, "A"); assertValid(3, "D"); assertValid(4, "D");
        assertValid(5, "E"); assertValid(6, "E"); assertValid(7, "SE");
        assertValid(7, "CSec"); assertValid(8, "BIS"); assertValid(9, "HPC");
        assertValid(10, "ES");
    }

    @Test void requiredInvalidCombinationsAreRejected() {
        assertFalse(AcademicGroupPolicy.isValid(3, "E"));
        assertFalse(AcademicGroupPolicy.isValid(4, "E"));
        assertFalse(AcademicGroupPolicy.isValid(7, "A"));
        assertFalse(AcademicGroupPolicy.isValid(8, "D"));
        assertFalse(AcademicGroupPolicy.isValid(9, "E"));
        assertFalse(AcademicGroupPolicy.isValid(10, "B"));
    }

    @Test void canonicalNormalizationPreservesMajorDisplayCase() {
        assertEquals("CSec", AcademicGroupPolicy.normalize(10, " csec "));
        assertEquals("B", AcademicGroupPolicy.normalize(3, " b "));
    }

    @Test void discussionTargetsRemainSeparatedBySemesterAndGroup() {
        DiscussionTarget semester3B = DiscussionTarget.fromAuthenticatedUser(1, DiscussionScope.SECTION, 5L, 3, "B");
        DiscussionTarget semester3C = DiscussionTarget.fromAuthenticatedUser(1, DiscussionScope.SECTION, 5L, 3, "C");
        DiscussionTarget semester7Se = DiscussionTarget.fromAuthenticatedUser(1, DiscussionScope.SECTION, 5L, 7, "SE");
        DiscussionTarget semester7Ke = DiscussionTarget.fromAuthenticatedUser(1, DiscussionScope.SECTION, 5L, 7, "KE");
        assertNotEquals(semester3B, semester3C);
        assertNotEquals(semester7Se, semester7Ke);
        assertFalse(DiscussionAccess.matches(DiscussionScope.SECTION, 3, "B", 3, "C"));
        assertFalse(DiscussionAccess.matches(DiscussionScope.SECTION, 7, "SE", 7, "KE"));
    }

    private static void assertValid(int semester, String group) {
        assertTrue(AcademicGroupPolicy.isValid(semester, group), semester + "," + group);
    }
}
