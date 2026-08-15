package com.studenthub.util;

import com.studenthub.model.Role;

public final class RegistrationPolicy {
    private RegistrationPolicy() {}
    public static Role initialRole() { return Role.STUDENT; }
}
