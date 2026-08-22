package com.studenthub.util;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class MyanmarTimeTest {
    @Test void utcIsConvertedAcrossDateBoundary(){assertEquals("Jan 2, 2026 · 5:30 AM",MyanmarTime.formatUtc(LocalDateTime.of(2026,1,1,23,0),"MMM d, yyyy · h:mm a"));}
    @Test void zoneIsAsiaYangon(){assertEquals("Asia/Yangon",MyanmarTime.ZONE.getId());}
}
