package com.studenthub.controller;

import com.studenthub.service.DiscussionService;
import com.studenthub.util.CsrfToken;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

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
            boolean admin = "ADMIN".equals(String.valueOf(session.getAttribute("role")));
            String moderationScope = admin ? canonicalModerationScope(request) : request.getParameter("moderationScope");
            request.setAttribute("room", service.load(userId, request.getParameter("scope"), roomId, moderationScope));
            if (admin) {
                List<DiscussionService.ModerationScopeOption> options = service.moderationRooms(userId);
                request.setAttribute("moderationRooms", options);
                request.setAttribute("moderationSemesters", options.stream()
                        .filter(option -> "SEMESTERS".equals(option.group())).toList());
                request.setAttribute("moderationSections", options.stream()
                        .filter(option -> "SECTIONS".equals(option.group())).toList());
                request.setAttribute("sectionSemesters", options.stream()
                        .filter(option -> "SECTIONS".equals(option.group()))
                        .map(DiscussionService.ModerationScopeOption::semester).distinct().sorted().toList());
                DiscussionService.ModerationScopeOption selected = options.stream()
                        .filter(option -> option.key().equalsIgnoreCase(moderationScope)).findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("INVALID_MODERATION_SCOPE"));
                request.setAttribute("selectedModerationScope", selected.key());
                request.setAttribute("selectedModerationSemester", selected.semester());
                request.setAttribute("selectedModerationSection", selected.sectionName());
                session.setAttribute("selectedDiscussionScope", selected.key());
            }
        } catch (IllegalArgumentException exception) {
            session.setAttribute("flashError", "MISSING_SECTION_SELECTION".equals(exception.getMessage())
                    ? "Select a semester and section to continue."
                    : "That discussion scope is no longer available.");
            response.sendRedirect(request.getContextPath() + "/discussions");
            return;
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

    static String canonicalModerationScope(HttpServletRequest request) {
        String direct = request.getParameter("moderationScope");
        if (direct != null && !direct.isBlank()) return direct.trim();
        if (!"section".equalsIgnoreCase(request.getParameter("moderationMode"))) return "all_students";
        String semester = request.getParameter("sectionSemester");
        String section = request.getParameter("sectionName");
        if (semester == null || semester.isBlank() || section == null || section.isBlank()) {
            throw new IllegalArgumentException("MISSING_SECTION_SELECTION");
        }
        try {
            int parsedSemester = Integer.parseInt(semester.trim());
            if (parsedSemester < 1) throw new NumberFormatException();
            return "section:" + parsedSemester + ":" + section.trim().toUpperCase(java.util.Locale.ROOT);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("INVALID_MODERATION_SCOPE");
        }
    }
}
