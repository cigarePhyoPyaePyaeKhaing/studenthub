package com.studenthub.util;

import java.util.Map;
import java.util.Optional;

public final class ProfilePhotoValidator {
    public static final long MAX_BYTES = 2L * 1024L * 1024L;
    private static final Map<String, String> EXTENSIONS = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp");

    private ProfilePhotoValidator() {}

    public static Optional<String> validatedExtension(String contentType, byte[] content) {
        String extension = EXTENSIONS.get(contentType == null ? "" : contentType.toLowerCase());
        if (extension == null || content == null || content.length == 0 || content.length > MAX_BYTES) {
            return Optional.empty();
        }
        boolean signatureMatches = switch (extension) {
            case "jpg" -> content.length >= 3
                    && unsigned(content[0]) == 0xff && unsigned(content[1]) == 0xd8 && unsigned(content[2]) == 0xff;
            case "png" -> content.length >= 8
                    && unsigned(content[0]) == 0x89 && content[1] == 0x50 && content[2] == 0x4e
                    && content[3] == 0x47 && content[4] == 0x0d && content[5] == 0x0a
                    && content[6] == 0x1a && content[7] == 0x0a;
            case "webp" -> content.length >= 12
                    && content[0] == 'R' && content[1] == 'I' && content[2] == 'F' && content[3] == 'F'
                    && content[8] == 'W' && content[9] == 'E' && content[10] == 'B' && content[11] == 'P';
            default -> false;
        };
        return signatureMatches ? Optional.of(extension) : Optional.empty();
    }

    private static int unsigned(byte value) {
        return value & 0xff;
    }
}
