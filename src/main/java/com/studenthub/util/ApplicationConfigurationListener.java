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

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        DBConnection.closeDataSource();
    }

    private void ensureTablesExist(ServletContext context) {
        String createAcademicRequestsSql = """
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

        String createUniversitiesSql = """
                CREATE TABLE IF NOT EXISTS universities (
                    university_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(180) NOT NULL UNIQUE,
                    short_name VARCHAR(30) NULL UNIQUE,
                    status ENUM('PENDING','APPROVED','REJECTED','INACTIVE') NOT NULL DEFAULT 'APPROVED',
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    INDEX idx_university_status(status, name)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                """;

        String insertDefaultUniversitySql = """
                INSERT INTO universities (name, short_name, status)
                VALUES ('University of Information Technology', 'UIT', 'APPROVED')
                ON DUPLICATE KEY UPDATE name = VALUES(name)
                """;

        try (Connection connection = DBConnection.getConnection();
             Statement statement = connection.createStatement()) {

            statement.execute(createAcademicRequestsSql);
            context.log("Database tables verified/initialized: academic_change_requests");

            statement.execute(createUniversitiesSql);
            statement.execute(insertDefaultUniversitySql);
            context.log("Database tables verified/initialized: universities (UIT)");

            ensureUserUniversityColumn(connection, context);
            ensureNotificationTypeColumn(connection, context);

        } catch (SQLException e) {
            context.log("Database table initialization check: " + e.getClass().getName() + ": " + e.getMessage());
            LOGGER.log(Level.INFO, "Database table initialization check: {0}", e.getMessage());
        }
    }

    private void ensureUserUniversityColumn(Connection connection, ServletContext context) {
        try {
            boolean hasColumn = false;
            var metaData = connection.getMetaData();
            try (var rs = metaData.getColumns(null, null, "users", "university_id")) {
                if (rs.next()) {
                    hasColumn = true;
                }
            }
            if (!hasColumn) {
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("ALTER TABLE users ADD COLUMN university_id BIGINT NULL");
                    context.log("Added column users.university_id");
                }
            }
        } catch (Exception e) {
            context.log("ensureUserUniversityColumn check: " + e.getMessage());
        }
    }

    private void ensureNotificationTypeColumn(Connection connection, ServletContext context) {
        try {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("ALTER TABLE notifications MODIFY COLUMN notification_type VARCHAR(50) NOT NULL");
                context.log("Ensured notifications.notification_type is VARCHAR(50)");
            }
        } catch (Exception e) {
            context.log("ensureNotificationTypeColumn check: " + e.getMessage());
        }
    }
}