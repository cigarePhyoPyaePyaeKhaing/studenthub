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
import java.time.Duration;
import java.time.Instant;

@WebServlet(name = "ResetPasswordServlet", urlPatterns = "/reset-password")
public class ResetPasswordServlet extends HttpServlet {
    private final AuthService authService = new AuthService();
    @Override protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (Boolean.TRUE.equals(request.getSession(false) == null ? null : request.getSession().getAttribute("passwordResetComplete"))) { request.getRequestDispatcher("/WEB-INF/views/auth/reset-success.jsp").forward(request,response); return; }
        if (!authorized(request)) { response.sendRedirect(request.getContextPath() + "/forgot-password"); return; }
        request.setAttribute("csrfToken", CsrfToken.getOrCreate(request.getSession())); request.getRequestDispatcher("/WEB-INF/views/auth/reset-password.jsp").forward(request, response);
    }
    @Override protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!CsrfToken.isValid(request)) { response.sendError(403); return; }
        if (!authorized(request)) { response.sendRedirect(request.getContextPath() + "/forgot-password"); return; }
        long userId = (Long) request.getSession().getAttribute("pendingResetUserId");
        try {
            String error = authService.resetPassword(userId, request.getParameter("password"), request.getParameter("confirmPassword"));
            if (error == null) { clearRecovery(request); request.getSession().setAttribute("passwordResetComplete", Boolean.TRUE); response.sendRedirect(request.getContextPath() + "/reset-password"); return; }
            request.setAttribute("error", error);
        } catch (SQLException exception) { getServletContext().log("Password update failed: " + exception.getClass().getName()); request.setAttribute("error", "Password reset is temporarily unavailable."); }
        doGet(request, response);
    }
    private boolean authorized(HttpServletRequest request) { if (request.getSession(false) == null || !(request.getSession().getAttribute("passwordResetAuthorizedAt") instanceof Instant authorizedAt) || !(request.getSession().getAttribute("pendingResetUserId") instanceof Long)) return false; return Duration.between(authorizedAt, Instant.now()).compareTo(Duration.ofMinutes(10)) < 0; }
    private void clearRecovery(HttpServletRequest request) { for (String name : new String[]{"passwordRecoveryInitiated","pendingResetUserId","pendingResetMaskedEmail","passwordResetSentAt","passwordResetAuthorizedAt"}) request.getSession().removeAttribute(name); }
}
