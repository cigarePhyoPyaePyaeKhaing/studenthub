package com.studenthub.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProfilePhotoValidatorTest {
    @Test void acceptsMatchingJpegPngAndWebpSignatures() {
        assertEquals("jpg", ProfilePhotoValidator.validatedExtension("image/jpeg",
                new byte[]{(byte) 0xff, (byte) 0xd8, (byte) 0xff, 0}).orElseThrow());
        assertEquals("png", ProfilePhotoValidator.validatedExtension("image/png",
                new byte[]{(byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a}).orElseThrow());
        assertEquals("webp", ProfilePhotoValidator.validatedExtension("image/webp",
                new byte[]{'R','I','F','F',0,0,0,0,'W','E','B','P'}).orElseThrow());
    }

    @Test void rejectsSvgWrongSignaturesAndUnsupportedTypes() {
        assertTrue(ProfilePhotoValidator.validatedExtension("image/svg+xml", "<svg/>".getBytes()).isEmpty());
        assertTrue(ProfilePhotoValidator.validatedExtension("image/jpeg", "not an image".getBytes()).isEmpty());
        assertTrue(ProfilePhotoValidator.validatedExtension("application/octet-stream",
                new byte[]{(byte) 0xff, (byte) 0xd8, (byte) 0xff}).isEmpty());
    }

    @Test void rejectsEmptyAndOversizedFiles() {
        assertTrue(ProfilePhotoValidator.validatedExtension("image/png", new byte[0]).isEmpty());
        assertTrue(ProfilePhotoValidator.validatedExtension("image/png",
                new byte[(int) ProfilePhotoValidator.MAX_BYTES + 1]).isEmpty());
    }
}
