-- Ajout du thème de question CULTURE (enum Question.QuestionTheme).
-- À exécuter en local et en prod, avant questions_qcm_fun.sql. Relançable sans risque.
-- La liste reprend toutes les valeurs de l'enum Java (SOUND manquait en prod).

ALTER TABLE question
    MODIFY theme ENUM('TECH_TREE', 'IMAGE', 'UNIT_STATS', 'SOUND', 'CULTURE') NULL DEFAULT NULL;

-- QCM culture déjà insérés en TECH_TREE par la première version de questions_qcm_fun.sql.
UPDATE question SET theme = 'CULTURE'
WHERE theme = 'TECH_TREE' AND libelle IN (
    'Quel code de triche fait apparaître une voiture de sport armée (la célèbre « Cobra ») ?',
    'Que fait le code de triche « marco » ?',
    'Quel code de triche donne de la nourriture ?',
    'Quels codes de triche donnent des ressources ? (plusieurs réponses)',
    'Que permet le code de triche « aegis » ?',
    'Quelle unité le code « to smithereens » fait-il apparaître ?',
    'Quelle provocation (taunt) déclenche le célèbre « Wololo » des moines ?',
    'Quelle provocation (taunt) déclenche un célèbre éclat de rire ?',
    'À quoi sert traditionnellement la provocation 14 ?',
    'Quelle campagne sert de tutoriel dans The Age of Kings ?',
    'Comment meurt Frédéric Barberousse à la fin de sa campagne (et dans l''Histoire) ?',
    'Quel personnage n''avait PAS de campagne dans la version originale de 1999 (The Age of Kings) ?',
    'Avec quelle civilisation joue-t-on la campagne de Jeanne d''Arc ?',
    'Quel studio a développé Age of Empires II: The Age of Kings en 1999 ?',
    'En quelle année est sortie Age of Empires II: Definitive Edition ?',
    'De quel pays vient le joueur professionnel TheViper ?'
);
