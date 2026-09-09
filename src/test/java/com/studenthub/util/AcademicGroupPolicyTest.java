package com.studenthub.util;

import static org.junit.jupiter.api.Assertions.*;

import com.studenthub.model.DiscussionScope;
import java.util.List;
import org.junit.jupiter.api.Test;

class AcademicGroupPolicyTest {
    @Test void exactSemesterMappingsAreStable() {
        for (int semester = 1; semester <= 7; semester++) {
            assertEquals(List.of("SE", "KE", "BIS", "HPC", "CN", "CSec", "ES"),
                    AcademicGroupPolicy.optionsFor(semester));
            assertEquals("Major", AcademicGroupPolicy.groupLabel(semester));
        }
        for (int semester = 8; semester <= 10; semester++) {
            assertEquals(List.of("A", "B", "C", "D", "E"), AcademicGroupPolicy.optionsFor(semester));
            assertEquals("Section", AcademicGroupPolicy.groupLabel(semester));
        }
        assertEquals(64, AcademicGroupPolicy.allOptions().size());
    }

    @Test void requiredValidCombinationsAreAccepted() {
        assertValid(1, "SE"); assertValid(2, "KE"); assertValid(3, "BIS");
        assertValid(4, "HPC"); assertValid(5, "CN"); assertValid(6, "CSec");
        assertValid(7, "ES"); assertValid(8, "A"); assertValid(8, "E");
        assertValid(9, "C"); assertValid(10, "E");
    }

    @Test void requiredInvalidCombinationsAreRejected() {
        assertFalse(AcademicGroupPolicy.isValid(3, "E"));
        assertFalse(AcademicGroupPolicy.isValid(6, "F"));
        assertFalse(AcademicGroupPolicy.isValid(7, "F"));
        assertFalse(AcademicGroupPolicy.isValid(8, "BIS"));
        assertFalse(AcademicGroupPolicy.isValid(9, "HPC"));
        assertFalse(AcademicGroupPolicy.isValid(10, "ES"));
    }

    @Test void canonicalNormalizationPreservesAcademicGroupDisplayCase() {
        assertEquals("CSec", AcademicGroupPolicy.normalize(7, " csec "));
        assertEquals("E", AcademicGroupPolicy.normalize(10, " e "));
        assertEquals("B", AcademicGroupPolicy.normalize(3, " b "));
    }

    @Test void discussionTargetsRemainSeparatedBySemesterAndGroup() {
        DiscussionTarget semester3Se = DiscussionTarget.fromAuthenticatedUser(1, DiscussionScope.SECTION, 5L, 3, "SE");
        DiscussionTarget semester3Ke = DiscussionTarget.fromAuthenticatedUser(1, DiscussionScope.SECTION, 5L, 3, "KE");
        DiscussionTarget semester7Se = DiscussionTarget.fromAuthenticatedUser(1, DiscussionScope.SECTION, 5L, 7, "SE");
        DiscussionTarget semester7Ke = DiscussionTarget.fromAuthenticatedUser(1, DiscussionScope.SECTION, 5L, 7, "KE");
        assertNotEquals(semester3Se, semester3Ke);
        assertNotEquals(semester7Se, semester7Ke);
        assertFalse(DiscussionAccess.matches(DiscussionScope.SECTION, 3, "SE", 3, "KE"));
        assertFalse(DiscussionAccess.matches(DiscussionScope.SECTION, 7, "SE", 7, "KE"));
    }

    private static void assertValid(int semester, String group) {
        assertTrue(AcademicGroupPolicy.isValid(semester, group), semester + "," + group);
    }
}
