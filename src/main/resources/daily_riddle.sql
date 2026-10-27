-- Tables de l'énigme journalière (entités DailyRiddleEntry, DailyRiddleHint,
-- DailyRiddleSchedule). À exécuter une fois sur la base ageofquizz (MySQL 8).
-- Les données sont insérées au démarrage du back si daily_riddle_entry est vide.

CREATE TABLE IF NOT EXISTS daily_riddle_entry (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(20) NOT NULL,
    active BIT(1) NOT NULL DEFAULT b'1',
    manually_edited BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (id),
    CONSTRAINT uk_daily_riddle_entry_type_name UNIQUE (type, name),
    CONSTRAINT chk_daily_riddle_entry_type CHECK (type IN ('CIVILIZATION', 'UNIT'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS daily_riddle_hint (
    id INT NOT NULL AUTO_INCREMENT,
    entry_id INT NOT NULL,
    hint_order INT NOT NULL,
    label VARCHAR(100) NOT NULL,
    hint_text VARCHAR(1000) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_daily_riddle_hint_entry (entry_id),
    CONSTRAINT fk_daily_riddle_hint_entry FOREIGN KEY (entry_id)
        REFERENCES daily_riddle_entry (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS daily_riddle_schedule (
    riddle_date DATE NOT NULL,
    entry_id INT NOT NULL,
    PRIMARY KEY (riddle_date),
    KEY idx_daily_riddle_schedule_entry (entry_id),
    CONSTRAINT fk_daily_riddle_schedule_entry FOREIGN KEY (entry_id)
        REFERENCES daily_riddle_entry (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- Parties des utilisateurs connectés (entité DailyRiddleResult), une par jour.
-- guesses : propositions séparées par des retours à la ligne.
CREATE TABLE IF NOT EXISTS daily_riddle_result (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    riddle_date DATE NOT NULL,
    guesses VARCHAR(2000) NOT NULL DEFAULT '',
    status VARCHAR(10) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_daily_riddle_result_user_date UNIQUE (user_id, riddle_date),
    CONSTRAINT chk_daily_riddle_result_status CHECK (status IN ('PLAYING', 'WON', 'LOST')),
    CONSTRAINT fk_daily_riddle_result_user FOREIGN KEY (user_id)
        REFERENCES `user` (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
