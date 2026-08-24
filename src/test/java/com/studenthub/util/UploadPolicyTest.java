package com.studenthub.util;
import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;
class UploadPolicyTest{
 @Test void limitsAreConsistent(){assertEquals(10L*1024*1024,UploadPolicy.limit("image/png"));assertEquals(50L*1024*1024,UploadPolicy.limit("video/mp4"));assertEquals(20L*1024*1024,UploadPolicy.limit("audio/mpeg"));assertEquals(20L*1024*1024,UploadPolicy.limit("application/pdf"));assertTrue(UploadPolicy.MULTIPART_MAX>UploadPolicy.VIDEO_MAX);}
 @Test void validatesMp4AndWebmHeaders(){assertTrue(AttachmentValidator.validateHeader("clip.mp4","video/mp4",new byte[]{0,0,0,20,'f','t','y','p'},1024).valid());assertTrue(AttachmentValidator.validateHeader("clip.webm","video/webm",new byte[]{0x1a,0x45,(byte)0xdf,(byte)0xa3},1024).valid());}
 @Test void oversizedVideoRejectedBeforeStorage(){assertFalse(AttachmentValidator.validateHeader("clip.mp4","video/mp4",new byte[]{0,0,0,20,'f','t','y','p'},UploadPolicy.VIDEO_MAX+1).valid());}
}
