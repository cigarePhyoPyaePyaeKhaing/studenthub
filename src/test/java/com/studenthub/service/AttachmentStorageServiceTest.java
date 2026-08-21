package com.studenthub.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class AttachmentStorageServiceTest {

    @TempDir
    Path tempDir;

    private AttachmentStorageService service;

    @BeforeEach
    void setUp() {
        service = new AttachmentStorageService(tempDir);
    }

    @Test
    void testValidateAllowsImages() {
        AttachmentStorageService.ValidationStatus status =
                service.validate("photo.png", "image/png", 5 * 1024 * 1024);
        assertTrue(status.isValid());
        assertEquals("IMAGE", status.fileType());
        assertEquals("image/png", status.detectedMime());
    }

    @Test
    void testValidateAllowsDocuments() {
        AttachmentStorageService.ValidationStatus status =
                service.validate("assignment.pdf", "application/pdf", 10 * 1024 * 1024);
        assertTrue(status.isValid());
        assertEquals("DOCUMENT", status.fileType());
        assertEquals("application/pdf", status.detectedMime());
    }

    @Test
    void testValidateAllowsVideos() {
        AttachmentStorageService.ValidationStatus status =
                service.validate("demo.mp4", "video/mp4", 30 * 1024 * 1024);
        assertTrue(status.isValid());
        assertEquals("VIDEO", status.fileType());
        assertEquals("video/mp4", status.detectedMime());
    }

    @Test
    void testValidateBlocksExecutablesAndScripts() {
        String[] dangerous = {"shell.jsp", "trojan.exe", "script.sh", "run.bat", "app.py", "eval.php", "hack.war"};
        for (String file : dangerous) {
            AttachmentStorageService.ValidationStatus status =
                    service.validate(file, "application/octet-stream", 1024);
            assertFalse(status.isValid(), "Should block " + file);
            assertEquals(AttachmentStorageService.ValidationResult.DISALLOWED_EXTENSION, status.result());
        }
    }

    @Test
    void testValidateEnforcesSizeLimits() {
        // Image limit is 15MB
        AttachmentStorageService.ValidationStatus largeImg =
                service.validate("huge.jpg", "image/jpeg", 20 * 1024 * 1024);
        assertFalse(largeImg.isValid());
        assertEquals(AttachmentStorageService.ValidationResult.FILE_TOO_LARGE, largeImg.result());

        // Doc limit is 25MB
        AttachmentStorageService.ValidationStatus largeDoc =
                service.validate("huge.pdf", "application/pdf", 30 * 1024 * 1024);
        assertFalse(largeDoc.isValid());
        assertEquals(AttachmentStorageService.ValidationResult.FILE_TOO_LARGE, largeDoc.result());

        // Video limit is 50MB
        AttachmentStorageService.ValidationStatus largeVid =
                service.validate("huge.mp4", "video/mp4", 60 * 1024 * 1024);
        assertFalse(largeVid.isValid());
        assertEquals(AttachmentStorageService.ValidationResult.FILE_TOO_LARGE, largeVid.result());
    }

    @Test
    void testStoreAndResolveFile() throws IOException {
        String content = "Hello StudentHub attachment storage";
        ByteArrayInputStream in = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));

        AttachmentStorageService.StoredFileInfo stored =
                service.store(in, "lecture_notes.pdf", "application/pdf", content.length());

        assertNotNull(stored);
        assertEquals("lecture_notes.pdf", stored.originalFilename());
        assertTrue(stored.storedFilename().endsWith(".pdf"));
        assertEquals("DOCUMENT", stored.fileType());

        File resolved = service.resolveFile(stored.storedFilename());
        assertNotNull(resolved);
        assertTrue(resolved.exists());
        assertEquals(content.length(), resolved.length());

        assertTrue(service.deleteFile(stored.storedFilename()));
        assertNull(service.resolveFile(stored.storedFilename()));
    }

    @Test
    void testPathTraversalPrevention() {
        assertThrows(SecurityException.class, () -> {
            service.resolveFile("../../../etc/passwd");
        });
        assertThrows(SecurityException.class, () -> {
            service.resolveFile("..\\..\\Windows\\System32\\cmd.exe");
        });
    }
}
