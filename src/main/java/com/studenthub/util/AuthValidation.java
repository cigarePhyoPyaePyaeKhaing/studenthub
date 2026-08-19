package com.studenthub.util;

import java.util.Locale;
import java.util.regex.Pattern;

public final class AuthValidation {
    private static final Pattern STUDENT_ID = Pattern.compile("^TNT-\\d{4}$");
    private static final Pattern EMAIL = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern PASSWORD = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$");

    private AuthValidation() {
    }

    public static String normalizeStudentId(String value) {
        String normalized = value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
        if (normalized.matches("^TNT\\d{4}$")) return "TNT-" + normalized.substring(3);
        return normalized;
    }

    public static String normalizeEmail(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    public static boolean isValidStudentId(String value) {
        return STUDENT_ID.matcher(normalizeStudentId(value)).matches();
    }

    public static boolean isValidEmail(String value) {
        String normalized = normalizeEmail(value);
        return normalized.length() <= 120 && EMAIL.matcher(normalized).matches();
    }

    public static boolean isValidPassword(String value) {
        return value != null && value.length() <= 128 && PASSWORD.matcher(value).matches();
    }
}
