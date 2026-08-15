package com.studenthub.controller;

import com.studenthub.util.DBConnection;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet(name = "DatabaseTestServlet", urlPatterns = "/db-test")
public class DatabaseTestServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT 1");
             ResultSet resultSet = statement.executeQuery()) {

            if (!resultSet.next() || resultSet.getInt(1) != 1) {
                throw new SQLException("Database health check returned an unexpected result.");
            }

            response.setStatus(HttpServletResponse.SC_OK);
            writePage(response.getWriter(), true);
        } catch (SQLException | RuntimeException exception) {
            logDiagnosticDetails(exception);
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            writePage(response.getWriter(), false);
        }
    }

    private void logDiagnosticDetails(Exception exception) {
        getServletContext().log("StudentHub database health check failed.");
        getServletContext().log("STUDENTHUB_DB_URL present: " + isEnvironmentVariablePresent("STUDENTHUB_DB_URL"));
        getServletContext().log("STUDENTHUB_DB_USER present: " + isEnvironmentVariablePresent("STUDENTHUB_DB_USER"));
        getServletContext().log("STUDENTHUB_DB_PASSWORD present: "
                + isEnvironmentVariablePresent("STUDENTHUB_DB_PASSWORD"));
        getServletContext().log("Exception class: " + exception.getClass().getName());

        if (exception instanceof SQLException sqlException) {
            getServletContext().log("SQLState: " + safeLogValue(sqlException.getSQLState()));
            getServletContext().log("Vendor error code: " + sqlException.getErrorCode());
        }

        getServletContext().log("Exception message: " + safeLogValue(exception.getMessage()));
    }

    private boolean isEnvironmentVariablePresent(String variableName) {
        String value = System.getenv(variableName);
        return value != null && !value.isBlank();
    }

    private String safeLogValue(String value) {
        if (value == null) {
            return "(none)";
        }
        return value.replace('\r', ' ').replace('\n', ' ');
    }

    private void writePage(PrintWriter writer, boolean successful) {
        String heading = successful
                ? "Database connection successful."
                : "Database connection could not be established.";
        String detail = successful
                ? "MySQL is connected to StudentHub."
                : "Check the server database configuration and try again.";
        String statusClass = successful ? "success" : "error";

        writer.println("""
                <!doctype html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1">
                    <title>StudentHub Database</title>
                    <style>
                        * { box-sizing: border-box; }
                        body { margin: 0; min-height: 100vh; display: grid; place-items: center;
                               padding: 1.5rem; font-family: system-ui, sans-serif; color: #172554;
                               background: linear-gradient(135deg, #f8fafc, #eff6ff); }
                        main { width: min(100%%, 38rem); padding: 2.5rem; border-radius: 1.25rem;
                               background: #fff; box-shadow: 0 1.25rem 3rem rgba(30, 64, 175, .13); }
                        h1 { margin-top: 0; font-size: clamp(1.8rem, 5vw, 2.7rem); }
                        .status { margin-top: 1.5rem; padding: 1rem 1.25rem; border-radius: .8rem; }
                        .success { color: #166534; background: #f0fdf4; }
                        .error { color: #991b1b; background: #fef2f2; }
                        p { line-height: 1.6; }
                    </style>
                </head>
                <body>
                    <main>
                        <h1>StudentHub Database</h1>
                        <section class="status %s" role="status">
                            <strong>%s</strong>
                            <p>%s</p>
                        </section>
                    </main>
                </body>
                </html>
                """.formatted(statusClass, heading, detail));
    }
}
