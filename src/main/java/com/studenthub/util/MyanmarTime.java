package com.studenthub.util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class MyanmarTime {
    public static final ZoneId ZONE = ZoneId.of("Asia/Yangon");
    private MyanmarTime() {}
    public static String formatUtc(LocalDateTime utc, String pattern) {
        if (utc == null) return "";
        return utc.atZone(ZoneOffset.UTC).withZoneSameInstant(ZONE)
                .format(DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH));
    }
    public static String formatLocal(LocalDateTime local, String pattern) {
        return local == null ? "" : local.format(DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH));
    }
    public static LocalDateTime now() { return LocalDateTime.now(ZONE); }
}
