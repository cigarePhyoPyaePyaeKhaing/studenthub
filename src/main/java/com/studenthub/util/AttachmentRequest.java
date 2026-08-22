package com.studenthub.util;
import jakarta.servlet.http.Part;import java.io.*;
public final class AttachmentRequest {
 private AttachmentRequest(){}
 public static Result read(Part part){if(part==null||part.getSize()==0)return new Result(null,null);if(part.getSize()>AttachmentValidator.MAX_BYTES)return new Result(null,"Attachment must be 20 MB or smaller.");try{byte[] data=part.getInputStream().readNBytes((int)AttachmentValidator.MAX_BYTES+1);AttachmentValidator.Result checked=AttachmentValidator.validate(part.getSubmittedFileName(),part.getContentType(),data);if(!checked.valid())return new Result(null,checked.error());AttachmentUpload upload=new AttachmentStorage().save(checked.value());return new Result(upload,null);}catch(Exception e){return new Result(null,"The attachment could not be stored. Check persistent attachment storage configuration.");}}
 public static void discard(AttachmentUpload upload){if(upload!=null)new AttachmentStorage().delete(upload.storageKey());}
 public record Result(AttachmentUpload upload,String error){public boolean valid(){return error==null;}}
}
