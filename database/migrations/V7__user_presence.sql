USE studenthub_db;

DROP PROCEDURE IF EXISTS migrate_studenthub_user_presence;
DELIMITER $$
CREATE PROCEDURE migrate_studenthub_user_presence()
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'users' AND column_name = 'last_active_at'
    ) THEN
        ALTER TABLE users ADD COLUMN last_active_at DATETIME NULL;
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.statistics
        WHERE table_schema = DATABASE() AND table_name = 'users' AND index_name = 'idx_users_last_active'
    ) THEN
        CREATE INDEX idx_users_last_active ON users(last_active_at);
    END IF;
END$$
DELIMITER ;

CALL migrate_studenthub_user_presence();
DROP PROCEDURE migrate_studenthub_user_presence;
