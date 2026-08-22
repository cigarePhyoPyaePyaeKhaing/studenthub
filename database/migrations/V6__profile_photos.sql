USE studenthub_db;

DROP PROCEDURE IF EXISTS migrate_studenthub_profile_photo;
DELIMITER $$
CREATE PROCEDURE migrate_studenthub_profile_photo()
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'users' AND column_name = 'profile_image'
    ) THEN
        ALTER TABLE users ADD COLUMN profile_image VARCHAR(255) NULL AFTER section_name;
    END IF;
END$$
DELIMITER ;

CALL migrate_studenthub_profile_photo();
DROP PROCEDURE migrate_studenthub_profile_photo;
