package com.studenthub.controller;

import com.studenthub.service.AdminService;
import com.studenthub.util.AdminRequest;
import com.studenthub.util.CsrfToken;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name = "AdminDeleteUserServlet", urlPatterns = "/admin/users/delete")
public class AdminDeleteUserServlet extends HttpServlet {
    private final AdminService service = new AdminService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!AdminRequest.requireAdmin(request, response)) {
            return;
        }
        if (!CsrfToken.isValid(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        Long targetId = AdminRequest.positiveId(request.getParameter("userId"));
        if (targetId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        long actingAdminId = (Long) request.getSession().getAttribute("userId");
        Object actingRole = request.getSession().getAttribute("role");

        try {
            AdminService.OperationResult result = service.deleteStudent(actingAdminId, actingRole, targetId);
            if (result.successful()) {
                request.getSession().setAttribute("flash", result.message());
            } else {
                request.getSession().setAttribute("flashError", result.message());
            }
        } catch (SQLException exception) {
            getServletContext().log("Admin student deletion failed: " + exception.getClass().getName(), exception);
            request.getSession().setAttribute("flashError", "The student account could not be deleted right now.");
        }
        response.sendRedirect(request.getContextPath() + "/admin/users");
    }
}
