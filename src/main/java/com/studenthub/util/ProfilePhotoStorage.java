package com.studenthub.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

public class ProfilePhotoStorage {
    private static final Pattern SAFE_FILENAME = Pattern.compile("^[a-f0-9-]{36}\\.(jpg|png|webp)$");
    private final Path directory;

    public ProfilePhotoStorage() {
        this(resolveDirectory());
    }

    ProfilePhotoStorage(Path directory) {
        this.directory = directory == null ? null : directory.toAbsolutePath().normalize();
    }

    public String save(byte[] content, String extension) throws IOException {
        if (directory == null) throw new IOException("Persistent profile storage is not configured");
        Files.createDirectories(directory);
        String filename = UUID.randomUUID() + "." + extension;
        Path destination = safePath(filename).orElseThrow(() -> new IOException("Invalid generated filename"));
        Files.write(destination, content, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
        return filename;
    }

    public Optional<Path> find(String filename) {
        return safePath(filename).filter(Files::isRegularFile);
    }

    public void delete(String filename) {
        safePath(filename).ifPresent(path -> {
            try { Files.deleteIfExists(path); } catch (IOException ignored) { }
        });
    }

    public boolean isConfigured() { return directory != null; }

    public boolean ensureWritable() {
        if (directory == null) return false;
        try {
            Files.createDirectories(directory);
            return Files.isDirectory(directory) && Files.isWritable(directory);
        } catch (IOException | SecurityException exception) {
            return false;
        }
    }

    private Optional<Path> safePath(String filename) {
        if (directory == null || filename == null || !SAFE_FILENAME.matcher(filename).matches()) return Optional.empty();
        Path resolved = directory.resolve(filename).normalize();
        return resolved.getParent() != null && resolved.getParent().equals(directory)
                ? Optional.of(resolved) : Optional.empty();
    }

    private static Path resolveDirectory() {
        String configured = System.getenv("STUDENTHUB_UPLOAD_DIR");
        if (configured != null && !configured.isBlank()) {
            return Path.of(configured).resolve("profile");
        }
        String environment = System.getenv("APP_ENV");
        if (environment != null && environment.equalsIgnoreCase("production")) {
            return null;
        }
        return Path.of(System.getProperty("java.io.tmpdir"), "studenthub-uploads", "profile");
    }
}
