package com.studenthub.controller;

import com.studenthub.service.DiscussionService;
import com.studenthub.util.CsrfToken;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name = "DiscussionsServlet", urlPatterns = "/discussions")
public class DiscussionsServlet extends HttpServlet {
    private final DiscussionService service = new DiscussionService();

    @Override protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long userId = (Long) request.getSession().getAttribute("userId");
        try {
            Long roomId = parsePositiveLong(request.getParameter("roomId"));
            request.setAttribute("room", service.load(userId, request.getParameter("scope"), roomId));
            if ("ADMIN".equals(String.valueOf(request.getSession().getAttribute("role")))) {
                request.setAttribute("moderationRooms", service.moderationRooms(userId));
                request.setAttribute("selectedRoomId", roomId);
                if (roomId == null) request.getSession().removeAttribute("selectedDiscussionRoomId");
                else request.getSession().setAttribute("selectedDiscussionRoomId", roomId);
            }
        } catch (SecurityException exception) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        } catch (SQLException exception) {
            getServletContext().log("Discussion load failed: " + exception.getClass().getName());
            request.setAttribute("error", "Discussions are temporarily unavailable.");
        }
        request.setAttribute("csrfToken", CsrfToken.getOrCreate(request.getSession()));
        Object flash = request.getSession().getAttribute("flash");
        if (flash != null) {
            request.setAttribute("message", flash);
            request.getSession().removeAttribute("flash");
        }
        Object flashError = request.getSession().getAttribute("flashError");
        if (flashError != null) {
            request.setAttribute("error", flashError);
            request.getSession().removeAttribute("flashError");
        }
        request.getRequestDispatcher("/WEB-INF/views/discussions/index.jsp").forward(request, response);
    }

    private Long parsePositiveLong(String value) {
        if (value == null || value.isBlank()) return null;
        try { long parsed = Long.parseLong(value); return parsed > 0 ? parsed : null; }
        catch (NumberFormatException exception) { return null; }
    }
}
