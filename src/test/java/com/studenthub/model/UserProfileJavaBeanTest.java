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
        UserProfile profile = new UserProfile(
                101L, "TNT-0101", "Student One", "student1@uit.edu",
                Role.STUDENT, true, 3, "A", "Computer Science",
                "+95912345678", "Yangon, Myanmar", "Software engineering student",
                "https://example.com/avatar.png", 1L, "University of Information Technology", "UIT", true, true);

        assertEquals(101L, profile.getUserId());
        assertEquals(101L, profile.getId());
        assertEquals("TNT-0101", profile.getStudentId());
        assertEquals("Student One", profile.getFullName());
        assertEquals("student1@uit.edu", profile.getEmail());
        assertEquals(Role.STUDENT, profile.getRole());
        assertTrue(profile.isEmailVerified());
        assertTrue(profile.getEmailVerified());
        assertEquals(3, profile.getSemester());
        assertEquals("A", profile.getSectionName());
        assertEquals("A", profile.getSection());
        assertEquals("Computer Science", profile.getMajor());
        assertEquals("+95912345678", profile.getPhone());
        assertEquals("Yangon, Myanmar", profile.getAddress());
        assertEquals("Software engineering student", profile.getBio());
        assertEquals("https://example.com/avatar.png", profile.getAvatarUrl());
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
        UserProfile crProfile = new UserProfile(202L, "TNT-0202", "CR User", "cr@uit.edu",
                Role.CR, true, 5, "B", 1L, "University of Information Technology", "UIT", true, true);

        assertEquals(202L, crProfile.getUserId());
        assertEquals("TNT-0202", crProfile.getStudentId());
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
    void nullOptionalValuesAreSafe() {
        UserProfile profile = new UserProfile(70L, null, "Null Academic", "null@uit.edu",
                Role.STUDENT, false, null, null);

        assertNull(profile.getStudentId());
        assertNull(profile.getSemester());
        assertNull(profile.getSectionName());
        assertNull(profile.getSection());
        assertNull(profile.getMajor());
        assertNull(profile.getPhone());
        assertNull(profile.getAddress());
        assertNull(profile.getBio());
        assertNull(profile.getAvatarUrl());
        assertNull(profile.getUniversityId());
        assertNull(profile.getUniversityName());
        assertNull(profile.getUniversityShortName());
        assertFalse(profile.isUniversityLocked());
        assertFalse(profile.isAcademicInfoLocked());
        assertEquals("N", profile.getInitial());
    }

    @Test
    void initialFallbackWhenFullNameEmptyOrBlank() {
        UserProfile emptyName = new UserProfile(80L, "TNT-80", "", "test@uit.edu",
                Role.STUDENT, true, null, null);
        assertEquals("S", emptyName.getInitial());

        UserProfile nullName = new UserProfile(81L, "TNT-81", null, "test2@uit.edu",
                Role.STUDENT, true, null, null);
        assertEquals("S", nullName.getInitial());

        UserProfile lowerCaseName = new UserProfile(82L, "TNT-82", "alice", "test3@uit.edu",
                Role.STUDENT, true, null, null);
        assertEquals("A", lowerCaseName.getInitial());
    }

    @Test
    void javaBeanPropertyDescriptorsMatchJspElExpectations() throws Exception {
        PropertyDescriptor[] descriptors = Introspector.getBeanInfo(UserProfile.class).getPropertyDescriptors();
        Set<String> propertyNames = Arrays.stream(descriptors)
                .map(PropertyDescriptor::getName)
                .collect(Collectors.toSet());

        assertTrue(propertyNames.contains("userId"));
        assertTrue(propertyNames.contains("id"));
        assertTrue(propertyNames.contains("studentId"));
        assertTrue(propertyNames.contains("fullName"));
        assertTrue(propertyNames.contains("email"));
        assertTrue(propertyNames.contains("role"));
        assertTrue(propertyNames.contains("emailVerified"));
        assertTrue(propertyNames.contains("semester"));
        assertTrue(propertyNames.contains("sectionName"));
        assertTrue(propertyNames.contains("section"));
        assertTrue(propertyNames.contains("major"));
        assertTrue(propertyNames.contains("phone"));
        assertTrue(propertyNames.contains("address"));
        assertTrue(propertyNames.contains("bio"));
        assertTrue(propertyNames.contains("avatarUrl"));
        assertTrue(propertyNames.contains("universityId"));
        assertTrue(propertyNames.contains("universityName"));
        assertTrue(propertyNames.contains("universityShortName"));
        assertTrue(propertyNames.contains("universityLocked"));
        assertTrue(propertyNames.contains("academicInfoLocked"));
        assertTrue(propertyNames.contains("initial"));
    }
}
