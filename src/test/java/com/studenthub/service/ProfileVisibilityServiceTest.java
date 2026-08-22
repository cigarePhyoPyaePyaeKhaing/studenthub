package com.studenthub.service;
import com.studenthub.dao.UserDAO;
import com.studenthub.model.Role;
import com.studenthub.model.UserProfile;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class ProfileVisibilityServiceTest {
    @Test void privateProfileIsOwnerAndAdminOnly() throws Exception {
        UserProfile privateProfile=profile("PRIVATE"); ProfileService service=new ProfileService(new StubUserDAO(privateProfile),null);
        assertTrue(service.findVisibleProfile(8,8,false).isPresent());
        assertTrue(service.findVisibleProfile(8,2,true).isPresent());
        assertTrue(service.findVisibleProfile(8,2,false).isEmpty());
    }
    @Test void publicProfileIsVisibleToAuthenticatedViewer() throws Exception {
        ProfileService service=new ProfileService(new StubUserDAO(profile("PUBLIC")),null);
        assertTrue(service.findVisibleProfile(8,2,false).isPresent());
    }
    private static UserProfile profile(String visibility){return new UserProfile(8,"TNT-8","Student","s@example.test",Role.STUDENT,true,4,"B",null,null,null,null,null,1L,"UIT","UIT",true,true,visibility);}
    private static class StubUserDAO extends UserDAO {private final UserProfile profile;StubUserDAO(UserProfile profile){this.profile=profile;}@Override public Optional<UserProfile> findProfileById(long id){return Optional.of(profile);}}
}
