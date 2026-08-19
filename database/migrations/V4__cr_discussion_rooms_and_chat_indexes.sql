-- Additive scope extension for the existing discussion-room model.
ALTER TABLE chat_rooms
    MODIFY room_type ENUM('SECTION', 'SEMESTER', 'ALL', 'CR_SEMESTER', 'CR_ALL') NOT NULL;

-- Supports room lookup/create by the exact scope tuple used by DiscussionDAO.
CREATE INDEX idx_chat_rooms_scope
    ON chat_rooms (room_type, semester, section_name);

-- Supports bounded newest-first message retrieval without sorting all room history.
CREATE INDEX idx_messages_room_recent
    ON messages (room_id, created_at, message_id);
