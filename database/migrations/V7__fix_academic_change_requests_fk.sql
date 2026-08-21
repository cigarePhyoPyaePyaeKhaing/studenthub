-- =============================================================================
-- Migration: V7__fix_academic_change_requests_fk.sql
-- Purpose: Create academic_change_requests with valid MySQL 8.4 FK constraints.
-- Target Database: defaultdb (MySQL 8.4+)
-- Root Cause Fixed: MySQL 8.4 prohibits ON DELETE CASCADE on a base column
--                   (user_id) when referenced in a STORED generated column
--                   (pending_user_id). Removed ON DELETE CASCADE to use standard
--                   RESTRICT / NO ACTION while preserving foreign keys and
--                   the one-pending-request uniqueness business rule.
-- Safety: Non-destructive. Uses CREATE TABLE IF NOT EXISTS.
-- =============================================================================

CREATE TABLE IF NOT EXISTS academic_change_requests (
    request_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    old_semester INT NULL,
    old_section VARCHAR(20) NULL,
    requested_semester INT NOT NULL,
    requested_section VARCHAR(20) NOT NULL,
    reason VARCHAR(1000) NOT NULL,
    status ENUM('PENDING','APPROVED','REJECTED') NOT NULL DEFAULT 'PENDING',
    pending_user_id INT GENERATED ALWAYS AS (CASE WHEN status='PENDING' THEN user_id ELSE NULL END) STORED,
    reviewed_by INT NULL,
    admin_note VARCHAR(1000) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_at DATETIME NULL,

    CONSTRAINT fk_academic_user
        FOREIGN KEY (user_id)
        REFERENCES users (id),

    CONSTRAINT fk_academic_reviewer
        FOREIGN KEY (reviewed_by)
        REFERENCES users (id)
        ON DELETE SET NULL,

    UNIQUE KEY uq_academic_one_pending (pending_user_id),
    INDEX idx_academic_status (status, created_at),
    INDEX idx_academic_user (user_id, status),
    INDEX idx_academic_reviewer (reviewed_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
