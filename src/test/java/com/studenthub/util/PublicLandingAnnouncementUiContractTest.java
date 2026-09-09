package com.studenthub.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class PublicLandingAnnouncementUiContractTest {
    private static String source(String path) throws Exception { return Files.readString(Path.of(path)); }

    @Test void publicNavigationAndHomeContainNoRemovedDemoContent() throws Exception {
        String header = source("src/main/webapp/WEB-INF/views/public/partials/header.jsp");
        String footer = source("src/main/webapp/WEB-INF/views/public/partials/footer.jsp");
        String home = source("src/main/webapp/WEB-INF/views/public/home.jsp");
        assertFalse(header.contains(">Features</a>"));
        assertFalse(header.contains(">About</a>"));
        assertFalse(footer.contains(">Features</a>"));
        assertFalse(footer.contains(">About</a>"));
        for (String demo : new String[]{"Database Assignment", "Normalization Exercises", "Linear Algebra Exam", "Lecture Material uploaded", "Semester 4", "Section B", "Campus Pulse academic preview"}) {
            assertFalse(home.contains(demo));
        }
        assertTrue(home.contains("class=\"hero-visual\""));
        assertTrue(home.contains("/register"));
        assertTrue(home.contains("/login"));
    }

    @Test void announcementPickerReflectsSupportedBackendAndProvidesAccessibleState() throws Exception {
        String jsp = source("src/main/webapp/WEB-INF/views/posts/create.jsp");
        String script = source("src/main/webapp/assets/js/announcement-form.js");
        String validator = source("src/main/java/com/studenthub/util/AttachmentValidator.java");
        assertTrue(jsp.contains("data-upload-input"));
        assertTrue(jsp.contains("data-upload-filename"));
        assertTrue(jsp.contains("data-upload-remove"));
        assertTrue(jsp.contains("aria-live=\"polite\""));
        assertTrue(jsp.contains("audio/mpeg"));
        assertTrue(validator.contains("Map.entry(\"audio/mpeg\",Set.of(\"mp3\"))"));
        assertTrue(script.contains("form.dataset.submitting"));
        assertTrue(script.contains("file.size>limit*1024*1024"));
        assertTrue(script.contains("knownExtension"));
    }
}
