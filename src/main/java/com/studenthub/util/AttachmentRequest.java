package com.studenthub.util;
import jakarta.servlet.http.Part;import java.io.*;
public final class AttachmentRequest {
 private AttachmentRequest(){}
 public static Result read(Part part){if(part==null||part.getSize()==0)return new Result(null,null);if(part.getSize()>UploadPolicy.limit(part.getContentType()))return new Result(null,UploadPolicy.sizeError(part.getContentType()));try(InputStream input=part.getInputStream()){return new Result(new AttachmentStorage().saveStream(part.getSubmittedFileName(),part.getContentType(),part.getSize(),input),null);}catch(Exception e){String message=e.getMessage();if(message!=null&&message.startsWith("INVALID_UPLOAD:"))return new Result(null,message.substring(15));if("FILE_TOO_LARGE".equals(message))return new Result(null,UploadPolicy.sizeError(part.getContentType()));return new Result(null,"The attachment could not be stored. Check persistent attachment storage configuration.");}}
 public static void discard(AttachmentUpload upload){if(upload!=null)new AttachmentStorage().delete(upload.storageKey());}
 public record Result(AttachmentUpload upload,String error){public boolean valid(){return error==null;}}
}
