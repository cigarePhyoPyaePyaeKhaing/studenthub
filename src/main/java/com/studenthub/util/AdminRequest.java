package com.studenthub.util;

import com.studenthub.dao.UserDAO;
import com.studenthub.model.Role;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

public final class AdminRequest {
    private AdminRequest() {}
    public static boolean requireAdmin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!Authorization.isAuthenticated(request.getSession(false))) {
            response.sendRedirect(request.getContextPath() + "/login"); return false;
        }
        Long userId = (Long) request.getSession().getAttribute("userId");
        try {
            if (userId == null || new UserDAO().findVerifiedRoleById(userId).orElse(null) != Role.ADMIN) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN); return false;
            }
        } catch (SQLException exception) {
            request.getServletContext().log("Admin authorization failed: " + exception.getClass().getName());
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE); return false;
        }
        return true;
    }
    public static Long positiveId(String value) {
        try { long id = Long.parseLong(value); return id > 0 ? id : null; }
        catch (RuntimeException exception) { return null; }
    }
}
