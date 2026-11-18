-- Signalements de questions par les joueurs (entité QuestionReport) et nouveau
-- type de notification QUESTION_REPORTED. À exécuter sur la base ageofquizz
-- (MySQL 8 / MariaDB), relançable.
-- question_id / reporter_id / resolved_by_id en INT comme question.id et user.id.

CREATE TABLE IF NOT EXISTS question_report (
    id BIGINT NOT NULL AUTO_INCREMENT,
    question_id INT NOT NULL,
    reporter_id INT NOT NULL,
    reason VARCHAR(20) NOT NULL,
    comment VARCHAR(500) NULL,
    created_at DATETIME(6) NOT NULL,
    resolved_at DATETIME(6) NULL,
    resolved_by_id INT NULL,
    PRIMARY KEY (id),
    KEY idx_question_report_open (resolved_at, created_at),
    KEY idx_question_report_question (question_id, resolved_at),
    KEY idx_question_report_reporter (reporter_id),
    CONSTRAINT chk_question_report_reason CHECK (reason IN ('OUTDATED', 'WRONG_ANSWER', 'WORDING',
        'MEDIA', 'OTHER')),
    CONSTRAINT fk_question_report_question FOREIGN KEY (question_id)
        REFERENCES question (id) ON DELETE CASCADE,
    CONSTRAINT fk_question_report_reporter FOREIGN KEY (reporter_id)
        REFERENCES `user` (id) ON DELETE CASCADE,
    CONSTRAINT fk_question_report_resolved_by FOREIGN KEY (resolved_by_id)
        REFERENCES `user` (id) ON DELETE SET NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- Le CHECK de notification.type (créé par notification.sql) doit accepter
-- QUESTION_REPORTED. Relançable tant que la contrainte existe (cas normal).
ALTER TABLE notification DROP CONSTRAINT chk_notification_type;
ALTER TABLE notification ADD CONSTRAINT chk_notification_type CHECK (type IN ('QUESTION_VALIDATED',
    'QUESTION_REJECTED', 'QUESTION_MODIFIED', 'QUESTION_TO_REVIEW', 'QUESTION_RESUBMITTED',
    'QUESTION_REPORTED'));
