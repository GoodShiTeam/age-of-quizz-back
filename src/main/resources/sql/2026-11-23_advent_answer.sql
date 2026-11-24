-- Réponses au calendrier de l'avent (entité AdventAnswer), un essai par
-- joueur, par édition (event_year) et par case (day).
-- À exécuter en local et en prod. Relançable sans risque.
-- user_id en INT : user.id est un INT(11) en base (pas BIGINT).

CREATE TABLE IF NOT EXISTS advent_answer (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id INT NOT NULL,
    event_year INT NOT NULL,
    day INT NOT NULL,
    answer VARCHAR(500) NOT NULL,
    correct BIT(1) NOT NULL,
    answered_at DATETIME(6) NOT NULL,
    -- Jokers dépensés pour rejouer la case (1 joker gagné toutes les 10
    -- questions de quiz jouées pendant l'event)
    jokers_used INT NOT NULL DEFAULT 0,
    -- Bonne réponse affichée après une erreur : plus de joker possible
    revealed BIT(1) NOT NULL DEFAULT b'0',
    -- Essais gratuits utilisés (format ZOOM : plusieurs essais, l'image se
    -- dézoome à chaque erreur ; 1 pour les autres formats)
    attempts INT NOT NULL DEFAULT 1,
    -- Case « rattrapage » : question de quiz ratée posée au joueur (lecture
    -- seule, pas de clé étrangère pour ne pas bloquer la suppression d'une question)
    quiz_question_id INT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_advent_answer_user_year_day UNIQUE (user_id, event_year, day),
    KEY idx_advent_answer_year (event_year),
    CONSTRAINT chk_advent_answer_day CHECK (day BETWEEN 1 AND 24),
    CONSTRAINT fk_advent_answer_user FOREIGN KEY (user_id)
        REFERENCES `user` (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- Ajout de la colonne attempts sur une table créée avant son introduction
-- (base locale). Sans effet si elle existe déjà.
SET @has_attempts := (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'advent_answer' AND COLUMN_NAME = 'attempts');
SET @sql := IF(@has_attempts = 0,
    'ALTER TABLE advent_answer ADD COLUMN attempts INT NOT NULL DEFAULT 1 AFTER revealed',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
