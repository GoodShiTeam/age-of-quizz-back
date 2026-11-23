-- Historique des actions de relecture (entité QuestionReview), base des stats
-- et badges des relecteurs. À exécuter une fois sur la base ageofquizz (MySQL 8),
-- idempotent. reviewer_id / question_id en INT comme user.id et question.id.
-- Pas d'historique pour les relectures faites avant ce script : les stats
-- partent de zéro.

CREATE TABLE IF NOT EXISTS question_review (
    id BIGINT NOT NULL AUTO_INCREMENT,
    reviewer_id INT NOT NULL,
    question_id INT NOT NULL,
    action VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_question_review_reviewer (reviewer_id),
    KEY idx_question_review_question (question_id),
    CONSTRAINT chk_question_review_action CHECK (action IN ('VALIDATED', 'REJECTED', 'MODIFIED')),
    CONSTRAINT fk_question_review_reviewer FOREIGN KEY (reviewer_id)
        REFERENCES `user` (id) ON DELETE CASCADE,
    CONSTRAINT fk_question_review_question FOREIGN KEY (question_id)
        REFERENCES question (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
