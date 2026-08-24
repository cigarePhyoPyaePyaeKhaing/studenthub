package com.studenthub.util;
import java.io.*;import java.nio.file.*;import java.util.*;import java.util.regex.Pattern;
public class AttachmentStorage implements ContentStorage {
    private static final Pattern KEY=Pattern.compile("^[a-f0-9-]{36}\\.[a-z0-9]{2,5}$"); private final Path directory;
    public AttachmentStorage(){this(resolve());} AttachmentStorage(Path path){directory=path==null?null:path.toAbsolutePath().normalize();}
    public AttachmentUpload save(AttachmentValidator.Validated value)throws IOException{if(directory==null)throw new IOException("Persistent attachment storage is not configured");Files.createDirectories(directory);String key=UUID.randomUUID()+"."+value.extension();Path p=safe(key).orElseThrow();Files.write(p,value.content(),StandardOpenOption.CREATE_NEW);return new AttachmentUpload(value.originalFilename(),key,value.mimeType(),value.content().length);}
    public Optional<Path> find(String key){return safe(key).filter(Files::isRegularFile);} public void delete(String key){safe(key).ifPresent(p->{try{Files.deleteIfExists(p);}catch(IOException ignored){}});}
    private Optional<Path> safe(String key){if(directory==null||key==null||!KEY.matcher(key).matches())return Optional.empty();Path p=directory.resolve(key).normalize();return p.getParent()!=null&&p.getParent().equals(directory)?Optional.of(p):Optional.empty();}
    public boolean isConfigured(){return directory!=null;} public boolean ensureWritable(){if(directory==null)return false;try{Files.createDirectories(directory);return Files.isDirectory(directory)&&Files.isWritable(directory);}catch(IOException|SecurityException e){return false;}}
    private static Path resolve(){String configured=System.getenv("STUDENTHUB_ATTACHMENT_DIR");if(configured!=null&&!configured.isBlank())return Path.of(configured);String root=System.getenv("STUDENTHUB_UPLOAD_DIR");if(root!=null&&!root.isBlank())return Path.of(root).resolve("attachments");if("production".equalsIgnoreCase(System.getenv("APP_ENV")))return null;return Path.of(System.getProperty("java.io.tmpdir"),"studenthub-uploads","attachments");}
}
