package com.studenthub.util;

public final class AdminMutationPolicy {
    private AdminMutationPolicy() {}
    public static boolean canProceed(Object role, boolean csrfValid) {
        return Authorization.isAdmin(role) && csrfValid;
    }
}
