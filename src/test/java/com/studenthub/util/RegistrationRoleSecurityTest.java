package com.studenthub.util;

import com.studenthub.model.Role;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RegistrationRoleSecurityTest {
    @Test void publicRegistrationCannotAssignAdmin() { assertEquals(Role.STUDENT,RegistrationPolicy.initialRole()); }
    @Test void publicRegistrationCannotAssignCr() { assertEquals(Role.STUDENT,RegistrationPolicy.initialRole()); }
}
