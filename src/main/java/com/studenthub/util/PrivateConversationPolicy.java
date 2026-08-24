package com.studenthub.util;

import java.util.UUID;

public final class PrivateConversationPolicy{
 private PrivateConversationPolicy(){}
 public record Pair(long first,long second){}
 public static Pair normalize(long a,long b){if(a<=0||b<=0)throw new IllegalArgumentException("Invalid user.");if(a==b)throw new IllegalArgumentException("You cannot message yourself.");return new Pair(Math.min(a,b),Math.max(a,b));}
 public static boolean participant(long viewer,long a,long b){return viewer==a||viewer==b;}
 public static boolean validClientId(String value){try{return value!=null&&UUID.fromString(value).toString().equalsIgnoreCase(value);}catch(IllegalArgumentException e){return false;}}
}
