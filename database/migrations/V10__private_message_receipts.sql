ALTER TABLE private_messages ADD COLUMN delivered_at TIMESTAMP NULL AFTER created_at;
ALTER TABLE private_messages ADD COLUMN seen_at TIMESTAMP NULL AFTER delivered_at;
CREATE INDEX idx_private_message_receipts ON private_messages(conversation_id,sender_id,seen_at,delivered_at,message_id);
