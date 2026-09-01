-- Additive scope extension for CR - Admin discussion room.
ALTER TABLE chat_rooms
    MODIFY room_type ENUM('SECTION', 'SEMESTER', 'ALL', 'CR_SEMESTER', 'CR_ALL', 'CR_ADMIN') NOT NULL;