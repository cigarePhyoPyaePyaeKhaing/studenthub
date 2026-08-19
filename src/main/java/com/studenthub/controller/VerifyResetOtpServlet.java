package com.studenthub.controller;

import com.studenthub.service.AuthService;
import com.studenthub.service.OtpService;
import com.studenthub.util.CsrfToken;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.time.Instant;

@WebServlet(name = "VerifyResetOtpServlet", urlPatterns = "/verify-reset-code")
public class VerifyResetOtpServlet extends HttpServlet {
    private final AuthService authService = new AuthService();
    @Override protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (request.getSession(false) == null || !Boolean.TRUE.equals(request.getSession().getAttribute("passwordRecoveryInitiated"))) { response.sendRedirect(request.getContextPath() + "/forgot-password"); return; }
        request.setAttribute("maskedEmail", request.getSession().getAttribute("pendingResetMaskedEmail"));
        Object sentAt = request.getSession().getAttribute("passwordResetSentAt");
        long elapsed = sentAt instanceof Instant instant ? java.time.Duration.between(instant, Instant.now()).toSeconds() : 60;
        request.setAttribute("resendSeconds", Math.max(0, 60 - elapsed));
        request.setAttribute("csrfToken", CsrfToken.getOrCreate(request.getSession()));
        request.getRequestDispatcher("/WEB-INF/views/auth/verify-reset.jsp").forward(request, response);
    }
    @Override protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!CsrfToken.isValid(request)) { response.sendError(403); return; }
        if ("cancel".equals(request.getParameter("action"))) { clearRecovery(request); response.sendRedirect(request.getContextPath() + "/forgot-password"); return; }
        if (!Boolean.TRUE.equals(request.getSession().getAttribute("passwordRecoveryInitiated"))) { response.sendRedirect(request.getContextPath() + "/forgot-password"); return; }
        Object value = request.getSession().getAttribute("pendingResetUserId");
        if ("resend".equals(request.getParameter("action"))) {
            if (value instanceof Long userId && authService.resendPasswordReset(userId).delivered()) request.getSession().setAttribute("passwordResetSentAt", Instant.now());
            request.setAttribute("message", "If an account matches the information provided, a new verification code has been sent."); doGet(request,response); return;
        }
        if (!(value instanceof Long userId)) { request.setAttribute("error", "Incorrect or expired code. Please try again."); doGet(request,response); return; }
        try {
            OtpService.VerificationResult result = authService.verifyPasswordReset(userId, request.getParameter("code"));
            if (result == OtpService.VerificationResult.SUCCESS) { request.getSession().setAttribute("passwordResetAuthorizedAt", Instant.now()); response.sendRedirect(request.getContextPath() + "/reset-password"); return; }
            request.setAttribute("error", "Incorrect or expired code. Please try again.");
        } catch (SQLException exception) { getServletContext().log("Reset verification failed: " + exception.getClass().getName()); request.setAttribute("error", "Verification is temporarily unavailable."); }
        doGet(request, response);
    }
    private void clearRecovery(HttpServletRequest request) { for (String name : new String[]{"passwordRecoveryInitiated","pendingResetUserId","pendingResetMaskedEmail","passwordResetSentAt","passwordResetAuthorizedAt","passwordResetComplete"}) request.getSession().removeAttribute(name); }
}
