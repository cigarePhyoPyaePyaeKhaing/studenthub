package com.studenthub.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserProfileAcademicLockTest {

    @Test
    void semester4AndSectionCIsLocked() {
        UserProfile profile = new UserProfile(
                1L, "TNT-0001", "Student Four", "student4@uit.edu",
                Role.STUDENT, true, 4, "C");

        assertTrue(profile.academicInfoLocked());
        assertTrue(profile.isAcademicInfoLocked());
        assertTrue(profile.getAcademicInfoLocked());
    }

    @Test
    void semesterNullAndSectionNullIsNotLocked() {
        UserProfile profile = new UserProfile(
                2L, "TNT-0002", "Unassigned Student", "unassigned@uit.edu",
                Role.STUDENT, true, null, null);

        assertFalse(profile.academicInfoLocked());
        assertFalse(profile.isAcademicInfoLocked());
        assertFalse(profile.getAcademicInfoLocked());
    }

    @Test
    void semester4AndSectionNullIsNotLocked() {
        UserProfile profile = new UserProfile(
                3L, "TNT-0003", "Half Student", "half@uit.edu",
                Role.STUDENT, true, 4, null);

        assertFalse(profile.academicInfoLocked());
        assertFalse(profile.isAcademicInfoLocked());
        assertFalse(profile.getAcademicInfoLocked());
    }

    @Test
    void semesterNullAndSectionCIsNotLocked() {
        UserProfile profile = new UserProfile(
                4L, "TNT-0004", "Half Student 2", "half2@uit.edu",
                Role.STUDENT, true, null, "C");

        assertFalse(profile.academicInfoLocked());
        assertFalse(profile.isAcademicInfoLocked());
        assertFalse(profile.getAcademicInfoLocked());
    }

    @Test
    void blankSectionIsNotLocked() {
        UserProfile profile = new UserProfile(
                5L, "TNT-0005", "Blank Section Student", "blank@uit.edu",
                Role.STUDENT, true, 4, "   ");

        assertFalse(profile.academicInfoLocked());
        assertFalse(profile.isAcademicInfoLocked());
        assertFalse(profile.getAcademicInfoLocked());
    }

    @Test
    void fullConstructorDerivesAcademicInfoLockedCorrectly() {
        UserProfile locked = new UserProfile(
                6L, "TNT-0006", "Full Student", "full@uit.edu",
                Role.STUDENT, true, 2, "A",
                null, null, null, null, null,
                null, null, null, false, false);

        assertTrue(locked.academicInfoLocked());
        assertTrue(locked.isAcademicInfoLocked());
        assertTrue(locked.getAcademicInfoLocked());
    }
}