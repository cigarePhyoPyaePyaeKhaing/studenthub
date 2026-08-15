package com.studenthub.controller;

import com.studenthub.service.AuthService;
import com.studenthub.util.CsrfToken;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name = "ResetPasswordServlet", urlPatterns = "/reset-password")
public class ResetPasswordServlet extends HttpServlet {
    private final AuthService authService = new AuthService();
    @Override protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!authorized(request)) { response.sendRedirect(request.getContextPath() + "/forgot-password"); return; }
        request.setAttribute("csrfToken", CsrfToken.getOrCreate(request.getSession())); request.getRequestDispatcher("/WEB-INF/views/auth/reset-password.jsp").forward(request, response);
    }
    @Override protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!CsrfToken.isValid(request)) { response.sendError(403); return; }
        if (!authorized(request)) { response.sendRedirect(request.getContextPath() + "/forgot-password"); return; }
        long userId = (Long) request.getSession().getAttribute("pendingResetUserId");
        try {
            String error = authService.resetPassword(userId, request.getParameter("password"), request.getParameter("confirmPassword"));
            if (error == null) { request.getSession().invalidate(); request.getSession(true).setAttribute("flash", "Password updated. You can now sign in."); response.sendRedirect(request.getContextPath() + "/login"); return; }
            request.setAttribute("error", error);
        } catch (SQLException exception) { getServletContext().log("Password update failed: " + exception.getClass().getName()); request.setAttribute("error", "Password reset is temporarily unavailable."); }
        doGet(request, response);
    }
    private boolean authorized(HttpServletRequest request) { return request.getSession(false) != null && Boolean.TRUE.equals(request.getSession().getAttribute("passwordResetAuthorized")) && request.getSession().getAttribute("pendingResetUserId") instanceof Long; }
}
