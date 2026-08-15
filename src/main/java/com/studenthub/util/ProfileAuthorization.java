package com.studenthub.util;

public final class ProfileAuthorization {
    private ProfileAuthorization() {}

    public static boolean canAccessOwnProfile(Object authenticatedUserId) {
        return Authorization.isAuthenticatedUserId(authenticatedUserId);
    }

    public static long updateTarget(long authenticatedUserId) {
        return authenticatedUserId;
    }

    public static boolean canSubmitUpdate(boolean csrfValid) {
        return csrfValid;
    }
}
