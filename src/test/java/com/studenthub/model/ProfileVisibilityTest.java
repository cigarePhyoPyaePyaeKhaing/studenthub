package com.studenthub.model;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class ProfileVisibilityTest {
    @Test void existingProfilesDefaultPrivate(){UserProfile p=new UserProfile(1,"TNT-1","A","a@b.test",Role.STUDENT,true,1,"A");assertEquals("PRIVATE",p.getProfileVisibility());assertFalse(p.isProfilePublic());}
    @Test void publicValueIsRecognized(){UserProfile p=new UserProfile(1,"TNT-1","A","a@b.test",Role.STUDENT,true,1,"A",null,null,null,null,null,null,null,null,false,true,"PUBLIC");assertTrue(p.isProfilePublic());}
}
