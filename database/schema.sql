CREATE DATABASE IF NOT EXISTS studenthub_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE studenthub_db;

CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NULL UNIQUE,
    student_id VARCHAR(8) NULL UNIQUE,
    email VARCHAR(120) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role ENUM('STUDENT', 'CR', 'ADMIN') NOT NULL DEFAULT 'STUDENT',
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    google_sub VARCHAR(255) NULL UNIQUE,
    university_id BIGINT NULL,
    university_locked BOOLEAN NOT NULL DEFAULT FALSE,
    academic_info_locked BOOLEAN NOT NULL DEFAULT FALSE,
    semester INT NULL,
    section_name VARCHAR(20) NULL,
    profile_image VARCHAR(255) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_users_university(university_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS verification_codes (
    code_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    email VARCHAR(120) NOT NULL,
    purpose ENUM('EMAIL_VERIFICATION', 'PASSWORD_RESET') NOT NULL,
    code_hash VARCHAR(255) NOT NULL,
    expires_at DATETIME NOT NULL,
    used_at DATETIME NULL,
    attempt_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_verification_codes_user FOREIGN KEY (user_id) REFERENCES users (user_id),
    INDEX idx_verification_codes_lookup (user_id, purpose, used_at, expires_at),
    INDEX idx_verification_codes_email_created (email, purpose, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS categories (
    category_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(50) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO categories (category_name) VALUES
    ('Assignment'), ('Exam'), ('General News'), ('Lecture Material'), ('Event')
ON DUPLICATE KEY UPDATE category_name = VALUES(category_name);

CREATE TABLE IF NOT EXISTS posts (
    post_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    category_id BIGINT NULL,
    title VARCHAR(200) NULL,
    content TEXT NOT NULL,
    image_url VARCHAR(500) NULL,
    visibility ENUM('SECTION', 'SEMESTER', 'ALL') NOT NULL DEFAULT 'ALL',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_posts_user FOREIGN KEY (user_id) REFERENCES users (user_id),
    CONSTRAINT fk_posts_category FOREIGN KEY (category_id) REFERENCES categories (category_id),
    INDEX idx_posts_user_id (user_id),
    INDEX idx_posts_category_id (category_id),
    INDEX idx_posts_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS deadlines (
    deadline_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NULL,
    title VARCHAR(200) NOT NULL,
    subject_name VARCHAR(100) NOT NULL,
    due_date DATETIME NOT NULL,
    semester INT NOT NULL,
    section_name VARCHAR(20) NULL,
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_deadlines_post FOREIGN KEY (post_id) REFERENCES posts (post_id),
    CONSTRAINT fk_deadlines_created_by FOREIGN KEY (created_by) REFERENCES users (user_id),
    INDEX idx_deadlines_due_date (due_date),
    INDEX idx_deadlines_semester (semester),
    INDEX idx_deadlines_post_id (post_id),
    INDEX idx_deadlines_created_by (created_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS comments (
    comment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_comments_post FOREIGN KEY (post_id) REFERENCES posts (post_id),
    CONSTRAINT fk_comments_user FOREIGN KEY (user_id) REFERENCES users (user_id),
    INDEX idx_comments_post_id (post_id),
    INDEX idx_comments_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS reactions (
    reaction_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    reaction_type ENUM('LIKE', 'LOVE', 'HELPFUL') NOT NULL DEFAULT 'LIKE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reactions_post FOREIGN KEY (post_id) REFERENCES posts (post_id),
    CONSTRAINT fk_reactions_user FOREIGN KEY (user_id) REFERENCES users (user_id),
    CONSTRAINT uq_reactions_post_user_type UNIQUE (post_id, user_id, reaction_type),
    INDEX idx_reactions_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS chat_rooms (
    room_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_name VARCHAR(100) NOT NULL,
    room_type ENUM('SECTION', 'SEMESTER', 'ALL', 'CR_SEMESTER', 'CR_ALL') NOT NULL,
    semester INT NULL,
    section_name VARCHAR(20) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_chat_rooms_scope (room_type, semester, section_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS messages (
    message_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    message TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_messages_room FOREIGN KEY (room_id) REFERENCES chat_rooms (room_id),
    CONSTRAINT fk_messages_sender FOREIGN KEY (sender_id) REFERENCES users (user_id),
    INDEX idx_messages_room_id (room_id),
    INDEX idx_messages_created_at (created_at),
    INDEX idx_messages_sender_id (sender_id),
    INDEX idx_messages_room_recent (room_id, created_at, message_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS notifications (
    notification_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    notification_type ENUM('ANNOUNCEMENT','DEADLINE','COMMENT','REACTION') NOT NULL,
    title VARCHAR(200) NOT NULL, message VARCHAR(500) NOT NULL, link_url VARCHAR(500) NOT NULL,
    actor_id BIGINT NOT NULL, target_user_id BIGINT NULL,
    visibility ENUM('ALL','SEMESTER','SECTION') NOT NULL,
    semester INT NULL, section_name VARCHAR(20) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notifications_actor FOREIGN KEY (actor_id) REFERENCES users(user_id),
    CONSTRAINT fk_notifications_target FOREIGN KEY (target_user_id) REFERENCES users(user_id),
    INDEX idx_notifications_created (created_at),
    INDEX idx_notifications_target (target_user_id, created_at),
    INDEX idx_notifications_scope (visibility, semester, section_name, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS notification_reads (
    notification_id BIGINT NOT NULL, user_id BIGINT NOT NULL,
    read_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (notification_id,user_id),
    CONSTRAINT fk_notification_reads_notification FOREIGN KEY (notification_id) REFERENCES notifications(notification_id) ON DELETE CASCADE,
    CONSTRAINT fk_notification_reads_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    INDEX idx_notification_reads_user (user_id,read_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS universities (
    university_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(180) NOT NULL UNIQUE,
    short_name VARCHAR(30) NULL UNIQUE,
    status ENUM('PENDING','APPROVED','REJECTED','INACTIVE') NOT NULL DEFAULT 'PENDING',
    requested_by BIGINT NULL, approved_by BIGINT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, approved_at DATETIME NULL,
    CONSTRAINT fk_university_requester FOREIGN KEY(requested_by) REFERENCES users(user_id) ON DELETE SET NULL,
    CONSTRAINT fk_university_approver FOREIGN KEY(approved_by) REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_university_status(status,name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO universities(name,short_name,status,approved_at)
VALUES('University of Information Technology','UIT','APPROVED',UTC_TIMESTAMP())
ON DUPLICATE KEY UPDATE name=VALUES(name);

SET @users_university_fk_exists = (
    SELECT COUNT(*) FROM information_schema.REFERENTIAL_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE() AND CONSTRAINT_NAME = 'fk_users_university'
);
SET @users_university_fk_sql = IF(@users_university_fk_exists = 0,
    'ALTER TABLE users ADD CONSTRAINT fk_users_university FOREIGN KEY(university_id) REFERENCES universities(university_id)',
    'SELECT 1');
PREPARE users_university_fk_stmt FROM @users_university_fk_sql;
EXECUTE users_university_fk_stmt;
DEALLOCATE PREPARE users_university_fk_stmt;

CREATE TABLE IF NOT EXISTS academic_change_requests (
    request_id BIGINT AUTO_INCREMENT PRIMARY KEY,user_id BIGINT NOT NULL,
    old_semester INT NULL,old_section VARCHAR(20) NULL,requested_semester INT NOT NULL,
    requested_section VARCHAR(20) NOT NULL,reason VARCHAR(1000) NOT NULL,
    status ENUM('PENDING','APPROVED','REJECTED') NOT NULL DEFAULT 'PENDING',
    pending_user_id BIGINT GENERATED ALWAYS AS(CASE WHEN status='PENDING' THEN user_id ELSE NULL END) STORED,
    reviewed_by BIGINT NULL,admin_note VARCHAR(1000) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,reviewed_at DATETIME NULL,
    CONSTRAINT fk_academic_user FOREIGN KEY(user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_academic_reviewer FOREIGN KEY(reviewed_by) REFERENCES users(user_id) ON DELETE SET NULL,
    UNIQUE KEY uq_academic_one_pending(pending_user_id),
    INDEX idx_academic_status(status,created_at),INDEX idx_academic_user(user_id,status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
