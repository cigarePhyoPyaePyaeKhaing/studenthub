package com.studenthub.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class AttachmentStorageService {

    public record StoredFileInfo(
            String originalFilename,
            String storedFilename,
            String fileType,
            String mimeType,
            long fileSize) {
    }

    public enum ValidationResult {
        VALID, EMPTY_FILE, FILE_TOO_LARGE, DISALLOWED_EXTENSION, UNKNOWN_TYPE
    }

    public record ValidationStatus(ValidationResult result, String fileType, String detectedMime, String message) {
        public boolean isValid() {
            return result == ValidationResult.VALID;
        }
    }

    private static final Set<String> BLOCKED_EXTENSIONS = Set.of(
            "jsp", "jspx", "exe", "sh", "bat", "cmd", "war", "class", "jar",
            "py", "php", "php3", "php4", "php5", "phtml", "js", "html", "htm",
            "cgi", "pl", "ps1", "vbs", "msi", "dll", "so", "dylib", "bin", "com"
    );

    private static final Map<String, String> IMAGE_EXTENSIONS = Map.of(
            "jpg", "image/jpeg",
            "jpeg", "image/jpeg",
            "png", "image/png",
            "gif", "image/gif",
            "webp", "image/webp"
    );

    private static final Map<String, String> VIDEO_EXTENSIONS = Map.of(
            "mp4", "video/mp4",
            "webm", "video/webm",
            "mov", "video/quicktime",
            "mkv", "video/x-matroska"
    );

    private static final Map<String, String> DOCUMENT_EXTENSIONS = Map.ofEntries(
            Map.entry("pdf", "application/pdf"),
            Map.entry("doc", "application/msword"),
            Map.entry("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
            Map.entry("txt", "text/plain"),
            Map.entry("xls", "application/vnd.ms-excel"),
            Map.entry("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
            Map.entry("ppt", "application/vnd.ms-powerpoint"),
            Map.entry("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation"),
            Map.entry("zip", "application/zip"),
            Map.entry("csv", "text/csv")
    );

    private static final long MAX_IMAGE_SIZE = 15L * 1024 * 1024;      // 15 MB
    private static final long MAX_DOCUMENT_SIZE = 25L * 1024 * 1024;   // 25 MB
    private static final long MAX_VIDEO_SIZE = 50L * 1024 * 1024;      // 50 MB

    private final Path storageDirectory;

    public AttachmentStorageService() {
        String envDir = System.getenv("STUDENTHUB_UPLOAD_DIR");
        Path dir;
        if (envDir != null && !envDir.isBlank()) {
            dir = Paths.get(envDir.trim());
        } else {
            dir = Paths.get(System.getProperty("java.io.tmpdir"), "studenthub_uploads");
        }
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize upload storage directory: " + dir, e);
        }
        this.storageDirectory = dir.toAbsolutePath().normalize();
    }

    public AttachmentStorageService(Path customDirectory) {
        try {
            Files.createDirectories(customDirectory);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize custom upload storage directory: " + customDirectory, e);
        }
        this.storageDirectory = customDirectory.toAbsolutePath().normalize();
    }

    public ValidationStatus validate(String filename, String declaredMime, long size) {
        if (filename == null || filename.isBlank() || size <= 0) {
            return new ValidationStatus(ValidationResult.EMPTY_FILE, null, null, "File is empty or invalid.");
        }
        String ext = extractExtension(filename);
        if (ext.isEmpty() || BLOCKED_EXTENSIONS.contains(ext)) {
            return new ValidationStatus(ValidationResult.DISALLOWED_EXTENSION, null, null, "File type is not permitted for upload.");
        }

        if (IMAGE_EXTENSIONS.containsKey(ext)) {
            if (size > MAX_IMAGE_SIZE) {
                return new ValidationStatus(ValidationResult.FILE_TOO_LARGE, "IMAGE", null, "Images must not exceed 15 MB.");
            }
            String mime = declaredMime != null && declaredMime.startsWith("image/") ? declaredMime : IMAGE_EXTENSIONS.get(ext);
            return new ValidationStatus(ValidationResult.VALID, "IMAGE", mime, null);
        }

        if (VIDEO_EXTENSIONS.containsKey(ext)) {
            if (size > MAX_VIDEO_SIZE) {
                return new ValidationStatus(ValidationResult.FILE_TOO_LARGE, "VIDEO", null, "Videos must not exceed 50 MB.");
            }
            String mime = declaredMime != null && declaredMime.startsWith("video/") ? declaredMime : VIDEO_EXTENSIONS.get(ext);
            return new ValidationStatus(ValidationResult.VALID, "VIDEO", mime, null);
        }

        if (DOCUMENT_EXTENSIONS.containsKey(ext)) {
            if (size > MAX_DOCUMENT_SIZE) {
                return new ValidationStatus(ValidationResult.FILE_TOO_LARGE, "DOCUMENT", null, "Documents must not exceed 25 MB.");
            }
            String mime = DOCUMENT_EXTENSIONS.getOrDefault(ext, declaredMime != null ? declaredMime : "application/octet-stream");
            return new ValidationStatus(ValidationResult.VALID, "DOCUMENT", mime, null);
        }

        return new ValidationStatus(ValidationResult.UNKNOWN_TYPE, null, null, "Unsupported file format.");
    }

    public StoredFileInfo store(InputStream inputStream, String originalFilename, String declaredMime, long size)
            throws IOException {
        ValidationStatus status = validate(originalFilename, declaredMime, size);
        if (!status.isValid()) {
            throw new IllegalArgumentException(status.message());
        }

        String safeOriginalName = Paths.get(originalFilename).getFileName().toString();
        String ext = extractExtension(safeOriginalName);
        String storedFilename = UUID.randomUUID().toString() + (ext.isEmpty() ? "" : "." + ext);

        Path targetPath = storageDirectory.resolve(storedFilename).normalize();
        if (!targetPath.startsWith(storageDirectory)) {
            throw new SecurityException("Invalid path traversal detected.");
        }

        Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
        long actualSize = Files.size(targetPath);

        return new StoredFileInfo(safeOriginalName, storedFilename, status.fileType(), status.detectedMime(), actualSize);
    }

    public File resolveFile(String storedFilename) {
        if (storedFilename == null || storedFilename.isBlank()) {
            return null;
        }
        if (storedFilename.contains("\\") || storedFilename.contains("/")) {
            throw new SecurityException("Path traversal prohibited.");
        }
        Path targetPath = storageDirectory.resolve(storedFilename).normalize();
        if (!targetPath.startsWith(storageDirectory)) {
            throw new SecurityException("Path traversal prohibited.");
        }
        File file = targetPath.toFile();
        return file.exists() && file.isFile() ? file : null;
    }

    public boolean deleteFile(String storedFilename) {
        if (storedFilename == null || storedFilename.isBlank()) {
            return false;
        }
        if (storedFilename.contains("\\") || storedFilename.contains("/")) {
            return false;
        }
        Path targetPath = storageDirectory.resolve(storedFilename).normalize();
        if (!targetPath.startsWith(storageDirectory)) {
            return false;
        }
        try {
            return Files.deleteIfExists(targetPath);
        } catch (IOException e) {
            return false;
        }
    }

    public String extractExtension(String filename) {
        if (filename == null) return "";
        int idx = filename.lastIndexOf('.');
        if (idx < 0 || idx == filename.length() - 1) return "";
        return filename.substring(idx + 1).toLowerCase(Locale.ROOT).trim();
    }
}
