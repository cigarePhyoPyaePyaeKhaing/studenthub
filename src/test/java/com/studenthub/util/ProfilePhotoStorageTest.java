package com.studenthub.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class ProfilePhotoStorageTest {
    @TempDir Path temporaryDirectory;

    @Test void unavailableStorageFailsWithoutCreatingDatabaseReference() {
        ProfilePhotoStorage storage = new ProfilePhotoStorage(null);
        assertFalse(storage.isConfigured());
        assertFalse(storage.ensureWritable());
        assertThrows(IOException.class, () -> storage.save(new byte[]{1,2,3}, "jpg"));
    }

    @Test void savesFindsAndDeletesGeneratedPhotoInConfiguredDirectory() throws Exception {
        ProfilePhotoStorage storage = new ProfilePhotoStorage(temporaryDirectory.resolve("profile"));
        assertTrue(storage.isConfigured());
        assertTrue(storage.ensureWritable());
        String key = storage.save(new byte[]{1,2,3}, "png");
        Path saved = storage.find(key).orElseThrow();
        assertArrayEquals(new byte[]{1,2,3}, Files.readAllBytes(saved));
        storage.delete(key);
        assertTrue(storage.find(key).isEmpty());
    }

    @Test void rejectsInvalidOrMissingStorageKeys() {
        ProfilePhotoStorage storage = new ProfilePhotoStorage(temporaryDirectory);
        assertTrue(storage.find("../../secret.jpg").isEmpty());
        assertTrue(storage.find("missing.jpg").isEmpty());
    }
}
