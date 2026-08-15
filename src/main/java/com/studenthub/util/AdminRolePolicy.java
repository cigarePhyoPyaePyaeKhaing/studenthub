package com.studenthub.util;

import com.studenthub.model.Role;

public final class AdminRolePolicy {
    public enum Decision { ALLOWED, NO_CHANGE, FORBIDDEN, SELF_DEMOTION, LAST_ADMIN, INVALID_ROLE, NOT_FOUND }
    private AdminRolePolicy() {}
    public static Decision decide(boolean actorIsAdmin, long actorId, long targetId,
                                  Role currentRole, Role newRole, int adminCount) {
        if (!actorIsAdmin) return Decision.FORBIDDEN;
        if (newRole == null) return Decision.INVALID_ROLE;
        if (currentRole == null) return Decision.NOT_FOUND;
        if (currentRole == newRole) return Decision.NO_CHANGE;
        if (actorId == targetId && currentRole == Role.ADMIN && newRole != Role.ADMIN) return Decision.SELF_DEMOTION;
        if (currentRole == Role.ADMIN && newRole != Role.ADMIN && adminCount <= 1) return Decision.LAST_ADMIN;
        return Decision.ALLOWED;
    }
}
