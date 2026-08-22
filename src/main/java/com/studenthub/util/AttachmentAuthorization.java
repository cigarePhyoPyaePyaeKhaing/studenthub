package com.studenthub.util;

import java.util.regex.Pattern;

/** Central policy applied before any uploaded attachment path is resolved. */
public final class AttachmentAuthorization {
    private static final Pattern STORED_NAME = Pattern.compile("^[a-f0-9-]{36}\\.[a-z0-9]{2,5}$");
    private AttachmentAuthorization() {}
    public static boolean canServe(boolean authenticated, String storedName) {
        return authenticated && storedName != null && STORED_NAME.matcher(storedName).matches();
    }
}
