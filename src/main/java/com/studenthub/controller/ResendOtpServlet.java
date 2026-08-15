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

@WebServlet(name = "ResendOtpServlet", urlPatterns = "/resend-verification")
public class ResendOtpServlet extends HttpServlet {
    private final AuthService authService = new AuthService();
    @Override protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!CsrfToken.isValid(request)) { response.sendError(403); return; }
        Object pending = request.getSession(false) == null ? null : request.getSession().getAttribute("pendingVerificationUserId");
        if (!(pending instanceof Long userId)) { response.sendRedirect(request.getContextPath() + "/register"); return; }
        try {
            authService.resendVerification(userId);
            request.setAttribute("message", "A new verification code was sent.");
        } catch (IllegalStateException exception) {
            request.setAttribute("error", exception.getMessage());
        } catch (SQLException | EmailServiceException exception) {
            getServletContext().log("Verification resend failed: " + exception.getClass().getName());
            request.setAttribute("error", "A new code could not be sent right now.");
        }
        request.setAttribute("csrfToken", CsrfToken.getOrCreate(request.getSession()));
        request.getRequestDispatcher("/WEB-INF/views/auth/verify-email.jsp").forward(request, response);
    }
}
