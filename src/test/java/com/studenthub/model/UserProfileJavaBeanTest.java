package com.studenthub.model;

import org.junit.jupiter.api.Test;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class UserProfileJavaBeanTest {

    @Test
    void studentProfileExposesExpectedProperties() {
        UserProfile profile = new UserProfile(101L, "UIT-0101", "Student One", "student1@uit.edu",
                Role.STUDENT, true, 3, "A", 1L, "University of Information Technology", "UIT", true, true);

        assertEquals(101L, profile.getUserId());
        assertEquals("UIT-0101", profile.getStudentId());
        assertEquals("Student One", profile.getFullName());
        assertEquals("student1@uit.edu", profile.getEmail());
        assertEquals(Role.STUDENT, profile.getRole());
        assertTrue(profile.isEmailVerified());
        assertTrue(profile.getEmailVerified());
        assertEquals(3, profile.getSemester());
        assertEquals("A", profile.getSectionName());
        assertEquals(1L, profile.getUniversityId());
        assertEquals("University of Information Technology", profile.getUniversityName());
        assertEquals("UIT", profile.getUniversityShortName());
        assertTrue(profile.isUniversityLocked());
        assertTrue(profile.getUniversityLocked());
        assertTrue(profile.isAcademicInfoLocked());
        assertTrue(profile.getAcademicInfoLocked());
        assertEquals("S", profile.getInitial());
    }

    @Test
    void crProfileExposesExpectedProperties() {
        UserProfile crProfile = new UserProfile(202L, "UIT-0202", "CR User", "cr@uit.edu",
                Role.CR, true, 5, "B", 1L, "University of Information Technology", "UIT", true, true);

        assertEquals(202L, crProfile.getUserId());
        assertEquals("UIT-0202", crProfile.getStudentId());
        assertEquals("CR User", crProfile.getFullName());
        assertEquals("cr@uit.edu", crProfile.getEmail());
        assertEquals(Role.CR, crProfile.getRole());
        assertTrue(crProfile.isEmailVerified());
        assertEquals(5, crProfile.getSemester());
        assertEquals("B", crProfile.getSectionName());
    }

    @Test
    void adminProfileExposesExpectedProperties() {
        UserProfile adminProfile = new UserProfile(1L, "ADMIN-01", "System Admin", "admin@uit.edu",
                Role.ADMIN, true, null, null, null, null, null, false, false);

        assertEquals(1L, adminProfile.getUserId());
        assertEquals("ADMIN-01", adminProfile.getStudentId());
        assertEquals("System Admin", adminProfile.getFullName());
        assertEquals("admin@uit.edu", adminProfile.getEmail());
        assertEquals(Role.ADMIN, adminProfile.getRole());
        assertTrue(adminProfile.isEmailVerified());
        assertNull(adminProfile.getSemester());
        assertNull(adminProfile.getSectionName());
        assertNull(adminProfile.getUniversityId());
        assertNull(adminProfile.getUniversityName());
        assertNull(adminProfile.getUniversityShortName());
        assertFalse(adminProfile.isUniversityLocked());
        assertFalse(adminProfile.isAcademicInfoLocked());
    }

    @Test
    void profileWithOptionalUniversity() {
        UserProfile profile = new UserProfile(50L, "ST-50", "Uni Student", "uni@uit.edu",
                Role.STUDENT, true, 2, "C", 2L, "Yangon University", "YU", false, false);

        assertEquals(2L, profile.getUniversityId());
        assertEquals("Yangon University", profile.getUniversityName());
        assertEquals("YU", profile.getUniversityShortName());
        assertFalse(profile.isUniversityLocked());
    }

    @Test
    void profileWithoutOptionalUniversity() {
        UserProfile profile = new UserProfile(60L, "ST-60", "No Uni", "nouni@uit.edu",
                Role.STUDENT, true, 2, "C", null, null, null, false, false);

        assertNull(profile.getUniversityId());
        assertNull(profile.getUniversityName());
        assertNull(profile.getUniversityShortName());
        assertFalse(profile.isUniversityLocked());
    }

    @Test
    void nullOptionalAcademicValuesAreSafe() {
        UserProfile profile = new UserProfile(70L, null, "Null Academic", "null@uit.edu",
                Role.STUDENT, false, null, null);

        assertNull(profile.getStudentId());
        assertNull(profile.getSemester());
        assertNull(profile.getSectionName());
        assertNull(profile.getUniversityId());
        assertNull(profile.getUniversityName());
        assertNull(profile.getUniversityShortName());
        assertFalse(profile.isUniversityLocked());
        assertFalse(profile.isAcademicInfoLocked());
        assertEquals("N", profile.getInitial());
    }

    @Test
    void initialFallbackWhenFullNameEmptyOrBlank() {
        UserProfile emptyName = new UserProfile(80L, "ST-80", "", "test@uit.edu",
                Role.STUDENT, true, null, null);
        assertEquals("S", emptyName.getInitial());

        UserProfile nullName = new UserProfile(81L, "ST-81", null, "test2@uit.edu",
                Role.STUDENT, true, null, null);
        assertEquals("S", nullName.getInitial());
    }

    @Test
    void javaBeanPropertyDescriptorsMatchJspElExpectations() throws Exception {
        PropertyDescriptor[] descriptors = Introspector.getBeanInfo(UserProfile.class).getPropertyDescriptors();
        Set<String> propertyNames = Arrays.stream(descriptors)
                .map(PropertyDescriptor::getName)
                .collect(Collectors.toSet());

        assertTrue(propertyNames.contains("userId"));
        assertTrue(propertyNames.contains("studentId"));
        assertTrue(propertyNames.contains("fullName"));
        assertTrue(propertyNames.contains("email"));
        assertTrue(propertyNames.contains("role"));
        assertTrue(propertyNames.contains("emailVerified"));
        assertTrue(propertyNames.contains("semester"));
        assertTrue(propertyNames.contains("sectionName"));
        assertTrue(propertyNames.contains("universityId"));
        assertTrue(propertyNames.contains("universityName"));
        assertTrue(propertyNames.contains("universityShortName"));
        assertTrue(propertyNames.contains("universityLocked"));
        assertTrue(propertyNames.contains("academicInfoLocked"));
        assertTrue(propertyNames.contains("initial"));
    }
}
