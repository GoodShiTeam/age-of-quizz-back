-- Ajout du thème de question SHORTCUTS / « Raccourcis » (enum Question.QuestionTheme).
-- À exécuter en local et en prod. Relançable sans risque.
-- La liste reprend toutes les valeurs de l'enum Java.

ALTER TABLE question
    MODIFY theme ENUM('TECH_TREE', 'IMAGE', 'UNIT_STATS', 'SOUND', 'CULTURE', 'SHORTCUTS') NULL DEFAULT NULL;
