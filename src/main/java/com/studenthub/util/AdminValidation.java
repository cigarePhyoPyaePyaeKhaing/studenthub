package com.studenthub.util;

import com.studenthub.model.Role;

public final class AdminValidation {
    public static final int PAGE_SIZE = 20;
    public static final int MAX_SEARCH_LENGTH = 100;
    private AdminValidation() {}
    public static String normalizeSearch(String input) {
        if (input == null || input.trim().isEmpty()) return null;
        String search = input.trim(); return search.length() <= MAX_SEARCH_LENGTH ? search : null;
    }
    public static boolean searchTooLong(String input) {
        return input != null && input.trim().length() > MAX_SEARCH_LENGTH;
    }
    public static int page(String input) {
        try { int page = Integer.parseInt(input); return page > 0 && page <= 1_000_000 ? page : 1; }
        catch (RuntimeException exception) { return 1; }
    }
    public static Role role(String input) {
        try { return input == null ? null : Role.valueOf(input.trim().toUpperCase()); }
        catch (IllegalArgumentException exception) { return null; }
    }
    public static int totalPages(long total) { return (int) Math.max(1, (total + PAGE_SIZE - 1) / PAGE_SIZE); }
}
