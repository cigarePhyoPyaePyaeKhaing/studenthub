package com.studenthub.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class AttachmentStorageTest {
    @TempDir Path temporaryDirectory;

    @Test void unavailableStorageRejectsWriteWithoutMetadata() {
        AttachmentStorage storage = new AttachmentStorage(null);
        assertFalse(storage.isConfigured());
        assertFalse(storage.ensureWritable());
        assertThrows(IOException.class, () -> storage.save(validated("photo.jpg", "image/jpeg", "jpg")));
    }

    @Test void savesFindsAndDeletesValidatedMedia() throws Exception {
        AttachmentStorage storage = new AttachmentStorage(temporaryDirectory.resolve("attachments"));
        assertTrue(storage.ensureWritable());
        AttachmentUpload upload = storage.save(validated("photo.jpg", "image/jpeg", "jpg"));
        assertTrue(storage.find(upload.storageKey()).isPresent());
        storage.delete(upload.storageKey());
        assertTrue(storage.find(upload.storageKey()).isEmpty());
    }

    @Test void configuredNonDirectoryIsReportedUnwritable() throws Exception {
        Path file = temporaryDirectory.resolve("occupied");
        Files.writeString(file, "x");
        AttachmentStorage storage = new AttachmentStorage(file);
        assertTrue(storage.isConfigured());
        assertFalse(storage.ensureWritable());
    }

    private AttachmentValidator.Validated validated(String name, String mime, String extension) {
        return new AttachmentValidator.Validated(name, mime, extension, new byte[]{1,2,3});
    }
}
