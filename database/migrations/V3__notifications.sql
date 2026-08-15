USE studenthub_db;

CREATE TABLE IF NOT EXISTS notifications (
    notification_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    notification_type ENUM('ANNOUNCEMENT','DEADLINE','COMMENT','REACTION') NOT NULL,
    title VARCHAR(200) NOT NULL,
    message VARCHAR(500) NOT NULL,
    link_url VARCHAR(500) NOT NULL,
    actor_id BIGINT NOT NULL,
    target_user_id BIGINT NULL,
    visibility ENUM('ALL','SEMESTER','SECTION') NOT NULL,
    semester INT NULL,
    section_name VARCHAR(20) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notifications_actor FOREIGN KEY (actor_id) REFERENCES users(user_id),
    CONSTRAINT fk_notifications_target FOREIGN KEY (target_user_id) REFERENCES users(user_id),
    INDEX idx_notifications_created (created_at),
    INDEX idx_notifications_target (target_user_id, created_at),
    INDEX idx_notifications_scope (visibility, semester, section_name, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS notification_reads (
    notification_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    read_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (notification_id, user_id),
    CONSTRAINT fk_notification_reads_notification FOREIGN KEY (notification_id)
        REFERENCES notifications(notification_id) ON DELETE CASCADE,
    CONSTRAINT fk_notification_reads_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    INDEX idx_notification_reads_user (user_id, read_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
