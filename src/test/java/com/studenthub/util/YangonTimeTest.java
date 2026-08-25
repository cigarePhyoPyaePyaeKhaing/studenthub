package com.studenthub.util;
import org.junit.jupiter.api.Test;import java.time.*;import static org.junit.jupiter.api.Assertions.*;
class YangonTimeTest{
 @Test void convertsUtcToYangon(){assertEquals(LocalDateTime.of(2026,8,24,16,35),YangonTime.fromUtc(LocalDateTime.of(2026,8,24,10,5)).toLocalDateTime());}
 @Test void todayUsesYangonClock(){Instant now=Instant.parse("2026-08-24T11:00:00Z");assertEquals("4:35 PM",YangonTime.label(LocalDateTime.of(2026,8,24,10,5),now));}
 @Test void yesterdayUsesFriendlyLabel(){Instant now=Instant.parse("2026-08-24T12:00:00Z");assertEquals("Yesterday",YangonTime.label(LocalDateTime.of(2026,8,23,12,0),now));}
 @Test void dateGroupFormatting(){Instant now=Instant.parse("2026-08-24T12:00:00Z");assertEquals("Today",YangonTime.dateGroup(LocalDateTime.of(2026,8,24,10,0),now));assertEquals("Yesterday",YangonTime.dateGroup(LocalDateTime.of(2026,8,23,10,0),now));assertEquals("Aug 20",YangonTime.dateGroup(LocalDateTime.of(2026,8,20,10,0),now));}
}
