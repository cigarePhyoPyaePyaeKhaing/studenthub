package com.studenthub.util;

import com.studenthub.model.Role;
import com.studenthub.model.UserProfile;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProfileSessionTest {
    @Test void nullAcademicValuesAreHandled() {
        ProfileSession.Values values = ProfileSession.values(profile("Mya", null, null));
        assertNull(values.semester());
        assertNull(values.sectionName());
    }
    @Test void updatedSessionValuesComeFromReloadedProfile() {
        ProfileSession.Values values = ProfileSession.values(profile("Updated Name", 4, "B"));
        assertEquals("Updated Name", values.fullName());
        assertEquals(4, values.semester());
        assertEquals("B", values.sectionName());
    }
    @Test void profileInitialHandlesActualAndMissingNames() {
        assertEquals("M", profile("Mya", 4, "B").getInitial());
        assertEquals("S", profile("", null, null).getInitial());
    }
    private UserProfile profile(String name, Integer semester, String section) {
        return new UserProfile(7, "TNT-0007", name, "student@example.edu",
                Role.STUDENT, true, semester, section);
    }
}
