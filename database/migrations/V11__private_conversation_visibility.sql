CREATE TABLE IF NOT EXISTS private_conversation_visibility (
  conversation_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  deleted_at TIMESTAMP NULL,
  PRIMARY KEY(conversation_id,user_id),
  CONSTRAINT fk_private_visibility_conversation FOREIGN KEY(conversation_id) REFERENCES private_conversations(conversation_id) ON DELETE CASCADE,
  CONSTRAINT fk_private_visibility_user FOREIGN KEY(user_id) REFERENCES users(user_id) ON DELETE CASCADE,
  INDEX idx_private_visibility_user(user_id,deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
