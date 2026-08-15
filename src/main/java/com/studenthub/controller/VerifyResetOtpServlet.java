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

@WebServlet(name = "VerifyResetOtpServlet", urlPatterns = "/verify-reset-code")
public class VerifyResetOtpServlet extends HttpServlet {
    private final AuthService authService = new AuthService();
    @Override protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (request.getSession(false) == null || request.getSession().getAttribute("pendingResetUserId") == null) { response.sendRedirect(request.getContextPath() + "/forgot-password"); return; }
        request.setAttribute("csrfToken", CsrfToken.getOrCreate(request.getSession()));
        request.getRequestDispatcher("/WEB-INF/views/auth/verify-reset.jsp").forward(request, response);
    }
    @Override protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!CsrfToken.isValid(request)) { response.sendError(403); return; }
        Object value = request.getSession().getAttribute("pendingResetUserId");
        if (!(value instanceof Long userId)) { response.sendRedirect(request.getContextPath() + "/forgot-password"); return; }
        try {
            OtpService.VerificationResult result = authService.verifyPasswordReset(userId, request.getParameter("code"));
            if (result == OtpService.VerificationResult.SUCCESS) { request.getSession().setAttribute("passwordResetAuthorized", Boolean.TRUE); response.sendRedirect(request.getContextPath() + "/reset-password"); return; }
            request.setAttribute("error", result == OtpService.VerificationResult.EXPIRED ? "This code has expired." : "The code is incorrect or unavailable.");
        } catch (SQLException exception) { getServletContext().log("Reset verification failed: " + exception.getClass().getName()); request.setAttribute("error", "Verification is temporarily unavailable."); }
        doGet(request, response);
    }
}
