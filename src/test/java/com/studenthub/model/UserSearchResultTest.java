package com.studenthub.model;
import org.junit.jupiter.api.Test;import java.time.LocalDateTime;import static org.junit.jupiter.api.Assertions.*;
class UserSearchResultTest{@Test void exposesOnlyPublicIdentity(){var user=new UserSearchResult(2,"TNT-2464","Phyo Pyae","ADMIN","photo.jpg",LocalDateTime.now(java.time.ZoneOffset.UTC));assertEquals("Phyo Pyae",user.getFullName());assertEquals("TNT-2464",user.getStudentId());assertTrue(user.isActiveNow());}}
