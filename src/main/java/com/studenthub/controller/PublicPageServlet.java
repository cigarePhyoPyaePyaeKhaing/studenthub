package com.studenthub.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@WebServlet(name = "PublicPageServlet", urlPatterns = {"/public-home", "/features", "/how-it-works", "/about"})
public class PublicPageServlet extends HttpServlet {
    private static final Map<String, String> VIEWS = Map.of(
            "/public-home", "home",
            "/features", "features",
            "/how-it-works", "how-it-works",
            "/about", "about");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String page = VIEWS.get(request.getServletPath());
        if (page == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        request.setAttribute("publicPage", page);
        request.getRequestDispatcher("/WEB-INF/views/public/" + page + ".jsp").forward(request, response);
    }
}
