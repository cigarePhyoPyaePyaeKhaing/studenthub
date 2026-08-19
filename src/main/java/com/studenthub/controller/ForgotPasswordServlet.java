package com.studenthub.controller;

import com.studenthub.service.AuthService;
import com.studenthub.util.CsrfToken;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import com.studenthub.model.User;

@WebServlet(name = "ForgotPasswordServlet", urlPatterns = "/forgot-password")
public class ForgotPasswordServlet extends HttpServlet {
    private final AuthService authService = new AuthService();

    @Override protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("csrfToken", CsrfToken.getOrCreate(request.getSession(true)));
        request.getRequestDispatcher("/WEB-INF/views/auth/forgot-password.jsp").forward(request, response);
    }

    @Override protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!CsrfToken.isValid(request)) { response.sendError(403); return; }
        String login = request.getParameter("login");
        AuthService.PasswordResetRequestResult result = authService.requestPasswordReset(login);
        clearRecovery(request);
        if (result.delivered()) {
            User user = result.user();
            request.getSession().setAttribute("passwordRecoveryInitiated", Boolean.TRUE);
            request.getSession().setAttribute("passwordResetSentAt", java.time.Instant.now());
            request.getSession().setAttribute("pendingResetUserId", user.userId());
            request.getSession().setAttribute("pendingResetMaskedEmail", maskEmail(user.email()));
            response.sendRedirect(request.getContextPath() + "/verify-reset-code");
            return;
        }
        boolean deliveryProblem = switch (result.status()) {
            case OTP_STORAGE_FAILED, BREVO_CONFIGURATION_MISSING, BREVO_UNAUTHORIZED,
                 BREVO_PROVIDER_ERROR, NETWORK_ERROR, INTERRUPTED_REQUEST, THROTTLED -> true;
            default -> false;
        };
        request.setAttribute("message", deliveryProblem
                ? "We couldn't send the verification code right now. Please try again shortly."
                : "If an eligible account matches the information provided, a verification code will be sent.");
        doGet(request, response);
    }

    private void clearRecovery(HttpServletRequest request) {
        if (request.getSession(false) == null) return;
        for (String name : new String[]{"passwordRecoveryInitiated","pendingResetUserId","pendingResetMaskedEmail","passwordResetSentAt","passwordResetAuthorizedAt","passwordResetComplete"}) request.getSession().removeAttribute(name);
    }
    private String maskEmail(String email) {
        int at = email == null ? -1 : email.indexOf('@');
        if (at < 1) return "your email address";
        return email.substring(0, 1) + "*".repeat(Math.max(3, at - 1)) + email.substring(at);
    }
}
