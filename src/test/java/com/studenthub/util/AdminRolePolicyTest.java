package com.studenthub.util;

import com.studenthub.model.Role;
import org.junit.jupiter.api.Test;
import static com.studenthub.util.AdminRolePolicy.Decision.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AdminRolePolicyTest {
    @Test void invalidRoleRejected() { assertEquals(INVALID_ROLE, decide(1,2,Role.STUDENT,null,1)); }
    @Test void nonAdminCannotChangeRoles() {
        assertEquals(FORBIDDEN, AdminRolePolicy.decide(false,1,2,Role.STUDENT,Role.CR,1));
    }
    @Test void studentCanBePromotedToCr() { assertEquals(ALLOWED, decide(1,2,Role.STUDENT,Role.CR,1)); }
    @Test void crCanBeDemotedToStudent() { assertEquals(ALLOWED, decide(1,2,Role.CR,Role.STUDENT,1)); }
    @Test void anotherUserCanBePromotedToAdmin() { assertEquals(ALLOWED, decide(1,2,Role.STUDENT,Role.ADMIN,1)); }
    @Test void selfDemotionRejected() { assertEquals(SELF_DEMOTION, decide(1,1,Role.ADMIN,Role.CR,2)); }
    @Test void finalAdminDemotionRejected() { assertEquals(LAST_ADMIN, decide(1,2,Role.ADMIN,Role.STUDENT,1)); }
    @Test void adminDemotionAllowedWhenAnotherAdminRemains() { assertEquals(ALLOWED, decide(1,2,Role.ADMIN,Role.STUDENT,2)); }
    @Test void missingUserHandledAsNotFound() { assertEquals(NOT_FOUND, decide(1,2,null,Role.CR,1)); }
    @Test void sameRoleIsSafeNoOp() { assertEquals(NO_CHANGE, decide(1,2,Role.CR,Role.CR,1)); }
    private AdminRolePolicy.Decision decide(long actor,long target,Role current,Role next,int admins){
        return AdminRolePolicy.decide(true,actor,target,current,next,admins);
    }
}
