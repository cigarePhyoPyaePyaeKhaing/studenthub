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
        HttpSession session = request.getSession(false);
        Object sessionUserId = session == null ? null : session.getAttribute("userId");
        if (!(sessionUserId instanceof Number number)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        long userId = number.longValue();
        try {
            Long roomId = parsePositiveLong(request.getParameter("roomId"));
            request.setAttribute("room", service.load(userId, request.getParameter("scope"), roomId));
            if ("ADMIN".equals(String.valueOf(session.getAttribute("role")))) {
                request.setAttribute("moderationRooms", service.moderationRooms(userId));
                request.setAttribute("selectedRoomId", roomId);
                if (roomId == null) session.removeAttribute("selectedDiscussionRoomId");
                else session.setAttribute("selectedDiscussionRoomId", roomId);
            }
        } catch (SecurityException exception) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        } catch (SQLException exception) {
            getServletContext().log("Discussion load failed: " + exception.getClass().getName());
            request.setAttribute("error", "Discussions are temporarily unavailable.");
        }
        request.setAttribute("csrfToken", CsrfToken.getOrCreate(session));
        Object flash = session.getAttribute("flash");
        if (flash != null) {
            request.setAttribute("message", flash);
            session.removeAttribute("flash");
        }
        Object flashError = session.getAttribute("flashError");
        if (flashError != null) {
            request.setAttribute("error", flashError);
            session.removeAttribute("flashError");
        }
        request.getRequestDispatcher("/WEB-INF/views/discussions/index.jsp").forward(request, response);
    }

    private Long parsePositiveLong(String value) {
        if (value == null || value.isBlank()) return null;
        try { long parsed = Long.parseLong(value); return parsed > 0 ? parsed : null; }
        catch (NumberFormatException exception) { return null; }
    }
}
