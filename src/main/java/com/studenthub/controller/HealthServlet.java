package com.studenthub.controller;

import com.studenthub.util.DBConnection;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

@WebServlet(name = "HealthServlet", urlPatterns = "/health")
public class HealthServlet extends HttpServlet {
    private static final ObjectMapper JSON = new ObjectMapper();
    @Override protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT 1");
             ResultSet results = statement.executeQuery()) {
            if (results.next() && results.getInt(1) == 1) {
                response.setStatus(200);
                Object version = getServletContext().getAttribute("buildVersion");
                JSON.writeValue(response.getWriter(), Map.of("status", "ok", "version", String.valueOf(version)));
                return;
            }
        } catch (Exception exception) {
            getServletContext().log("Health check failed: " + exception.getClass().getName());
        }
        response.setStatus(503); response.getWriter().write("{\"status\":\"unavailable\"}");
    }
}
