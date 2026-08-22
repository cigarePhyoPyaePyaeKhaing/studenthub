package com.studenthub.util;

import com.studenthub.model.Attachment;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class AttachmentValidator {
    public static final long MAX_REQUEST_BYTES = 25L * 1024 * 1024;
    private static final Map<String,String> EXT = Map.ofEntries(
            Map.entry("image/jpeg","jpg"), Map.entry("image/png","png"), Map.entry("image/webp","webp"),
            Map.entry("video/mp4","mp4"), Map.entry("video/webm","webm"),
            Map.entry("audio/webm","webm"), Map.entry("audio/mp4","m4a"), Map.entry("audio/mpeg","mp3"),
            Map.entry("application/pdf","pdf"), Map.entry("text/plain","txt"),
            Map.entry("application/zip","zip"), Map.entry("application/msword","doc"),
            Map.entry("application/vnd.openxmlformats-officedocument.wordprocessingml.document","docx"),
            Map.entry("application/vnd.ms-powerpoint","ppt"),
            Map.entry("application/vnd.openxmlformats-officedocument.presentationml.presentation","pptx"),
            Map.entry("application/vnd.ms-excel","xls"),
            Map.entry("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet","xlsx"));
    private AttachmentValidator() {}

    public record Upload(String originalName, String extension, String mimeType, byte[] content) {
        public Attachment metadata(String storedName) { return new Attachment(originalName, storedName, mimeType, content.length); }
    }

    public static Optional<Upload> validate(Part part, boolean allowAudio) throws IOException {
        if (part == null || part.getSize() == 0) return Optional.empty();
        String mime = part.getContentType() == null ? "" : part.getContentType().toLowerCase(Locale.ROOT);
        String extension = EXT.get(mime);
        if (extension == null || (!allowAudio && mime.startsWith("audio/"))) throw new IOException("Unsupported attachment type.");
        long limit = mime.startsWith("video/") ? 25L*1024*1024 : mime.startsWith("image/") ? 5L*1024*1024 : 10L*1024*1024;
        if (part.getSize() > limit) throw new IOException("Attachment exceeds the allowed size.");
        byte[] content = part.getInputStream().readNBytes((int)limit + 1);
        if (!signatureMatches(mime, content)) throw new IOException("Attachment content does not match its type.");
        String submitted = part.getSubmittedFileName();
        if (submitted != null && submitted.contains(".")) {
            String submittedExtension = submitted.substring(submitted.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
            boolean compatibleJpeg = extension.equals("jpg") && (submittedExtension.equals("jpg") || submittedExtension.equals("jpeg"));
            if (!compatibleJpeg && !extension.equals(submittedExtension)) throw new IOException("Attachment extension does not match its type.");
        }
        String safeName = submitted == null ? "attachment." + extension : submitted.replaceAll("[^A-Za-z0-9._ -]", "_");
        if (safeName.length() > 180) safeName = safeName.substring(safeName.length() - 180);
        return Optional.of(new Upload(safeName, extension, mime, content));
    }

    private static boolean signatureMatches(String mime, byte[] b) {
        if (b.length < 4) return false;
        if (mime.equals("image/jpeg")) return u(b[0])==0xff && u(b[1])==0xd8 && u(b[2])==0xff;
        if (mime.equals("image/png")) return b.length>=8 && u(b[0])==0x89 && b[1]=='P' && b[2]=='N' && b[3]=='G';
        if (mime.equals("image/webp")) return b.length>=12 && b[0]=='R'&&b[1]=='I'&&b[2]=='F'&&b[3]=='F'&&b[8]=='W'&&b[9]=='E'&&b[10]=='B'&&b[11]=='P';
        if (mime.equals("application/pdf")) return b[0]=='%'&&b[1]=='P'&&b[2]=='D'&&b[3]=='F';
        if (mime.equals("application/zip") || mime.contains("officedocument")) return b[0]=='P'&&b[1]=='K';
        if (mime.startsWith("video/") || mime.startsWith("audio/")) return b.length >= 12;
        return !mime.equals("text/html") && !mime.contains("javascript") && !mime.contains("svg");
    }
    private static int u(byte value) { return value & 0xff; }
}
