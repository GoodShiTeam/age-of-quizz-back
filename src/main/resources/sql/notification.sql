-- Notifications des auteurs et relecteurs (entité Notification).
-- À exécuter une fois sur la base ageofquizz (MySQL 8), idempotent.
-- recipient_id en INT : user.id est un INT(11) en base (pas BIGINT).
-- question_id en INT comme question.id.

CREATE TABLE IF NOT EXISTS notification (
    id BIGINT NOT NULL AUTO_INCREMENT,
    recipient_id INT NOT NULL,
    question_id INT NOT NULL,
    type VARCHAR(30) NOT NULL,
    actor_name VARCHAR(255) NULL,
    created_at DATETIME(6) NOT NULL,
    read_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    KEY idx_notification_recipient_created (recipient_id, created_at),
    KEY idx_notification_recipient_read (recipient_id, read_at),
    KEY idx_notification_question (question_id),
    CONSTRAINT chk_notification_type CHECK (type IN ('QUESTION_VALIDATED', 'QUESTION_REJECTED',
        'QUESTION_MODIFIED', 'QUESTION_TO_REVIEW', 'QUESTION_RESUBMITTED')),
    CONSTRAINT fk_notification_recipient FOREIGN KEY (recipient_id)
        REFERENCES `user` (id) ON DELETE CASCADE,
    CONSTRAINT fk_notification_question FOREIGN KEY (question_id)
        REFERENCES question (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
