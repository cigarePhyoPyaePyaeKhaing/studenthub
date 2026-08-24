ALTER TABLE chat_rooms ADD COLUMN university_id BIGINT NULL AFTER room_type;
ALTER TABLE chat_rooms ADD CONSTRAINT fk_chat_rooms_university FOREIGN KEY(university_id) REFERENCES universities(university_id);
CREATE INDEX idx_chat_rooms_university_scope ON chat_rooms(university_id,room_type,semester,section_name);

CREATE TABLE IF NOT EXISTS private_conversations (
  conversation_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user1_id BIGINT NOT NULL,
  user2_id BIGINT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_private_conversation_user1 FOREIGN KEY(user1_id) REFERENCES users(user_id) ON DELETE CASCADE,
  CONSTRAINT fk_private_conversation_user2 FOREIGN KEY(user2_id) REFERENCES users(user_id) ON DELETE CASCADE,
  CONSTRAINT ck_private_conversation_order CHECK(user1_id < user2_id),
  CONSTRAINT uq_private_conversation_pair UNIQUE(user1_id,user2_id),
  INDEX idx_private_conversation_updated(updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS private_messages (
  message_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  conversation_id BIGINT NOT NULL,
  sender_id BIGINT NOT NULL,
  client_message_id CHAR(36) NOT NULL,
  message TEXT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_private_message_conversation FOREIGN KEY(conversation_id) REFERENCES private_conversations(conversation_id) ON DELETE CASCADE,
  CONSTRAINT fk_private_message_sender FOREIGN KEY(sender_id) REFERENCES users(user_id) ON DELETE CASCADE,
  CONSTRAINT uq_private_message_client UNIQUE(sender_id,client_message_id),
  INDEX idx_private_message_recent(conversation_id,message_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS private_message_reads (
  conversation_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  last_read_message_id BIGINT NULL,
  read_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY(conversation_id,user_id),
  CONSTRAINT fk_private_read_conversation FOREIGN KEY(conversation_id) REFERENCES private_conversations(conversation_id) ON DELETE CASCADE,
  CONSTRAINT fk_private_read_user FOREIGN KEY(user_id) REFERENCES users(user_id) ON DELETE CASCADE,
  CONSTRAINT fk_private_read_message FOREIGN KEY(last_read_message_id) REFERENCES private_messages(message_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS private_message_attachments (
  attachment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  message_id BIGINT NOT NULL UNIQUE,
  original_filename VARCHAR(180) NOT NULL,
  storage_key VARCHAR(80) NOT NULL UNIQUE,
  mime_type VARCHAR(120) NOT NULL,
  file_size BIGINT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_private_attachment_message FOREIGN KEY(message_id) REFERENCES private_messages(message_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
