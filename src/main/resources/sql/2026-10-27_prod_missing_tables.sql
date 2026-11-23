-- Tables présentes en local mais absentes de la base de prod.
-- Déduites des entités GameSessionEntity, GamePlayerResultEntity,
-- GameQuestionDetailEntity, GameRecapSnapshotEntity et DailyRiddleResult.
--
-- En prod, user.id et question.id sont des INT(11) signés (et non BIGINT comme
-- le laisse penser User.id en Long) : les colonnes qui les référencent doivent
-- être en INT, sinon erreur 150 « Foreign key constraint is incorrectly formed ».
--
-- Partie 1 (CREATE + MODIFY) : relançable sans risque. Elle remet aussi en INT
-- les colonnes des tables créées par une version précédente de ce script.
-- Partie 2 (clés étrangères) : à exécuter une seule fois ; une relance échoue
-- avec « Duplicate key / constraint name », sans conséquence.

-- ======================= Partie 1 : tables =======================

CREATE TABLE IF NOT EXISTS game_session (
    game_session_id VARCHAR(36) NOT NULL,
    room_code VARCHAR(255) NULL,
    game_mode VARCHAR(255) NULL,
    status VARCHAR(255) NULL,
    start_time DATETIME(6) NULL,
    end_time DATETIME(6) NULL,
    total_duration_s INT NULL,
    question_count INT NULL,
    quiz_theme VARCHAR(255) NULL,
    quiz_difficulty VARCHAR(255) NULL,
    statistics JSON NULL,
    PRIMARY KEY (game_session_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- user_id est nul pour les joueurs anonymes.
CREATE TABLE IF NOT EXISTS game_player_result (
    id INT NOT NULL AUTO_INCREMENT,
    game_session_id VARCHAR(36) NULL,
    user_id INT NULL,
    username VARCHAR(255) NULL,
    `rank` INT NULL,
    score INT NULL,
    points INT NULL,
    correct_answers INT NULL,
    total_questions INT NULL,
    accuracy DOUBLE NULL,
    total_time_seconds BIGINT NULL,
    average_time_per_question DOUBLE NULL,
    avatar VARCHAR(255) NULL,
    additional JSON NULL,
    PRIMARY KEY (id),
    KEY idx_game_player_result_session (game_session_id),
    KEY idx_game_player_result_user (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS game_question_detail (
    id INT NOT NULL AUTO_INCREMENT,
    game_session_id VARCHAR(36) NULL,
    question_id INT NULL,
    question_order INT NULL,
    question_text TEXT NULL,
    correct_answers JSON NULL,
    PRIMARY KEY (id),
    KEY idx_game_question_detail_session (game_session_id),
    KEY idx_game_question_detail_question (question_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS game_recap_snapshot (
    game_session_id VARCHAR(36) NOT NULL,
    recap_json JSON NULL,
    PRIMARY KEY (game_session_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- Énigme du jour : résultats des utilisateurs connectés (aussi dans daily_riddle.sql).
CREATE TABLE IF NOT EXISTS daily_riddle_result (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id INT NOT NULL,
    riddle_date DATE NOT NULL,
    guesses VARCHAR(2000) NOT NULL DEFAULT '',
    status VARCHAR(10) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_daily_riddle_result_user_date UNIQUE (user_id, riddle_date),
    CONSTRAINT chk_daily_riddle_result_status CHECK (status IN ('PLAYING', 'WON', 'LOST'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- Tables déjà créées avec user_id en BIGINT par la version précédente du script.
ALTER TABLE game_player_result MODIFY user_id INT NULL;
ALTER TABLE daily_riddle_result MODIFY user_id INT NOT NULL;

-- ================== Partie 2 : clés étrangères (une fois) ==================

ALTER TABLE game_player_result
    ADD CONSTRAINT fk_game_player_result_user FOREIGN KEY (user_id) REFERENCES `user` (id);

ALTER TABLE game_question_detail
    ADD CONSTRAINT fk_game_question_detail_question FOREIGN KEY (question_id) REFERENCES question (id);

ALTER TABLE daily_riddle_result
    ADD CONSTRAINT fk_daily_riddle_result_user FOREIGN KEY (user_id) REFERENCES `user` (id) ON DELETE CASCADE;
