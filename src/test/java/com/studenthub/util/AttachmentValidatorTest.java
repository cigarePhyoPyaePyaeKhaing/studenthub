package com.studenthub.util;
import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;
class AttachmentValidatorTest{
 @Test void acceptsMatchingPngAndPdf(){assertTrue(AttachmentValidator.validate("photo.png","image/png",new byte[]{(byte)0x89,0x50,0x4e,0x47,0x0d,0x0a,0x1a,0x0a}).valid());assertTrue(AttachmentValidator.validate("notes.pdf","application/pdf","%PDF-1.7".getBytes()).valid());}
 @Test void rejectsExtensionMimeMismatchAndActiveContent(){assertFalse(AttachmentValidator.validate("attack.jsp","image/png",new byte[]{(byte)0x89,0x50,0x4e,0x47,0x0d,0x0a,0x1a,0x0a}).valid());assertFalse(AttachmentValidator.validate("vector.svg","image/svg+xml","<svg>".getBytes()).valid());}
 @Test void rejectsSpoofedSignatureAndOversize(){assertFalse(AttachmentValidator.validate("photo.png","image/png","not png".getBytes()).valid());assertFalse(AttachmentValidator.validate("big.txt","text/plain",new byte[(int)AttachmentValidator.MAX_BYTES+1]).valid());}
 @Test void sanitizesSubmittedPath(){var result=AttachmentValidator.validate("../../notes.txt","text/plain","safe text".getBytes());assertTrue(result.valid());assertEquals("notes.txt",result.value().originalFilename());}
}
