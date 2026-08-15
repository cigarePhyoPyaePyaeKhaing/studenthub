package com.studenthub.util;

import com.studenthub.model.AdminUserSummary;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.*;

class AdminDataSafetyTest {
    @Test void userListingModelContainsOnlySafeFields() {
        Set<String> names=Arrays.stream(AdminUserSummary.class.getRecordComponents()).map(c->c.getName()).collect(Collectors.toSet());
        assertTrue(names.containsAll(Set.of("studentId","fullName","email","role","semester","sectionName","emailVerified","createdAt")));
        assertFalse(names.contains("passwordHash"));assertFalse(names.contains("googleSub"));assertFalse(names.contains("otp"));
    }
    @Test void invalidAndMissingIdsHandledSafely() {
        assertNull(AdminRequest.positiveId(null));assertNull(AdminRequest.positiveId("bad"));assertNull(AdminRequest.positiveId("0"));assertEquals(5,AdminRequest.positiveId("5"));
    }
}
