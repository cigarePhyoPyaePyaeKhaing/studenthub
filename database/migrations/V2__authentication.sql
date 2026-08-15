USE studenthub_db;

DROP PROCEDURE IF EXISTS migrate_studenthub_auth;
DELIMITER $$
CREATE PROCEDURE migrate_studenthub_auth()
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'users' AND column_name = 'student_id'
    ) THEN
        ALTER TABLE users ADD COLUMN student_id VARCHAR(8) NULL AFTER username;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'users' AND column_name = 'email_verified'
    ) THEN
        ALTER TABLE users ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT FALSE AFTER role;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'users' AND column_name = 'google_sub'
    ) THEN
        ALTER TABLE users ADD COLUMN google_sub VARCHAR(255) NULL AFTER email_verified;
    END IF;

    ALTER TABLE users
        MODIFY username VARCHAR(50) NULL,
        MODIFY semester INT NULL,
        MODIFY section_name VARCHAR(20) NULL;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.statistics
        WHERE table_schema = DATABASE() AND table_name = 'users' AND index_name = 'uq_users_student_id'
    ) THEN
        ALTER TABLE users ADD CONSTRAINT uq_users_student_id UNIQUE (student_id);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.statistics
        WHERE table_schema = DATABASE() AND table_name = 'users' AND index_name = 'uq_users_google_sub'
    ) THEN
        ALTER TABLE users ADD CONSTRAINT uq_users_google_sub UNIQUE (google_sub);
    END IF;
END$$
DELIMITER ;

CALL migrate_studenthub_auth();
DROP PROCEDURE migrate_studenthub_auth;

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
