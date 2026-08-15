package com.studenthub.util;

import jakarta.servlet.http.*;
import java.io.IOException;

public final class AdminRequest {
    private AdminRequest() {}
    public static boolean requireAdmin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!Authorization.isAuthenticated(request.getSession(false))) {
            response.sendRedirect(request.getContextPath() + "/login"); return false;
        }
        if (!Authorization.isAdmin(request.getSession().getAttribute("role"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN); return false;
        }
        return true;
    }
    public static Long positiveId(String value) {
        try { long id = Long.parseLong(value); return id > 0 ? id : null; }
        catch (RuntimeException exception) { return null; }
    }
}
