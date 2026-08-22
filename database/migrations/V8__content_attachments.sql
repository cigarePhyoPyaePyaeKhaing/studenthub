USE studenthub_db;
CREATE TABLE IF NOT EXISTS attachments (
 attachment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
 post_id BIGINT NULL, comment_id BIGINT NULL, message_id BIGINT NULL,
 original_filename VARCHAR(180) NOT NULL, storage_key VARCHAR(80) NOT NULL UNIQUE,
 mime_type VARCHAR(120) NOT NULL, file_size BIGINT NOT NULL, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 CONSTRAINT fk_attachments_post FOREIGN KEY(post_id) REFERENCES posts(post_id) ON DELETE CASCADE,
 CONSTRAINT fk_attachments_comment FOREIGN KEY(comment_id) REFERENCES comments(comment_id) ON DELETE CASCADE,
 CONSTRAINT fk_attachments_message FOREIGN KEY(message_id) REFERENCES messages(message_id) ON DELETE CASCADE,
 CONSTRAINT ck_attachments_one_owner CHECK ((post_id IS NOT NULL)+(comment_id IS NOT NULL)+(message_id IS NOT NULL)=1),
 UNIQUE KEY uq_attachment_post(post_id), UNIQUE KEY uq_attachment_comment(comment_id), UNIQUE KEY uq_attachment_message(message_id),
 INDEX idx_attachments_created(created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
