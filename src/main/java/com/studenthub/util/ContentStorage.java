package com.studenthub.util;
import java.io.IOException;import java.nio.file.Path;import java.util.Optional;
public interface ContentStorage {AttachmentUpload save(AttachmentValidator.Validated value)throws IOException;Optional<Path> find(String key);void delete(String key);}
