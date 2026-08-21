-- Add required categories for announcements and deadlines
INSERT INTO categories (category_name) VALUES
    ('General'), ('Assignment'), ('Tutorial'), ('Exam')
ON DUPLICATE KEY UPDATE category_name = VALUES(category_name);

-- Support ACADEMIC_CHANGE notification types
ALTER TABLE notifications
    MODIFY notification_type ENUM('ANNOUNCEMENT', 'DEADLINE', 'COMMENT', 'REACTION', 'ACADEMIC_CHANGE') NOT NULL;

-- Create attachments table for announcements and discussions
CREATE TABLE IF NOT EXISTS attachments (
    attachment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    entity_type ENUM('POST', 'MESSAGE') NOT NULL,
    entity_id BIGINT NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    stored_filename VARCHAR(255) NOT NULL,
    file_type ENUM('IMAGE', 'DOCUMENT', 'VIDEO') NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    uploader_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_attachments_uploader FOREIGN KEY (uploader_id) REFERENCES users (user_id) ON DELETE CASCADE,
    INDEX idx_attachments_entity (entity_type, entity_id),
    INDEX idx_attachments_uploader (uploader_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
