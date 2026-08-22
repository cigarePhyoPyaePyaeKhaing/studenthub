package com.studenthub.util;

import jakarta.servlet.http.Part;
import java.io.*;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AttachmentValidatorTest {
    @Test void validPngIsAccepted() throws Exception {
        byte[] png={(byte)0x89,'P','N','G',13,10,26,10};
        var upload=AttachmentValidator.validate(new FakePart("photo.png","image/png",png),false);
        assertTrue(upload.isPresent()); assertEquals("png",upload.get().extension());
    }
    @Test void svgAndExtensionSpoofingAreRejected() {
        assertThrows(IOException.class,()->AttachmentValidator.validate(new FakePart("x.svg","image/svg+xml","<svg".getBytes()),false));
        byte[] png={(byte)0x89,'P','N','G',13,10,26,10};
        assertThrows(IOException.class,()->AttachmentValidator.validate(new FakePart("malware.exe","image/png",png),false));
    }
    private record FakePart(String submittedFileName,String contentType,byte[] bytes) implements Part {
        public InputStream getInputStream(){return new ByteArrayInputStream(bytes);} public String getName(){return "attachment";} public String getSubmittedFileName(){return submittedFileName;} public String getContentType(){return contentType;} public long getSize(){return bytes.length;}
        public void write(String fileName){} public void delete(){} public String getHeader(String name){return null;} public Collection<String> getHeaders(String name){return List.of();} public Collection<String> getHeaderNames(){return List.of();}
    }
}
