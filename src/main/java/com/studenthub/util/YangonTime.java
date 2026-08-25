package com.studenthub.util;

import java.time.*;
import java.time.format.DateTimeFormatter;

public final class YangonTime {
    public static final ZoneId ZONE = ZoneId.of("Asia/Yangon");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("h:mm a");
    private static final DateTimeFormatter OLDER = DateTimeFormatter.ofPattern("MMM d, h:mm a");
    private YangonTime() {}
    public static ZonedDateTime fromUtc(LocalDateTime utc) { return utc.atZone(ZoneOffset.UTC).withZoneSameInstant(ZONE); }
    public static String label(LocalDateTime utc) { return label(utc, Instant.now()); }
    static String label(LocalDateTime utc, Instant now) {
        if (utc == null) return "";
        ZonedDateTime value=fromUtc(utc), current=now.atZone(ZONE);
        if(value.toLocalDate().equals(current.toLocalDate()))return value.format(TIME);
        if(value.toLocalDate().equals(current.toLocalDate().minusDays(1)))return "Yesterday";
        return value.format(OLDER);
    }
    public static String dateGroup(LocalDateTime utc) { return dateGroup(utc, Instant.now()); }
    static String dateGroup(LocalDateTime utc, Instant now) {
        if (utc == null) return "";
        ZonedDateTime value = fromUtc(utc), current = now.atZone(ZONE);
        LocalDate date = value.toLocalDate(), today = current.toLocalDate();
        if (date.equals(today)) return "Today";
        if (date.equals(today.minusDays(1))) return "Yesterday";
        if (date.getYear() == today.getYear()) return date.format(DateTimeFormatter.ofPattern("MMM d"));
        return date.format(DateTimeFormatter.ofPattern("MMM d, yyyy"));
    }
    public static boolean active(LocalDateTime utc) { return utc != null && utc.isAfter(LocalDateTime.now(ZoneOffset.UTC).minusMinutes(3)); }
    public static String presence(LocalDateTime utc) {
        if(utc==null)return "Last seen unavailable";if(active(utc))return "Active now";
        long minutes=Math.max(0,Duration.between(utc,LocalDateTime.now(ZoneOffset.UTC)).toMinutes());
        if(minutes<60)return "Last seen "+minutes+" min ago";if(minutes<1440)return "Last seen "+(minutes/60)+"h ago";return "Last seen "+label(utc);
    }
}
