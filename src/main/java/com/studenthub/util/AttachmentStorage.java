package com.studenthub.util;

import java.io.IOException;
import java.nio.file.*;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

public final class AttachmentStorage {
    private static final Pattern SAFE = Pattern.compile("^[a-f0-9-]{36}\\.[a-z0-9]{2,5}$");
    private final Path directory;
    public AttachmentStorage() {
        String configured = System.getenv("STUDENTHUB_UPLOAD_DIR");
        Path root = configured == null || configured.isBlank()
                ? Path.of(System.getProperty("java.io.tmpdir"), "studenthub-uploads") : Path.of(configured);
        directory = root.resolve("attachments").toAbsolutePath().normalize();
    }
    public String save(AttachmentValidator.Upload upload) throws IOException {
        Files.createDirectories(directory);
        String stored = UUID.randomUUID() + "." + upload.extension();
        Path path = safe(stored).orElseThrow();
        Files.write(path, upload.content(), StandardOpenOption.CREATE_NEW);
        return stored;
    }
    public Optional<Path> find(String stored) { return safe(stored).filter(Files::isRegularFile); }
    public void delete(String stored) { safe(stored).ifPresent(p -> { try { Files.deleteIfExists(p); } catch (IOException ignored) {} }); }
    private Optional<Path> safe(String stored) {
        if (stored == null || !SAFE.matcher(stored).matches()) return Optional.empty();
        Path resolved = directory.resolve(stored).normalize();
        return directory.equals(resolved.getParent()) ? Optional.of(resolved) : Optional.empty();
    }
}
