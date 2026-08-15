package com.studenthub.util;

import com.studenthub.model.ProfileUpdate;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.*;

class ProfileSecurityTest {
    @Test void unauthenticatedProfileAccessIsRejected() {
        assertFalse(ProfileAuthorization.canAccessOwnProfile(null));
    }
    @Test void authenticatedUserCanAccessOwnProfile() {
        assertTrue(ProfileAuthorization.canAccessOwnProfile(7L));
    }
    @Test void updateAlwaysTargetsAuthenticatedUser() {
        assertEquals(7L, ProfileAuthorization.updateTarget(7L));
    }
    @Test void csrfIsRequiredForUpdate() {
        assertFalse(ProfileAuthorization.canSubmitUpdate(false));
        assertTrue(ProfileAuthorization.canSubmitUpdate(true));
    }
    @Test void updateCommandContainsOnlyPermittedFields() {
        Set<String> fields = Arrays.stream(ProfileUpdate.class.getRecordComponents())
                .map(component -> component.getName()).collect(Collectors.toSet());
        assertEquals(Set.of("fullName", "semester", "sectionName"), fields);
    }
    @Test void userIdCannotBeSuppliedInUpdateCommand() {
        assertFalse(hasField("userId"));
    }
    @Test void studentIdRoleAndVerificationCannotBeChangedByUpdateCommand() {
        assertFalse(hasField("studentId"));
        assertFalse(hasField("role"));
        assertFalse(hasField("emailVerified"));
        assertFalse(hasField("email"));
    }
    private boolean hasField(String name) {
        return Arrays.stream(ProfileUpdate.class.getRecordComponents())
                .anyMatch(component -> component.getName().equals(name));
    }
}
