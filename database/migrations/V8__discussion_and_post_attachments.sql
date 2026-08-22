USE studenthub_db;

ALTER TABLE messages ADD COLUMN attachment_name VARCHAR(180) NULL,
    ADD COLUMN attachment_stored_name VARCHAR(64) NULL,
    ADD COLUMN attachment_mime_type VARCHAR(120) NULL,
    ADD COLUMN attachment_size BIGINT NULL;

ALTER TABLE posts ADD COLUMN attachment_name VARCHAR(180) NULL,
    ADD COLUMN attachment_stored_name VARCHAR(64) NULL,
    ADD COLUMN attachment_mime_type VARCHAR(120) NULL,
    ADD COLUMN attachment_size BIGINT NULL;
