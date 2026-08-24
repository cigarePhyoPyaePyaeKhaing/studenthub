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
}
