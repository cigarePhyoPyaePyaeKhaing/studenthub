package com.studenthub.util;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.SessionCookieConfig;
import jakarta.servlet.annotation.WebListener;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebListener
public class ApplicationConfigurationListener implements ServletContextListener {
    private static final Logger LOGGER = Logger.getLogger(ApplicationConfigurationListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent event) {
        ServletContext context = event.getServletContext();
        SessionCookieConfig cookie = context.getSessionCookieConfig();
        cookie.setHttpOnly(true);
        cookie.setSecure("production".equalsIgnoreCase(System.getenv("APP_ENV")));
        cookie.setName("STUDENTHUB_SESSION");

        ensureTablesExist(context);
    }

    private void ensureTablesExist(ServletContext context) {
        String createSql = """
                CREATE TABLE IF NOT EXISTS academic_change_requests (
                    request_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    user_id BIGINT NOT NULL,
                    old_semester INT NULL,
                    old_section VARCHAR(20) NULL,
                    requested_semester INT NOT NULL,
                    requested_section VARCHAR(20) NOT NULL,
                    reason VARCHAR(1000) NOT NULL,
                    status ENUM('PENDING','APPROVED','REJECTED') NOT NULL DEFAULT 'PENDING',
                    pending_user_id BIGINT GENERATED ALWAYS AS(CASE WHEN status='PENDING' THEN user_id ELSE NULL END) STORED,
                    reviewed_by BIGINT NULL,
                    admin_note VARCHAR(1000) NULL,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    reviewed_at DATETIME NULL,
                    CONSTRAINT fk_academic_user FOREIGN KEY(user_id) REFERENCES users(user_id),
                    CONSTRAINT fk_academic_reviewer FOREIGN KEY(reviewed_by) REFERENCES users(user_id) ON DELETE SET NULL,
                    UNIQUE KEY uq_academic_one_pending(pending_user_id),
                    INDEX idx_academic_status(status,created_at),
                    INDEX idx_academic_user(user_id,status)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                """;
        try (Connection connection = DBConnection.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(createSql);
            context.log("Database tables verified/initialized: academic_change_requests");
        } catch (SQLException e) {
            context.log("Database table initialization check: " + e.getClass().getName() + ": " + e.getMessage());
            LOGGER.log(Level.INFO, "Database table initialization check: {0}", e.getMessage());
        }
    }
}