package com.studenthub.util;
import org.junit.jupiter.api.*;import java.io.*;import java.nio.file.*;import static org.junit.jupiter.api.Assertions.*;
class AttachmentStreamingTest{private Path dir;@BeforeEach void setup()throws Exception{dir=Files.createTempDirectory("studenthub-stream-test");}@AfterEach void cleanup()throws Exception{try(var files=Files.list(dir)){files.forEach(p->{try{Files.deleteIfExists(p);}catch(Exception ignored){}});}Files.deleteIfExists(dir);}
 @Test void streamsMp4ToFinalUuidKey()throws Exception{byte[] bytes={0,0,0,20,'f','t','y','p',1,2,3};var upload=new AttachmentStorage(dir).saveStream("clip.mp4","video/mp4",bytes.length,new ByteArrayInputStream(bytes));assertTrue(Files.isRegularFile(dir.resolve(upload.storageKey())));assertArrayEquals(bytes,Files.readAllBytes(dir.resolve(upload.storageKey())));assertFalse(upload.storageKey().endsWith(".part"));}
 @Test void invalidSignatureLeavesNoFile()throws Exception{assertThrows(IOException.class,()->new AttachmentStorage(dir).saveStream("clip.mp4","video/mp4",4,new ByteArrayInputStream(new byte[]{1,2,3,4})));try(var files=Files.list(dir)){assertEquals(0,files.count());}}
}
