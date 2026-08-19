package com.studenthub.controller;

import com.studenthub.service.AuthService;
import com.studenthub.service.EmailServiceException;
import com.studenthub.util.CsrfToken;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import com.studenthub.model.User;
import java.util.Optional;

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
        Optional<User> found = Optional.empty();
        try {
            found = authService.requestPasswordReset(login);
        } catch (SQLException | EmailServiceException | IllegalStateException exception) {
            getServletContext().log("Password reset request failed: " + exception.getClass().getName());
        }
        clearRecovery(request);
        request.getSession().setAttribute("passwordRecoveryInitiated", Boolean.TRUE);
        request.getSession().setAttribute("passwordResetSentAt", java.time.Instant.now());
        if (found.isPresent()) {
            User user = found.get();
            request.getSession().setAttribute("pendingResetUserId", user.userId());
            request.getSession().setAttribute("pendingResetMaskedEmail", maskEmail(user.email()));
        }
        response.sendRedirect(request.getContextPath() + "/verify-reset-code");
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
