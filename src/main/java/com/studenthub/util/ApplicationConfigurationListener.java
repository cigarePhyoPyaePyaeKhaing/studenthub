package com.studenthub.util;

import com.studenthub.dao.CategoryDAO;
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
        String commit = firstNonBlank(System.getenv("RAILWAY_GIT_COMMIT_SHA"), System.getenv("BUILD_VERSION"), "dev");
        context.setAttribute("buildVersion", commit);
        context.setAttribute("assetVersion", commit.length() > 12 ? commit.substring(0, 12) : commit);

        ProfilePhotoStorage profileStorage = new ProfilePhotoStorage();
        boolean profileStorageConfigured = profileStorage.isConfigured();
        boolean profileStorageWritable = profileStorage.ensureWritable();
        context.setAttribute("profileStorageConfigured", profileStorageConfigured);
        context.setAttribute("profileStorageWritable", profileStorageWritable);
        if (!profileStorageWritable) {
            context.log("Profile photo uploads unavailable: persistent storage is not configured or writable.");
        }
        AttachmentStorage attachmentStorage = new AttachmentStorage();
        boolean attachmentStorageConfigured = attachmentStorage.isConfigured();
        boolean attachmentStorageWritable = attachmentStorage.ensureWritable();
        context.setAttribute("attachmentStorageConfigured", attachmentStorageConfigured);
        context.setAttribute("attachmentStorageWritable", attachmentStorageWritable);
        if (!attachmentStorageWritable) context.log("Attachment uploads unavailable: persistent storage is not configured or writable.");

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
            ensureUserProfileImageColumn(connection, context);
            ensureUserPresenceColumn(connection, context);
            ensureNotificationTypeColumn(connection, context);
            ensurePostDeadlineColumn(connection, context);
            ensureStandardCategories(connection, context);
            ensureAttachmentsTable(connection, context);
            ensureDiscussionUniversityColumn(connection, context);
            ensurePrivateMessagingTables(connection, context);
            ensurePrivateMessageReceipts(connection, context);
            ensurePrivateConversationVisibility(connection, context);

        } catch (SQLException e) {
            context.log("Database table initialization check: " + e.getClass().getName() + ": " + e.getMessage());
            LOGGER.log(Level.INFO, "Database table initialization check: {0}", e.getMessage());
        }
    }
    private void ensurePrivateConversationVisibility(Connection connection,ServletContext context){String sql="CREATE TABLE IF NOT EXISTS private_conversation_visibility(conversation_id BIGINT NOT NULL,user_id BIGINT NOT NULL,deleted_at TIMESTAMP NULL,PRIMARY KEY(conversation_id,user_id),CONSTRAINT fk_private_visibility_conversation FOREIGN KEY(conversation_id) REFERENCES private_conversations(conversation_id) ON DELETE CASCADE,CONSTRAINT fk_private_visibility_user FOREIGN KEY(user_id) REFERENCES users(user_id) ON DELETE CASCADE,INDEX idx_private_visibility_user(user_id,deleted_at)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";try(Statement statement=connection.createStatement()){statement.execute(sql);}catch(SQLException exception){context.log("ensurePrivateConversationVisibility check: "+exception.getClass().getName());}}

    private void ensurePrivateMessageReceipts(Connection connection, ServletContext context) {
        try {
            boolean delivered;try(var columns=connection.getMetaData().getColumns(null,null,"private_messages","delivered_at")){delivered=columns.next();}
            if(!delivered)try(Statement statement=connection.createStatement()){statement.execute("ALTER TABLE private_messages ADD COLUMN delivered_at TIMESTAMP NULL AFTER created_at");statement.execute("ALTER TABLE private_messages ADD COLUMN seen_at TIMESTAMP NULL AFTER delivered_at");statement.execute("CREATE INDEX idx_private_message_receipts ON private_messages(conversation_id,sender_id,seen_at,delivered_at,message_id)");}
        }catch(SQLException exception){context.log("ensurePrivateMessageReceipts check: "+exception.getClass().getName());}
    }

    private void ensureDiscussionUniversityColumn(Connection connection, ServletContext context) {
        try {
            boolean exists; try (var columns=connection.getMetaData().getColumns(null,null,"chat_rooms","university_id")){exists=columns.next();}
            if(!exists) try(Statement statement=connection.createStatement()){
                statement.execute("ALTER TABLE chat_rooms ADD COLUMN university_id BIGINT NULL AFTER room_type");
                statement.execute("ALTER TABLE chat_rooms ADD CONSTRAINT fk_chat_rooms_university FOREIGN KEY(university_id) REFERENCES universities(university_id)");
                statement.execute("CREATE INDEX idx_chat_rooms_university_scope ON chat_rooms(university_id,room_type,semester,section_name)");
            }
        } catch(SQLException exception){context.log("ensureDiscussionUniversityColumn check: "+exception.getClass().getName());}
    }

    private void ensurePrivateMessagingTables(Connection connection, ServletContext context) {
        String[] statements = {
            "CREATE TABLE IF NOT EXISTS private_conversations (conversation_id BIGINT AUTO_INCREMENT PRIMARY KEY,user1_id BIGINT NOT NULL,user2_id BIGINT NOT NULL,created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,CONSTRAINT fk_private_conversation_user1 FOREIGN KEY(user1_id) REFERENCES users(user_id) ON DELETE CASCADE,CONSTRAINT fk_private_conversation_user2 FOREIGN KEY(user2_id) REFERENCES users(user_id) ON DELETE CASCADE,CONSTRAINT ck_private_conversation_order CHECK(user1_id<user2_id),CONSTRAINT uq_private_conversation_pair UNIQUE(user1_id,user2_id),INDEX idx_private_conversation_updated(updated_at)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci",
            "CREATE TABLE IF NOT EXISTS private_messages (message_id BIGINT AUTO_INCREMENT PRIMARY KEY,conversation_id BIGINT NOT NULL,sender_id BIGINT NOT NULL,client_message_id CHAR(36) NOT NULL,message TEXT NULL,created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,CONSTRAINT fk_private_message_conversation FOREIGN KEY(conversation_id) REFERENCES private_conversations(conversation_id) ON DELETE CASCADE,CONSTRAINT fk_private_message_sender FOREIGN KEY(sender_id) REFERENCES users(user_id) ON DELETE CASCADE,CONSTRAINT uq_private_message_client UNIQUE(sender_id,client_message_id),INDEX idx_private_message_recent(conversation_id,message_id)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci",
            "CREATE TABLE IF NOT EXISTS private_message_reads (conversation_id BIGINT NOT NULL,user_id BIGINT NOT NULL,last_read_message_id BIGINT NULL,read_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,PRIMARY KEY(conversation_id,user_id),CONSTRAINT fk_private_read_conversation FOREIGN KEY(conversation_id) REFERENCES private_conversations(conversation_id) ON DELETE CASCADE,CONSTRAINT fk_private_read_user FOREIGN KEY(user_id) REFERENCES users(user_id) ON DELETE CASCADE,CONSTRAINT fk_private_read_message FOREIGN KEY(last_read_message_id) REFERENCES private_messages(message_id) ON DELETE SET NULL) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci",
            "CREATE TABLE IF NOT EXISTS private_message_attachments (attachment_id BIGINT AUTO_INCREMENT PRIMARY KEY,message_id BIGINT NOT NULL UNIQUE,original_filename VARCHAR(180) NOT NULL,storage_key VARCHAR(80) NOT NULL UNIQUE,mime_type VARCHAR(120) NOT NULL,file_size BIGINT NOT NULL,created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,CONSTRAINT fk_private_attachment_message FOREIGN KEY(message_id) REFERENCES private_messages(message_id) ON DELETE CASCADE) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci"
        };
        try (Statement statement = connection.createStatement()) { for (String sql : statements) statement.execute(sql); context.log("Database tables verified/initialized: private messaging"); }
        catch (SQLException exception) { context.log("ensurePrivateMessagingTables check: " + exception.getClass().getName()); }
    }

    private void ensureAttachmentsTable(Connection connection, ServletContext context) {
        String sql = """
                CREATE TABLE IF NOT EXISTS attachments (
                  attachment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                  post_id BIGINT NULL, comment_id BIGINT NULL, message_id BIGINT NULL,
                  original_filename VARCHAR(180) NOT NULL, storage_key VARCHAR(80) NOT NULL UNIQUE,
                  mime_type VARCHAR(120) NOT NULL, file_size BIGINT NOT NULL,
                  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                  CONSTRAINT fk_attachments_post FOREIGN KEY(post_id) REFERENCES posts(post_id) ON DELETE CASCADE,
                  CONSTRAINT fk_attachments_comment FOREIGN KEY(comment_id) REFERENCES comments(comment_id) ON DELETE CASCADE,
                  CONSTRAINT fk_attachments_message FOREIGN KEY(message_id) REFERENCES messages(message_id) ON DELETE CASCADE,
                  CONSTRAINT ck_attachments_one_owner CHECK ((post_id IS NOT NULL)+(comment_id IS NOT NULL)+(message_id IS NOT NULL)=1),
                  UNIQUE KEY uq_attachment_post(post_id), UNIQUE KEY uq_attachment_comment(comment_id),
                  UNIQUE KEY uq_attachment_message(message_id), INDEX idx_attachments_created(created_at)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                """;
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
            context.log("Database tables verified/initialized: attachments");
        } catch (SQLException exception) {
            context.log("ensureAttachmentsTable check: " + exception.getClass().getName());
        }
    }

    private String firstNonBlank(String... values) {
        for (String value : values) if (value != null && !value.isBlank()) return value.trim();
        return "dev";
    }

    private void ensureUserPresenceColumn(Connection connection, ServletContext context) {
        try {
            boolean hasColumn;
            try (var results = connection.getMetaData().getColumns(null, null, "users", "last_active_at")) {
                hasColumn = results.next();
            }
            if (!hasColumn) {
                try (Statement statement = connection.createStatement()) {
                    statement.execute("ALTER TABLE users ADD COLUMN last_active_at DATETIME NULL");
                    context.log("Added column users.last_active_at");
                }
            }
        } catch (Exception exception) {
            context.log("ensureUserPresenceColumn check: " + exception.getClass().getName());
        }
    }

    private void ensurePostDeadlineColumn(Connection connection, ServletContext context) {
        try {
            boolean hasColumn = false;
            var metaData = connection.getMetaData();
            try (var rs = metaData.getColumns(null, null, "posts", "deadline_date")) {
                if (rs.next()) {
                    hasColumn = true;
                }
            }
            if (!hasColumn) {
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("ALTER TABLE posts ADD COLUMN deadline_date DATETIME NULL");
                    context.log("Added column posts.deadline_date");
                }
            }
        } catch (Exception e) {
            context.log("ensurePostDeadlineColumn check: " + e.getMessage());
        }
    }

    private void ensureStandardCategories(Connection connection, ServletContext context) {
        String[] categories = {
                "Assignment",
                "Tutorial",
                "Exam",
                "Event",
                "General News",
                "Lecture Material"
        };
        String checkSql = "SELECT category_id FROM categories WHERE category_name = ?";
        String insertSql = "INSERT INTO categories (category_name) VALUES (?)";
        try {
            for (String categoryName : categories) {
                boolean exists = false;
                try (java.sql.PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
                    checkStmt.setString(1, categoryName);
                    try (var rs = checkStmt.executeQuery()) {
                        if (rs.next()) {
                            exists = true;
                        }
                    }
                }
                if (!exists) {
                    try (java.sql.PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
                        insertStmt.setString(1, categoryName);
                        insertStmt.executeUpdate();
                        context.log("Inserted standard category: " + categoryName);
                    }
                }
            }
            CategoryDAO.invalidateCache();
        } catch (Exception e) {
            context.log("ensureStandardCategories check: " + e.getMessage());
        }
    }

    private void ensureUserUniversityColumn(Connection connection, ServletContext context) {
        try {
            var metaData = connection.getMetaData();
            boolean hasUniv = false, hasUnivLock = false, hasAcadLock = false;
            try (var rs = metaData.getColumns(null, null, "users", "university_id")) {
                if (rs.next()) hasUniv = true;
            }
            try (var rs = metaData.getColumns(null, null, "users", "university_locked")) {
                if (rs.next()) hasUnivLock = true;
            }
            try (var rs = metaData.getColumns(null, null, "users", "academic_info_locked")) {
                if (rs.next()) hasAcadLock = true;
            }
            try (Statement stmt = connection.createStatement()) {
                if (!hasUniv) {
                    stmt.execute("ALTER TABLE users ADD COLUMN university_id BIGINT NULL");
                    context.log("Added column users.university_id");
                }
                if (!hasUnivLock) {
                    stmt.execute("ALTER TABLE users ADD COLUMN university_locked BOOLEAN NOT NULL DEFAULT FALSE");
                    context.log("Added column users.university_locked");
                }
                if (!hasAcadLock) {
                    stmt.execute("ALTER TABLE users ADD COLUMN academic_info_locked BOOLEAN NOT NULL DEFAULT FALSE");
                    context.log("Added column users.academic_info_locked");
                }
            }
        } catch (Exception e) {
            context.log("ensureUserUniversityColumn check: " + e.getMessage());
        }
    }

    private void ensureUserProfileImageColumn(Connection connection, ServletContext context) {
        try {
            boolean hasColumn = false;
            var metaData = connection.getMetaData();
            try (var rs = metaData.getColumns(null, null, "users", "profile_image")) {
                if (rs.next()) hasColumn = true;
            }
            if (!hasColumn) {
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("ALTER TABLE users ADD COLUMN profile_image VARCHAR(255) NULL");
                    context.log("Added column users.profile_image");
                }
            }
        } catch (Exception e) {
            context.log("ensureUserProfileImageColumn check: " + e.getMessage());
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
