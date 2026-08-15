package com.studenthub.util;

public final class FeedRedirect {
    private FeedRedirect() {}
    public static String path(String source, Long categoryId) {
        String base = "home".equalsIgnoreCase(source) ? "/home" : "/announcements";
        return categoryId == null ? base : base + "?category=" + categoryId;
    }
    public static Long positiveId(String value) {
        try { long id = Long.parseLong(value); return id > 0 ? id : null; }
        catch (RuntimeException exception) { return null; }
    }
}
