package com.studenthub.util;

import java.nio.charset.StandardCharsets;
import java.util.*;

public final class AttachmentValidator {
    public static final long MAX_BYTES=UploadPolicy.VIDEO_MAX;
    private static final Map<String,Set<String>> ALLOWED=Map.ofEntries(
            Map.entry("image/jpeg",Set.of("jpg","jpeg")),Map.entry("image/png",Set.of("png")),Map.entry("image/webp",Set.of("webp")),Map.entry("image/gif",Set.of("gif")),
            Map.entry("video/mp4",Set.of("mp4")),Map.entry("video/webm",Set.of("webm")),Map.entry("application/pdf",Set.of("pdf")),
            Map.entry("application/msword",Set.of("doc")),Map.entry("application/vnd.openxmlformats-officedocument.wordprocessingml.document",Set.of("docx")),
            Map.entry("application/vnd.ms-powerpoint",Set.of("ppt")),Map.entry("application/vnd.openxmlformats-officedocument.presentationml.presentation",Set.of("pptx")),
            Map.entry("application/vnd.ms-excel",Set.of("xls")),Map.entry("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",Set.of("xlsx")),
            Map.entry("text/plain",Set.of("txt")),Map.entry("application/zip",Set.of("zip")),
            Map.entry("audio/mpeg",Set.of("mp3")),Map.entry("audio/mp4",Set.of("m4a")),Map.entry("audio/aac",Set.of("aac")),
            Map.entry("audio/wav",Set.of("wav")),Map.entry("audio/x-wav",Set.of("wav")),Map.entry("audio/ogg",Set.of("ogg")),Map.entry("audio/webm",Set.of("webm")));
    private AttachmentValidator(){}
    public static Result validate(String filename,String mime,byte[] data){
        if(data==null||data.length==0)return new Result(false,null,"Choose a non-empty attachment.");
        if(data.length>MAX_BYTES)return new Result(false,null,"Attachment must be 20 MB or smaller.");
        String safeName=filename==null?"attachment":filename.replace('\\','/'); safeName=safeName.substring(safeName.lastIndexOf('/')+1).replaceAll("[\\r\\n]","_");
        if(safeName.length()>180)safeName=safeName.substring(safeName.length()-180);
        int dot=safeName.lastIndexOf('.'); String ext=dot<0?"":safeName.substring(dot+1).toLowerCase(Locale.ROOT); String type=mime==null?"":mime.toLowerCase(Locale.ROOT).split(";",2)[0];
        if(!ALLOWED.getOrDefault(type,Set.of()).contains(ext)||!signature(type,ext,data))return new Result(false,null,"This attachment type is not supported or its content does not match the file type.");
        long limit=UploadPolicy.limit(type);
        if(data.length>limit)return new Result(false,null,type.startsWith("image/")?"Images must be 10 MB or smaller.":type.startsWith("video/")?"Videos must be 50 MB or smaller.":"Attachments must be 20 MB or smaller.");
        return new Result(true,new Validated(safeName,type,ext,data),null);
    }
    private static boolean signature(String type,String ext,byte[] b){
        if(type.equals("image/jpeg"))return starts(b,0xff,0xd8,0xff); if(type.equals("image/png"))return starts(b,0x89,0x50,0x4e,0x47,0x0d,0x0a,0x1a,0x0a);
        if(type.equals("image/gif"))return text(b,0,6).equals("GIF87a")||text(b,0,6).equals("GIF89a"); if(type.equals("image/webp"))return text(b,0,4).equals("RIFF")&&text(b,8,4).equals("WEBP");
        if(type.equals("video/mp4"))return text(b,4,4).equals("ftyp"); if(type.equals("video/webm")||type.equals("audio/webm"))return starts(b,0x1a,0x45,0xdf,0xa3); if(type.equals("application/pdf"))return text(b,0,5).equals("%PDF-");
        if(type.equals("audio/mpeg"))return text(b,0,3).equals("ID3")||(b.length>1&&(b[0]&255)==0xff&&((b[1]&0xe0)==0xe0));if(type.equals("audio/mp4"))return text(b,4,4).equals("ftyp");if(type.equals("audio/aac"))return b.length>1&&(b[0]&255)==0xff&&((b[1]&0xf6)==0xf0);if(type.equals("audio/wav")||type.equals("audio/x-wav"))return text(b,0,4).equals("RIFF")&&text(b,8,4).equals("WAVE");if(type.equals("audio/ogg"))return text(b,0,4).equals("OggS");
        if(Set.of("doc","ppt","xls").contains(ext))return starts(b,0xd0,0xcf,0x11,0xe0,0xa1,0xb1,0x1a,0xe1); if(Set.of("docx","pptx","xlsx","zip").contains(ext))return starts(b,0x50,0x4b,0x03,0x04)||starts(b,0x50,0x4b,0x05,0x06);
        if(type.equals("text/plain")){int checked=Math.min(b.length,4096);for(int i=0;i<checked;i++)if(b[i]==0)return false;return true;} return false;
    }
    private static boolean starts(byte[]b,int...v){if(b.length<v.length)return false;for(int i=0;i<v.length;i++)if((b[i]&255)!=v[i])return false;return true;} private static String text(byte[]b,int o,int n){return b.length<o+n?"":new String(b,o,n,StandardCharsets.US_ASCII);}
    public record Validated(String originalFilename,String mimeType,String extension,byte[] content){} public record Result(boolean valid,Validated value,String error){}
    public static Result validateHeader(String filename,String mime,byte[] header,long size){Result result=validate(filename,mime,header);if(!result.valid())return result;if(size>UploadPolicy.limit(result.value.mimeType()))return new Result(false,null,UploadPolicy.sizeError(result.value.mimeType()));return new Result(true,new Validated(result.value.originalFilename(),result.value.mimeType(),result.value.extension(),header),null);}
}
