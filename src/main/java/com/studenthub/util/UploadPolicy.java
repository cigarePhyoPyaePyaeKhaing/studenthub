package com.studenthub.util;
import java.util.Locale;
public final class UploadPolicy{
 public static final long IMAGE_MAX=10L*1024*1024,DOCUMENT_MAX=20L*1024*1024,AUDIO_MAX=20L*1024*1024,VIDEO_MAX=50L*1024*1024,MULTIPART_MAX=55L*1024*1024;
 private UploadPolicy(){}
 public static long limit(String mime){String m=mime==null?"":mime.toLowerCase(Locale.ROOT);return m.startsWith("image/")?IMAGE_MAX:m.startsWith("video/")?VIDEO_MAX:m.startsWith("audio/")?AUDIO_MAX:DOCUMENT_MAX;}
 public static String sizeError(String mime){return mime!=null&&mime.startsWith("video/")?"Videos must be 50 MB or smaller.":mime!=null&&mime.startsWith("image/")?"Images must be 10 MB or smaller.":"Attachments must be 20 MB or smaller.";}
}
