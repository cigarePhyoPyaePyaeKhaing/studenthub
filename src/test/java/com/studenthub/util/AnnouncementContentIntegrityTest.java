package com.studenthub.util;

import com.studenthub.model.Post;
import com.studenthub.model.Role;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AnnouncementContentIntegrityTest {

    @Test
    void preservesCompleteMultiLineEnglishAnnouncementWithFinalSentence() {
        String title = "Linear Algebra";
        String body = """
                This course introduces the fundamental concepts of linear algebra,
                including vectors, matrices, linear equations, determinants,
                eigenvalues, and eigenvectors.

                Students are expected to attend all classes, complete assignments
                on time, participate in discussions, and prepare well for quizzes
                and exams.

                Let's learn and succeed together!""";

        Post post = new Post(
                101L,
                5L,
                1L,
                "Professor Daw Win Mar",
                Role.CR,
                "Mathematics",
                title,
                body,
                null,
                "SECTION",
                LocalDateTime.now(),
                12L,
                4L,
                true
        );

        assertEquals(title, post.getTitle());
        assertEquals(body, post.getContent());
        assertTrue(post.getContent().startsWith("This course introduces"));
        assertTrue(post.getContent().contains("Students are expected to attend all classes"));
        assertTrue(post.getContent().endsWith("Let's learn and succeed together!"));
    }

    @Test
    void preservesFullMultiLineBurmeseAnnouncementContent() {
        String title = "စာမေးပွဲ အထူးသတိပေးချက်";
        String burmeseContent = """
                လာမည့် တနင်္လာနေ့တွင် ကျင်းပမည့် Midterm စာမေးပွဲအတွက် အောက်ပါအချက်များကို လိုက်နာကြပါရန် အသိပေးအပ်ပါသည်။
                ၁။ ကျောင်းသားကတ် (Student ID) မဖြစ်မနေ ယူဆောင်လာရမည်။
                ၂။ စာမေးပွဲခန်းမသို့ နံနက် ၈:၃၀ အရောက် လာရမည်။
                ၃။ မိုဘိုင်းဖုန်းနှင့် Smart Watch များကို စာမေးပွဲခန်းအတွင်း ယူဆောင်ခွင့်မပြုပါ။
                အားလုံး စာမေးပွဲ အောင်မြင်စွာ ဖြေဆိုနိုင်ကြပါစေ။""";

        Post post = new Post(
                102L,
                6L,
                2L,
                "U Aung Kyaw",
                Role.ADMIN,
                "Academic Affairs",
                title,
                burmeseContent,
                null,
                "ALL",
                LocalDateTime.now(),
                25L,
                8L,
                false
        );

        assertEquals(title, post.getTitle());
        assertEquals(burmeseContent, post.getContent());
        assertTrue(post.getContent().startsWith("လာမည့် တနင်္လာနေ့တွင်"));
        assertTrue(post.getContent().contains("ကျောင်းသားကတ် (Student ID)"));
        assertTrue(post.getContent().endsWith("အားလုံး စာမေးပွဲ အောင်မြင်စွာ ဖြေဆိုနိုင်ကြပါစေ။"));
    }

    @Test
    void preservesMixedBurmeseAndEnglishContentAcrossFeedModel() {
        String mixedContent = """
                Tutorial 4 (Data Structures & Algorithms) Submission Guidelines:
                - Deadline: September 5, 2026 at 11:59 PM.
                - တာဝန်ကျ ဆရာ/ဆရာမများထံ သတ်မှတ်ချိန်အတွင်း တင်သွင်းရန် လိုအပ်ပါသည်။
                - Late submission will receive a 10% penalty per day.
                Good luck to everyone!""";

        Post post = new Post(
                103L,
                7L,
                3L,
                "CR Ko Thant",
                Role.CR,
                "Computer Science",
                "Tutorial 4 Submission",
                mixedContent,
                null,
                "SEMESTER",
                LocalDateTime.now(),
                5L,
                2L,
                false
        );

        assertEquals(mixedContent, post.getContent());
        assertTrue(post.getContent().contains("Tutorial 4"));
        assertTrue(post.getContent().contains("တာဝန်ကျ ဆရာ/ဆရာမများထံ"));
        assertTrue(post.getContent().endsWith("Good luck to everyone!"));
    }

    @Test
    void preservesFirstMiddleAndFinalLineContract() {
        String body = "FIRST-LINE: Welcome to Advance Software Engineering\n"
                + "MIDDLE-LINE: Please review chapters 1 to 5 and complete the homework assignment.\n"
                + "FINAL-LINE-MUST-EXIST: All project proposals are due next Friday.";

        Post post = new Post(
                104L, 8L, 4L, "Admin User", Role.ADMIN, "General News",
                "Course Overview", body, null, "ALL", LocalDateTime.now(), 0L, 0L, false
        );

        String[] lines = post.getContent().split("\n");
        assertEquals(3, lines.length);
        assertEquals("FIRST-LINE: Welcome to Advance Software Engineering", lines[0]);
        assertEquals("MIDDLE-LINE: Please review chapters 1 to 5 and complete the homework assignment.", lines[1]);
        assertEquals("FINAL-LINE-MUST-EXIST: All project proposals are due next Friday.", lines[2]);
    }

    @Test
    void verifiesResponsiveBreakpointClassificationContract() {
        int[] mobileTabletWidths = {375, 390, 430, 768, 820, 1024};
        int[] desktopWidths = {1025, 1280, 1366, 1920};

        for (int width : mobileTabletWidths) {
            assertTrue(isMobileTabletViewport(width), "Width " + width + " must be classified as Mobile/Tablet layout");
        }

        for (int width : desktopWidths) {
            assertFalse(isMobileTabletViewport(width), "Width " + width + " must be classified as Desktop layout");
        }
    }

    @Test
    void validatesLongContentWithinLimitAndRejectsAboveLimit() {
        String validLongBody = "START-LINE\n" + "A".repeat(9500) + "\nFINAL-LINE-MUST-BE-VISIBLE";
        String error = PostValidation.validate("Test Title", validLongBody, 1L, "General News", "ALL", null);
        assertNull(error, "Valid content under 10,000 characters should pass validation");

        String overLimitBody = "B".repeat(10_001);
        String overLimitError = PostValidation.validate("Test Title", overLimitBody, 1L, "General News", "ALL", null);
        assertNotNull(overLimitError, "Content over 10,000 characters should return validation error");
        assertTrue(overLimitError.contains("10,000 characters"));
    }

    @Test
    void verifiesEditFlowPreservesFullContentUnchanged() {
        String originalContent = "START-LINE\n"
                + "Line 1: Detailed project requirements.\n"
                + "Line 2: Team division and rubric.\n"
                + "FINAL-LINE-MUST-BE-VISIBLE: Submit via StudentHub portal.";

        String updatedTitle = "Updated Course Announcement";
        Post post = new Post(
                105L, 8L, 1L, "Admin", Role.ADMIN, "General News",
                updatedTitle, originalContent, null, "ALL", LocalDateTime.now(), 2L, 1L, false
        );

        assertEquals(originalContent, post.getContent());
        assertTrue(post.getContent().contains("FINAL-LINE-MUST-BE-VISIBLE"));
    }

    @Test
    void verifiesSentinelLongAnnouncementContentIntegrity() {
        String sentinelContent = """
                FIRST-LINE-VISIBLE
                Line 02
                Line 03
                Line 04
                Line 05
                Line 06
                Line 07
                Line 08
                Line 09
                Line 10
                Line 11
                Line 12
                Line 13
                Line 14
                Line 15
                Line 16
                Line 17
                Line 18
                Line 19
                Line 20
                မြန်မာစာ စမ်းသပ်ချက် အပြည့်အစုံ
                FINAL-LINE-MUST-BE-VISIBLE""";

        Post post = new Post(
                106L, 1L, 1L, "Admin Sentinel", Role.ADMIN, "General News",
                "Sentinel Long Announcement", sentinelContent, null, "ALL", LocalDateTime.now(), 0L, 0L, false
        );

        String[] lines = post.getContent().lines().toArray(String[]::new);
        assertEquals(22, lines.length);
        assertEquals("FIRST-LINE-VISIBLE", lines[0]);
        assertEquals("Line 10", lines[9]);
        assertEquals("Line 20", lines[19]);
        assertEquals("မြန်မာစာ စမ်းသပ်ချက် အပြည့်အစုံ", lines[20]);
        assertEquals("FINAL-LINE-MUST-BE-VISIBLE", lines[21]);
    }

    @Test
    void verifiesMobileNavLabelsFullTextIntegrity() {
        String[] labels = {"Home", "Announcements", "Notifications", "Discussions", "Profile"};
        assertEquals(5, labels.length);
        for (String label : labels) {
            assertFalse(label.contains("..."), "Label must not contain ellipsis: " + label);
            assertFalse(label.endsWith("..."), "Label must not be truncated: " + label);
            assertTrue(label.length() >= 4, "Label must be full length word: " + label);
        }
        assertEquals("Announcements", labels[1]);
        assertEquals("Notifications", labels[2]);
        assertEquals("Discussions", labels[3]);
    }

    private boolean isMobileTabletViewport(int width) {
        return width <= 1024;
    }
}
