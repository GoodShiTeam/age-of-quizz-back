-- 43 nouveaux QCM « fun » (codes de triche, provocations, campagnes,
-- histoire du jeu, records de stats, bonus de civilisations).
-- Statut CREATED_REVIEW : à valider via la page de révision avant d'apparaître en jeu.
-- Stats = valeurs de base (sans bonus ni améliorations), issues des données aoe2techtree.
-- Relançable sans risque : une question déjà présente (même libellé) n'est pas recréée,
-- et ses réponses ne sont ajoutées que si elle n'en a aucune.
--
-- PRÉREQUIS : exécuter question_theme_culture.sql avant (ajout du thème CULTURE).

-- Auteur des questions (id de la table user) : à adapter.
SET @author_id = 1;

START TRANSACTION;

-- 1. Quel code de triche fait apparaître une voiture de sport armée (la célèbre « Cobra ») ?
INSERT INTO question (theme, type, civilisation, building, status, libelle, explication, author, created_at, modified_at)
SELECT 'CULTURE', 'MULTIPLE', 'NONE', 'NONE', 'CREATED_REVIEW', 'Quel code de triche fait apparaître une voiture de sport armée (la célèbre « Cobra ») ?', '« how do you turn this on » fait apparaître la Cobra, une voiture qui mitraille : l''unité la plus anachronique du jeu.', @author_id, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM question WHERE libelle = 'Quel code de triche fait apparaître une voiture de sport armée (la célèbre « Cobra ») ?');
SET @q = (SELECT id FROM question WHERE libelle = 'Quel code de triche fait apparaître une voiture de sport armée (la célèbre « Cobra ») ?' ORDER BY id DESC LIMIT 1);
INSERT INTO answer (question_id, value, correct)
SELECT @q, t.value, t.correct FROM (
SELECT 'aegis' AS value, 0 AS correct
  UNION ALL SELECT 'how do you turn this on', 1
  UNION ALL SELECT 'i r winner', 0
  UNION ALL SELECT 'to smithereens', 0
) t
WHERE NOT EXISTS (SELECT 1 FROM answer WHERE question_id = @q);

-- 2. Que fait le code de triche « marco » ?
INSERT INTO question (theme, type, civilisation, building, status, libelle, explication, author, created_at, modified_at)
SELECT 'CULTURE', 'MULTIPLE', 'NONE', 'NONE', 'CREATED_REVIEW', 'Que fait le code de triche « marco » ?', '« marco » révèle la carte ; son acolyte « polo » supprime le brouillard de guerre. Les deux ensemble : Marco Polo, l''explorateur.', @author_id, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM question WHERE libelle = 'Que fait le code de triche « marco » ?');
SET @q = (SELECT id FROM question WHERE libelle = 'Que fait le code de triche « marco » ?' ORDER BY id DESC LIMIT 1);
INSERT INTO answer (question_id, value, correct)
SELECT @q, t.value, t.correct FROM (
SELECT 'Il fait gagner la partie' AS value, 0 AS correct
  UNION ALL SELECT 'Il donne de l''or', 0
  UNION ALL SELECT 'Il supprime le brouillard de guerre', 0
  UNION ALL SELECT 'Il révèle toute la carte', 1
) t
WHERE NOT EXISTS (SELECT 1 FROM answer WHERE question_id = @q);

-- 3. Quel code de triche donne de la nourriture ?
INSERT INTO question (theme, type, civilisation, building, status, libelle, explication, author, created_at, modified_at)
SELECT 'CULTURE', 'MULTIPLE', 'NONE', 'NONE', 'CREATED_REVIEW', 'Quel code de triche donne de la nourriture ?', '« lumberjack » donne du bois, « robin hood » de l''or et « rock on » de la pierre.', @author_id, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM question WHERE libelle = 'Quel code de triche donne de la nourriture ?');
SET @q = (SELECT id FROM question WHERE libelle = 'Quel code de triche donne de la nourriture ?' ORDER BY id DESC LIMIT 1);
INSERT INTO answer (question_id, value, correct)
SELECT @q, t.value, t.correct FROM (
SELECT 'lumberjack' AS value, 0 AS correct
  UNION ALL SELECT 'robin hood', 0
  UNION ALL SELECT 'rock on', 0
  UNION ALL SELECT 'cheese steak jimmy''s', 1
) t
WHERE NOT EXISTS (SELECT 1 FROM answer WHERE question_id = @q);

-- 4. Quels codes de triche donnent des ressources ? (plusieurs réponses)
INSERT INTO question (theme, type, civilisation, building, status, libelle, explication, author, created_at, modified_at)
SELECT 'CULTURE', 'MULTIPLE', 'NONE', 'NONE', 'CREATED_REVIEW', 'Quels codes de triche donnent des ressources ? (plusieurs réponses)', 'Bois, or et pierre. « marco » ne donne rien : il révèle la carte.', @author_id, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM question WHERE libelle = 'Quels codes de triche donnent des ressources ? (plusieurs réponses)');
SET @q = (SELECT id FROM question WHERE libelle = 'Quels codes de triche donnent des ressources ? (plusieurs réponses)' ORDER BY id DESC LIMIT 1);
INSERT INTO answer (question_id, value, correct)
SELECT @q, t.value, t.correct FROM (
SELECT 'robin hood' AS value, 1 AS correct
  UNION ALL SELECT 'lumberjack', 1
  UNION ALL SELECT 'rock on', 1
  UNION ALL SELECT 'marco', 0
) t
WHERE NOT EXISTS (SELECT 1 FROM answer WHERE question_id = @q);

-- 5. Que permet le code de triche « aegis » ?
INSERT INTO question (theme, type, civilisation, building, status, libelle, explication, author, created_at, modified_at)
SELECT 'CULTURE', 'MULTIPLE', 'NONE', 'NONE', 'CREATED_REVIEW', 'Que permet le code de triche « aegis » ?', 'Avec « aegis », tout est instantané : bâtiments, unités et récolte. Le nom vient de l''égide, le bouclier des dieux grecs.', @author_id, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM question WHERE libelle = 'Que permet le code de triche « aegis » ?');
SET @q = (SELECT id FROM question WHERE libelle = 'Que permet le code de triche « aegis » ?' ORDER BY id DESC LIMIT 1);
INSERT INTO answer (question_id, value, correct)
SELECT @q, t.value, t.correct FROM (
SELECT 'Passer directement à l''Âge impérial' AS value, 0 AS correct
  UNION ALL SELECT 'Convertir toutes les unités ennemies', 0
  UNION ALL SELECT 'Rendre ses unités invincibles', 0
  UNION ALL SELECT 'Construire, produire et récolter instantanément', 1
) t
WHERE NOT EXISTS (SELECT 1 FROM answer WHERE question_id = @q);

-- 6. Quelle unité le code « to smithereens » fait-il apparaître ?
INSERT INTO question (theme, type, civilisation, building, status, libelle, explication, author, created_at, modified_at)
SELECT 'CULTURE', 'MULTIPLE', 'NONE', 'NONE', 'CREATED_REVIEW', 'Quelle unité le code « to smithereens » fait-il apparaître ?', '« to smithereens » signifie « réduire en miettes » : on obtient un saboteur, une version très explosive du bombardier.', @author_id, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM question WHERE libelle = 'Quelle unité le code « to smithereens » fait-il apparaître ?');
SET @q = (SELECT id FROM question WHERE libelle = 'Quelle unité le code « to smithereens » fait-il apparaître ?' ORDER BY id DESC LIMIT 1);
INSERT INTO answer (question_id, value, correct)
SELECT @q, t.value, t.correct FROM (
SELECT 'Un navire de démolition' AS value, 0 AS correct
  UNION ALL SELECT 'Un canon à bombarde', 0
  UNION ALL SELECT 'Un bombardier (saboteur)', 1
  UNION ALL SELECT 'Un trébuchet', 0
) t
WHERE NOT EXISTS (SELECT 1 FROM answer WHERE question_id = @q);

-- 7. Quelle provocation (taunt) déclenche le célèbre « Wololo » des moines ?
INSERT INTO question (theme, type, civilisation, building, status, libelle, explication, author, created_at, modified_at)
SELECT 'CULTURE', 'MULTIPLE', 'NONE', 'NONE', 'CREATED_REVIEW', 'Quelle provocation (taunt) déclenche le célèbre « Wololo » des moines ?', 'Le 30 reprend le chant de conversion des moines, devenu un mème au point de donner son nom au tournoi Red Bull Wololo.', @author_id, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM question WHERE libelle = 'Quelle provocation (taunt) déclenche le célèbre « Wololo » des moines ?');
SET @q = (SELECT id FROM question WHERE libelle = 'Quelle provocation (taunt) déclenche le célèbre « Wololo » des moines ?' ORDER BY id DESC LIMIT 1);
INSERT INTO answer (question_id, value, correct)
SELECT @q, t.value, t.correct FROM (
SELECT '1' AS value, 0 AS correct
  UNION ALL SELECT '14', 0
  UNION ALL SELECT '30', 1
  UNION ALL SELECT '11', 0
) t
WHERE NOT EXISTS (SELECT 1 FROM answer WHERE question_id = @q);

-- 8. Quelle provocation (taunt) déclenche un célèbre éclat de rire ?
INSERT INTO question (theme, type, civilisation, building, status, libelle, explication, author, created_at, modified_at)
SELECT 'CULTURE', 'MULTIPLE', 'NONE', 'NONE', 'CREATED_REVIEW', 'Quelle provocation (taunt) déclenche un célèbre éclat de rire ?', 'Le taunt 11, le rire d''Herb, est l''un des plus utilisés pour narguer un adversaire.', @author_id, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM question WHERE libelle = 'Quelle provocation (taunt) déclenche un célèbre éclat de rire ?');
SET @q = (SELECT id FROM question WHERE libelle = 'Quelle provocation (taunt) déclenche un célèbre éclat de rire ?' ORDER BY id DESC LIMIT 1);
INSERT INTO answer (question_id, value, correct)
SELECT @q, t.value, t.correct FROM (
SELECT '11' AS value, 1 AS correct
  UNION ALL SELECT '30', 0
  UNION ALL SELECT '23', 0
  UNION ALL SELECT '7', 0
) t
WHERE NOT EXISTS (SELECT 1 FROM answer WHERE question_id = @q);

-- 9. À quoi sert traditionnellement la provocation 14 ?
INSERT INTO question (theme, type, civilisation, building, status, libelle, explication, author, created_at, modified_at)
SELECT 'CULTURE', 'MULTIPLE', 'NONE', 'NONE', 'CREATED_REVIEW', 'À quoi sert traditionnellement la provocation 14 ?', 'Le 14 (« Start the game already! ») est le taunt préféré des joueurs impatients dans le salon d''attente.', @author_id, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM question WHERE libelle = 'À quoi sert traditionnellement la provocation 14 ?');
SET @q = (SELECT id FROM question WHERE libelle = 'À quoi sert traditionnellement la provocation 14 ?' ORDER BY id DESC LIMIT 1);
INSERT INTO answer (question_id, value, correct)
SELECT @q, t.value, t.correct FROM (
SELECT 'Féliciter son adversaire' AS value, 0 AS correct
  UNION ALL SELECT 'Annoncer une attaque', 0
  UNION ALL SELECT 'Demander de la nourriture', 0
  UNION ALL SELECT 'Presser les autres joueurs de lancer la partie', 1
) t
WHERE NOT EXISTS (SELECT 1 FROM answer WHERE question_id = @q);

-- 10. Quelle campagne sert de tutoriel dans The Age of Kings ?
INSERT INTO question (theme, type, civilisation, building, status, libelle, explication, author, created_at, modified_at)
SELECT 'CULTURE', 'MULTIPLE', 'NONE', 'NONE', 'CREATED_REVIEW', 'Quelle campagne sert de tutoriel dans The Age of Kings ?', 'On y apprend les bases en menant la révolte écossaise de William Wallace contre les Anglais.', @author_id, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM question WHERE libelle = 'Quelle campagne sert de tutoriel dans The Age of Kings ?');
SET @q = (SELECT id FROM question WHERE libelle = 'Quelle campagne sert de tutoriel dans The Age of Kings ?' ORDER BY id DESC LIMIT 1);
INSERT INTO answer (question_id, value, correct)
SELECT @q, t.value, t.correct FROM (
SELECT 'Gengis Khan' AS value, 0 AS correct
  UNION ALL SELECT 'Jeanne d''Arc', 0
  UNION ALL SELECT 'William Wallace', 1
  UNION ALL SELECT 'Saladin', 0
) t
WHERE NOT EXISTS (SELECT 1 FROM answer WHERE question_id = @q);

-- 11. Comment meurt Frédéric Barberousse à la fin de sa campagne (et dans l'Histoire) ?
INSERT INTO question (theme, type, civilisation, building, status, libelle, explication, author, created_at, modified_at)
SELECT 'CULTURE', 'MULTIPLE', 'TEUTONS', 'NONE', 'CREATED_REVIEW', 'Comment meurt Frédéric Barberousse à la fin de sa campagne (et dans l''Histoire) ?', 'En 1190, pendant la troisième croisade, l''empereur se noie dans le fleuve Saleph, en Anatolie, avant même d''atteindre la Terre sainte.', @author_id, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM question WHERE libelle = 'Comment meurt Frédéric Barberousse à la fin de sa campagne (et dans l''Histoire) ?');
SET @q = (SELECT id FROM question WHERE libelle = 'Comment meurt Frédéric Barberousse à la fin de sa campagne (et dans l''Histoire) ?' ORDER BY id DESC LIMIT 1);
INSERT INTO answer (question_id, value, correct)
SELECT @q, t.value, t.correct FROM (
SELECT 'Au siège de Jérusalem' AS value, 0 AS correct
  UNION ALL SELECT 'Noyé en traversant une rivière', 1
  UNION ALL SELECT 'Empoisonné par ses généraux', 0
  UNION ALL SELECT 'Tué en duel par Saladin', 0
) t
WHERE NOT EXISTS (SELECT 1 FROM answer WHERE question_id = @q);

-- 12. Quel personnage n'avait PAS de campagne dans la version originale de 1999 (The Age of Kings) ?
INSERT INTO question (theme, type, civilisation, building, status, libelle, explication, author, created_at, modified_at)
SELECT 'CULTURE', 'MULTIPLE', 'NONE', 'NONE', 'CREATED_REVIEW', 'Quel personnage n''avait PAS de campagne dans la version originale de 1999 (The Age of Kings) ?', 'La campagne d''El Cid est arrivée en 2000 avec l''extension The Conquerors, avec celles d''Attila et de Moctezuma.', @author_id, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM question WHERE libelle = 'Quel personnage n''avait PAS de campagne dans la version originale de 1999 (The Age of Kings) ?');
SET @q = (SELECT id FROM question WHERE libelle = 'Quel personnage n''avait PAS de campagne dans la version originale de 1999 (The Age of Kings) ?' ORDER BY id DESC LIMIT 1);
INSERT INTO answer (question_id, value, correct)
SELECT @q, t.value, t.correct FROM (
SELECT 'Gengis Khan' AS value, 0 AS correct
  UNION ALL SELECT 'Saladin', 0
  UNION ALL SELECT 'El Cid', 1
  UNION ALL SELECT 'Jeanne d''Arc', 0
) t
WHERE NOT EXISTS (SELECT 1 FROM answer WHERE question_id = @q);

-- 13. Avec quelle civilisation joue-t-on la campagne de Jeanne d'Arc ?
INSERT INTO question (theme, type, civilisation, building, status, libelle, explication, author, created_at, modified_at)
SELECT 'CULTURE', 'MULTIPLE', 'FRANKS', 'NONE', 'CREATED_REVIEW', 'Avec quelle civilisation joue-t-on la campagne de Jeanne d''Arc ?', 'Jeanne d''Arc mène les Francs contre les Anglais et leurs alliés bourguignons pendant la guerre de Cent Ans.', @author_id, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM question WHERE libelle = 'Avec quelle civilisation joue-t-on la campagne de Jeanne d''Arc ?');
SET @q = (SELECT id FROM question WHERE libelle = 'Avec quelle civilisation joue-t-on la campagne de Jeanne d''Arc ?' ORDER BY id DESC LIMIT 1);
INSERT INTO answer (question_id, value, correct)
SELECT @q, t.value, t.correct FROM (
SELECT 'Francs' AS value, 1 AS correct
  UNION ALL SELECT 'Bourguignons', 0
  UNION ALL SELECT 'Teutons', 0
  UNION ALL SELECT 'Britanniques', 0
) t
WHERE NOT EXISTS (SELECT 1 FROM answer WHERE question_id = @q);

-- 14. Quel studio a développé Age of Empires II: The Age of Kings en 1999 ?
INSERT INTO question (theme, type, civilisation, building, status, libelle, explication, author, created_at, modified_at)
SELECT 'CULTURE', 'MULTIPLE', 'NONE', 'NONE', 'CREATED_REVIEW', 'Quel studio a développé Age of Empires II: The Age of Kings en 1999 ?', 'Ensemble Studios a créé la série. Forgotten Empires a développé la Definitive Edition, et Relic Age of Empires IV.', @author_id, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM question WHERE libelle = 'Quel studio a développé Age of Empires II: The Age of Kings en 1999 ?');
SET @q = (SELECT id FROM question WHERE libelle = 'Quel studio a développé Age of Empires II: The Age of Kings en 1999 ?' ORDER BY id DESC LIMIT 1);
INSERT INTO answer (question_id, value, correct)
SELECT @q, t.value, t.correct FROM (
SELECT 'Relic Entertainment' AS value, 0 AS correct
  UNION ALL SELECT 'Forgotten Empires', 0
  UNION ALL SELECT 'Ensemble Studios', 1
  UNION ALL SELECT 'Blizzard Entertainment', 0
) t
WHERE NOT EXISTS (SELECT 1 FROM answer WHERE question_id = @q);

-- 15. En quelle année est sortie Age of Empires II: Definitive Edition ?
INSERT INTO question (theme, type, civilisation, building, status, libelle, explication, author, created_at, modified_at)
SELECT 'CULTURE', 'MULTIPLE', 'NONE', 'NONE', 'CREATED_REVIEW', 'En quelle année est sortie Age of Empires II: Definitive Edition ?', 'La Definitive Edition est sortie en novembre 2019, vingt ans après l''original. 2013 correspond à l''édition HD.', @author_id, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM question WHERE libelle = 'En quelle année est sortie Age of Empires II: Definitive Edition ?');
SET @q = (SELECT id FROM question WHERE libelle = 'En quelle année est sortie Age of Empires II: Definitive Edition ?' ORDER BY id DESC LIMIT 1);
INSERT INTO answer (question_id, value, correct)
SELECT @q, t.value, t.correct FROM (
SELECT '2017' AS value, 0 AS correct
  UNION ALL SELECT '2019', 1
  UNION ALL SELECT '2021', 0
  UNION ALL SELECT '2013', 0
) t
WHERE NOT EXISTS (SELECT 1 FROM answer WHERE question_id = @q);

-- 16. De quel pays vient le joueur professionnel TheViper ?
INSERT INTO question (theme, type, civilisation, building, status, libelle, explication, author, created_at, modified_at)
SELECT 'CULTURE', 'MULTIPLE', 'NONE', 'NONE', 'CREATED_REVIEW', 'De quel pays vient le joueur professionnel TheViper ?', 'Le Norvégien Ørjan « TheViper » Larsen est l''un des joueurs les plus titrés de l''histoire d''AoE2.', @author_id, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM question WHERE libelle = 'De quel pays vient le joueur professionnel TheViper ?');
SET @q = (SELECT id FROM question WHERE libelle = 'De quel pays vient le joueur professionnel TheViper ?' ORDER BY id DESC LIMIT 1);
INSERT INTO answer (question_id, value, correct)
SELECT @q, t.value, t.correct FROM (
SELECT 'Pologne' AS value, 0 AS correct
  UNION ALL SELECT 'Canada', 0
  UNION ALL SELECT 'Norvège', 1
  UNION ALL SELECT 'Allemagne', 0
) t
WHERE NOT EXISTS (SELECT 1 FROM answer WHERE question_id = @q);

-- 17. Parmi ces unités, laquelle a le plus de PV de base ?
INSERT INTO question (theme, type, civilisation, building, status, libelle, explication, author, created_at, modified_at)
SELECT 'UNIT_STATS', 'MULTIPLE', 'NONE', 'NONE', 'CREATED_REVIEW', 'Parmi ces unités, laquelle a le plus de PV de base ?', 'L''éléphant de guerre perse a 450 PV de base, contre 260 pour le navire tortue, 160 pour le paladin et 110 pour le cataphractaire.', @author_id, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM question WHERE libelle = 'Parmi ces unités, laquelle a le plus de PV de base ?');
SET @q = (SELECT id FROM question WHERE libelle = 'Parmi ces unités, laquelle a le plus de PV de base ?' ORDER BY id DESC LIMIT 1);
INSERT INTO answer (question_id, value, correct)
SELECT @q, t.value, t.correct FROM (
SELECT 'Éléphant de guerre' AS value, 1 AS correct
  UNION ALL SELECT 'Navire tortue', 0
  UNION ALL SELECT 'Cataphractaire', 0
  UNION ALL SELECT 'Paladin', 0
) t
WHERE NOT EXISTS (SELECT 1 FROM answer WHERE question_id = @q);

-- 18. Parmi ces unités, laquelle a la plus grande portée de base ?
INSERT INTO question (theme, type, civilisation, building, status, libelle, explication, author, created_at, modified_at)
SELECT 'UNIT_STATS', 'MULTIPLE', 'NONE', 'NONE', 'CREATED_REVIEW', 'Parmi ces unités, laquelle a la plus grande portée de base ?', 'Le trébuchet tire à 16 cases, devant le galion à canon (13), le canon à bombarde (12) et le fantassin à arc long (5).', @author_id, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM question WHERE libelle = 'Parmi ces unités, laquelle a la plus grande portée de base ?');
SET @q = (SELECT id FROM question WHERE libelle = 'Parmi ces unités, laquelle a la plus grande portée de base ?' ORDER BY id DESC LIMIT 1);
INSERT INTO answer (question_id, value, correct)
SELECT @q, t.value, t.correct FROM (
SELECT 'Galion à canon' AS value, 0 AS correct
  UNION ALL SELECT 'Fantassin à arc long', 0
  UNION ALL SELECT 'Trébuchet', 1
  UNION ALL SELECT 'Canon à bombarde', 0
) t
WHERE NOT EXISTS (SELECT 1 FROM answer WHERE question_id = @q);

-- 19. Quelle est l'attaque de base d'un trébuchet ?
INSERT INTO question (theme, type, civilisation, building, status, libelle, explication, author, created_at, modified_at)
SELECT 'UNIT_STATS', 'MULTIPLE', 'NONE', 'CASTLE', 'CREATED_REVIEW', 'Quelle est l''attaque de base d''un trébuchet ?', '200 d''attaque, la plus forte du jeu, pour raser les bâtiments à distance.', @author_id, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM question WHERE libelle = 'Quelle est l''attaque de base d''un trébuchet ?');
SET @q = (SELECT id FROM question WHERE libelle = 'Quelle est l''attaque de base d''un trébuchet ?' ORDER BY id DESC LIMIT 1);
INSERT INTO answer (question_id, value, correct)
SELECT @q, t.value, t.correct FROM (
SELECT '250' AS value, 0 AS correct
  UNION ALL SELECT '100', 0
  UNION ALL SELECT '150', 0
  UNION ALL SELECT '200', 1
) t
WHERE NOT EXISTS (SELECT 1 FROM answer WHERE question_id = @q);

-- 20. Combien de PV a un villageois de base ?
INSERT INTO question (theme, type, civilisation, building, status, libelle, explication, author, created_at, modified_at)
SELECT 'UNIT_STATS', 'MULTIPLE', 'NONE', 'TOWN_CENTER', 'CREATED_REVIEW', 'Combien de PV a un villageois de base ?', '25 PV seulement : la technologie Métier à tisser lui ajoute +15 PV, de quoi survivre à un loup ou à un raid précoce.', @author_id, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM question WHERE libelle = 'Combien de PV a un villageois de base ?');
SET @q = (SELECT id FROM question WHERE libelle = 'Combien de PV a un villageois de base ?' ORDER BY id DESC LIMIT 1);
INSERT INTO answer (question_id, value, correct)
SELECT @q, t.value, t.correct FROM (
SELECT '40' AS value, 0 AS correct
  UNION ALL SELECT '25', 1
  UNION ALL SELECT '30', 0
  UNION ALL SELECT '20', 0
) t
WHERE NOT EXISTS (SELECT 1 FROM answer WHERE question_id = @q);

-- 21. Comment se compare la vitesse de base d'un chevalier teutonique à celle d'un villageois ?
INSERT INTO question (theme, type, civilisation, building, status, libelle, explication, author, created_at, modified_at)
SELECT 'UNIT_STATS', 'MULTIPLE', 'TEUTONS', 'NONE', 'CREATED_REVIEW', 'Comment se compare la vitesse de base d''un chevalier teutonique à celle d''un villageois ?', 'Tous les deux avancent à 0,8. En armure lourde, le chevalier teutonique ne va pas plus vite qu''un paysan !', @author_id, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM question WHERE libelle = 'Comment se compare la vitesse de base d''un chevalier teutonique à celle d''un villageois ?');
SET @q = (SELECT id FROM question WHERE libelle = 'Comment se compare la vitesse de base d''un chevalier teutonique à celle d''un villageois ?' ORDER BY id DESC LIMIT 1);
INSERT INTO answer (question_id, value, correct)
SELECT @q, t.value, t.correct FROM (
SELECT 'Il est deux fois plus rapide' AS value, 0 AS correct
  UNION ALL SELECT 'Il est un peu plus rapide', 0
  UNION ALL SELECT 'Il est plus lent', 0
  UNION ALL SELECT 'Elle est identique', 1
) t
WHERE NOT EXISTS (SELECT 1 FROM answer WHERE question_id = @q);

-- 22. Parmi ces unités, laquelle se crée le plus vite ?
INSERT INTO question (theme, type, civilisation, building, status, libelle, explication, author, created_at, modified_at)
SELECT 'UNIT_STATS', 'MULTIPLE', 'NONE', 'NONE', 'CREATED_REVIEW', 'Parmi ces unités, laquelle se crée le plus vite ?', 'Le samouraï se forme en 9 secondes, contre 12 pour le chevalier teutonique et 13 pour le huskarl et le lanceur de hache.', @author_id, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM question WHERE libelle = 'Parmi ces unités, laquelle se crée le plus vite ?');
SET @q = (SELECT id FROM question WHERE libelle = 'Parmi ces unités, laquelle se crée le plus vite ?' ORDER BY id DESC LIMIT 1);
INSERT INTO answer (question_id, value, correct)
SELECT @q, t.value, t.correct FROM (
SELECT 'Huskarl' AS value, 0 AS correct
  UNION ALL SELECT 'Chevalier teutonique', 0
  UNION ALL SELECT 'Lanceur de hache', 0
  UNION ALL SELECT 'Samouraï', 1
) t
WHERE NOT EXISTS (SELECT 1 FROM answer WHERE question_id = @q);

-- 23. Combien coûte un moine ?
INSERT INTO question (theme, type, civilisation, building, status, libelle, explication, author, created_at, modified_at)
SELECT 'UNIT_STATS', 'MULTIPLE', 'NONE', 'MONASTERY', 'CREATED_REVIEW', 'Combien coûte un moine ?', 'Un moine coûte 100 or et rien d''autre. Le missionnaire espagnol coûte 100 or et 30 nourriture.', @author_id, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM question WHERE libelle = 'Combien coûte un moine ?');
SET @q = (SELECT id FROM question WHERE libelle = 'Combien coûte un moine ?' ORDER BY id DESC LIMIT 1);
INSERT INTO answer (question_id, value, correct)
SELECT @q, t.value, t.correct FROM (
SELECT '50 nourriture et 50 or' AS value, 0 AS correct
  UNION ALL SELECT '100 or', 1
  UNION ALL SELECT '50 or', 0
  UNION ALL SELECT '75 or', 0
) t
WHERE NOT EXISTS (SELECT 1 FROM answer WHERE question_id = @q);

-- 24. Combien coûte un paladin par rapport à un chevalier ?
INSERT INTO question (theme, type, civilisation, building, status, libelle, explication, author, created_at, modified_at)
SELECT 'UNIT_STATS', 'MULTIPLE', 'NONE', 'STABLE', 'CREATED_REVIEW', 'Combien coûte un paladin par rapport à un chevalier ?', '60 nourriture et 75 or dans les deux cas : c''est l''amélioration qui coûte cher, pas l''unité.', @author_id, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM question WHERE libelle = 'Combien coûte un paladin par rapport à un chevalier ?');
SET @q = (SELECT id FROM question WHERE libelle = 'Combien coûte un paladin par rapport à un chevalier ?' ORDER BY id DESC LIMIT 1);
INSERT INTO answer (question_id, value, correct)
SELECT @q, t.value, t.correct FROM (
SELECT '50 nourriture de plus' AS value, 0 AS correct
  UNION ALL SELECT '20 or de plus', 0
  UNION ALL SELECT 'Le double', 0
  UNION ALL SELECT 'Exactement la même chose', 1
) t
WHERE NOT EXISTS (SELECT 1 FROM answer WHERE question_id = @q);

-- 25. Quelle est l'armure de perçage de base d'un bélier ?
INSERT INTO question (theme, type, civilisation, building, status, libelle, explication, author, created_at, modified_at)
SELECT 'UNIT_STATS', 'MULTIPLE', 'NONE', 'SIEGE_WORKSHOP', 'CREATED_REVIEW', 'Quelle est l''armure de perçage de base d''un bélier ?', '180 d''armure de perçage : les flèches ne font quasiment rien aux béliers. Il faut les attaquer au corps à corps.', @author_id, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM question WHERE libelle = 'Quelle est l''armure de perçage de base d''un bélier ?');
SET @q = (SELECT id FROM question WHERE libelle = 'Quelle est l''armure de perçage de base d''un bélier ?' ORDER BY id DESC LIMIT 1);
INSERT INTO answer (question_id, value, correct)
SELECT @q, t.value, t.correct FROM (
SELECT '180' AS value, 1 AS correct
  UNION ALL SELECT '8', 0
  UNION ALL SELECT '50', 0
  UNION ALL SELECT '1000', 0
) t
WHERE NOT EXISTS (SELECT 1 FROM answer WHERE question_id = @q);

-- 26. Parmi ces unités, laquelle est la plus rapide ?
INSERT INTO question (theme, type, civilisation, building, status, libelle, explication, author, created_at, modified_at)
SELECT 'UNIT_STATS', 'MULTIPLE', 'NONE', 'NONE', 'CREATED_REVIEW', 'Parmi ces unités, laquelle est la plus rapide ?', 'La cogue marchande file à 1,65, devant le mangudaï (1,4), le chevalier (1,35) et le conquistador (1,3).', @author_id, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM question WHERE libelle = 'Parmi ces unités, laquelle est la plus rapide ?');
SET @q = (SELECT id FROM question WHERE libelle = 'Parmi ces unités, laquelle est la plus rapide ?' ORDER BY id DESC LIMIT 1);
INSERT INTO answer (question_id, value, correct)
SELECT @q, t.value, t.correct FROM (
SELECT 'Mangudaï' AS value, 0 AS correct
  UNION ALL SELECT 'Chevalier', 0
  UNION ALL SELECT 'Cogue marchande', 1
  UNION ALL SELECT 'Conquistador', 0
) t
WHERE NOT EXISTS (SELECT 1 FROM answer WHERE question_id = @q);

-- 27. Quelle civilisation n'a pas besoin de construire d'habitations ?
INSERT INTO question (theme, type, civilisation, building, status, libelle, explication, author, created_at, modified_at)
SELECT 'TECH_TREE', 'MULTIPLE', 'HUNS', 'NONE', 'CREATED_REVIEW', 'Quelle civilisation n''a pas besoin de construire d''habitations ?', 'Les Huns ignorent les habitations, en contrepartie ils commencent avec 100 de bois en moins.', @author_id, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM question WHERE libelle = 'Quelle civilisation n''a pas besoin de construire d''habitations ?');
SET @q = (SELECT id FROM question WHERE libelle = 'Quelle civilisation n''a pas besoin de construire d''habitations ?' ORDER BY id DESC LIMIT 1);
INSERT INTO answer (question_id, value, correct)
SELECT @q, t.value, t.correct FROM (
SELECT 'Mongols' AS value, 0 AS correct
  UNION ALL SELECT 'Incas', 0
  UNION ALL SELECT 'Goths', 0
  UNION ALL SELECT 'Huns', 1
) t
WHERE NOT EXISTS (SELECT 1 FROM answer WHERE question_id = @q);

-- 28. Quelle civilisation a pour bonus d'équipe de commencer avec un lama gratuit ?
INSERT INTO question (theme, type, civilisation, building, status, libelle, explication, author, created_at, modified_at)
SELECT 'TECH_TREE', 'MULTIPLE', 'INCAS', 'NONE', 'CREATED_REVIEW', 'Quelle civilisation a pour bonus d''équipe de commencer avec un lama gratuit ?', 'Le lama est le bonus d''équipe des Incas : un peu de nourriture en plus pour tous les alliés.', @author_id, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM question WHERE libelle = 'Quelle civilisation a pour bonus d''équipe de commencer avec un lama gratuit ?');
SET @q = (SELECT id FROM question WHERE libelle = 'Quelle civilisation a pour bonus d''équipe de commencer avec un lama gratuit ?' ORDER BY id DESC LIMIT 1);
INSERT INTO answer (question_id, value, correct)
SELECT @q, t.value, t.correct FROM (
SELECT 'Mayas' AS value, 0 AS correct
  UNION ALL SELECT 'Tupis', 0
  UNION ALL SELECT 'Incas', 1
  UNION ALL SELECT 'Aztèques', 0
) t
WHERE NOT EXISTS (SELECT 1 FROM answer WHERE question_id = @q);

-- 29. Quelle civilisation reçoit de l'or chaque fois qu'elle développe une technologie ?
INSERT INTO question (theme, type, civilisation, building, status, libelle, explication, author, created_at, modified_at)
SELECT 'TECH_TREE', 'MULTIPLE', 'SPANISH', 'NONE', 'CREATED_REVIEW', 'Quelle civilisation reçoit de l''or chaque fois qu''elle développe une technologie ?', 'Les Espagnols gagnent +20 or par technologie développée, et leurs améliorations de forge ne coûtent pas d''or.', @author_id, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM question WHERE libelle = 'Quelle civilisation reçoit de l''or chaque fois qu''elle développe une technologie ?');
SET @q = (SELECT id FROM question WHERE libelle = 'Quelle civilisation reçoit de l''or chaque fois qu''elle développe une technologie ?' ORDER BY id DESC LIMIT 1);
INSERT INTO answer (question_id, value, correct)
SELECT @q, t.value, t.correct FROM (
SELECT 'Italiens' AS value, 0 AS correct
  UNION ALL SELECT 'Portugais', 0
  UNION ALL SELECT 'Espagnols', 1
  UNION ALL SELECT 'Byzantins', 0
) t
WHERE NOT EXISTS (SELECT 1 FROM answer WHERE question_id = @q);

-- 30. Quelles unités sont des unités uniques espagnoles ? (plusieurs réponses)
INSERT INTO question (theme, type, civilisation, building, status, libelle, explication, author, created_at, modified_at)
SELECT 'TECH_TREE', 'MULTIPLE', 'SPANISH', 'NONE', 'CREATED_REVIEW', 'Quelles unités sont des unités uniques espagnoles ? (plusieurs réponses)', 'Conquistador (artilleur monté) et missionnaire (moine monté). Le jinete est berbère, l''arbalétrier génois italien.', @author_id, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM question WHERE libelle = 'Quelles unités sont des unités uniques espagnoles ? (plusieurs réponses)');
SET @q = (SELECT id FROM question WHERE libelle = 'Quelles unités sont des unités uniques espagnoles ? (plusieurs réponses)' ORDER BY id DESC LIMIT 1);
INSERT INTO answer (question_id, value, correct)
SELECT @q, t.value, t.correct FROM (
SELECT 'Conquistador' AS value, 1 AS correct
  UNION ALL SELECT 'Missionnaire', 1
  UNION ALL SELECT 'Arbalétrier génois', 0
  UNION ALL SELECT 'Jinete', 0
) t
WHERE NOT EXISTS (SELECT 1 FROM answer WHERE question_id = @q);

-- 31. Quelle unité la technologie unique espagnole « Suprématie » renforce-t-elle ?
INSERT INTO question (theme, type, civilisation, building, status, libelle, explication, author, created_at, modified_at)
SELECT 'TECH_TREE', 'MULTIPLE', 'SPANISH', 'NONE', 'CREATED_REVIEW', 'Quelle unité la technologie unique espagnole « Suprématie » renforce-t-elle ?', 'Suprématie donne aux villageois +40 PV, +6 d''attaque et de l''armure : des paysans capables de se battre.', @author_id, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM question WHERE libelle = 'Quelle unité la technologie unique espagnole « Suprématie » renforce-t-elle ?');
SET @q = (SELECT id FROM question WHERE libelle = 'Quelle unité la technologie unique espagnole « Suprématie » renforce-t-elle ?' ORDER BY id DESC LIMIT 1);
INSERT INTO answer (question_id, value, correct)
SELECT @q, t.value, t.correct FROM (
SELECT 'Les villageois' AS value, 1 AS correct
  UNION ALL SELECT 'Les conquistadors', 0
  UNION ALL SELECT 'Les galions à canon', 0
  UNION ALL SELECT 'Les missionnaires', 0
) t
WHERE NOT EXISTS (SELECT 1 FROM answer WHERE question_id = @q);

-- 32. Chez quelle civilisation les fermes coûtent-elles 40 % de moins ?
INSERT INTO question (theme, type, civilisation, building, status, libelle, explication, author, created_at, modified_at)
SELECT 'TECH_TREE', 'MULTIPLE', 'TEUTONS', 'MILL', 'CREATED_REVIEW', 'Chez quelle civilisation les fermes coûtent-elles 40 % de moins ?', 'Les Teutons ont des fermes à -40 %. Les Francs ont les technologies du moulin gratuites et les Slaves des fermiers 15 % plus rapides.', @author_id, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM question WHERE libelle = 'Chez quelle civilisation les fermes coûtent-elles 40 % de moins ?');
SET @q = (SELECT id FROM question WHERE libelle = 'Chez quelle civilisation les fermes coûtent-elles 40 % de moins ?' ORDER BY id DESC LIMIT 1);
INSERT INTO answer (question_id, value, correct)
SELECT @q, t.value, t.correct FROM (
SELECT 'Francs' AS value, 0 AS correct
  UNION ALL SELECT 'Britanniques', 0
  UNION ALL SELECT 'Slaves', 0
  UNION ALL SELECT 'Teutons', 1
) t
WHERE NOT EXISTS (SELECT 1 FROM answer WHERE question_id = @q);

-- 33. Quelle civilisation ne paie que 5 % de taxe au marché ?
INSERT INTO question (theme, type, civilisation, building, status, libelle, explication, author, created_at, modified_at)
SELECT 'TECH_TREE', 'MULTIPLE', 'SARACENS', 'MARKET', 'CREATED_REVIEW', 'Quelle civilisation ne paie que 5 % de taxe au marché ?', 'Les Sarrasins ont une taxe d''échange de seulement 5 % et des marchés moins chers (-100 bois).', @author_id, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM question WHERE libelle = 'Quelle civilisation ne paie que 5 % de taxe au marché ?');
SET @q = (SELECT id FROM question WHERE libelle = 'Quelle civilisation ne paie que 5 % de taxe au marché ?' ORDER BY id DESC LIMIT 1);
INSERT INTO answer (question_id, value, correct)
SELECT @q, t.value, t.correct FROM (
SELECT 'Maliens' AS value, 0 AS correct
  UNION ALL SELECT 'Portugais', 0
  UNION ALL SELECT 'Byzantins', 0
  UNION ALL SELECT 'Sarrasins', 1
) t
WHERE NOT EXISTS (SELECT 1 FROM answer WHERE question_id = @q);

-- 34. Comment commencent les Chinois ?
INSERT INTO question (theme, type, civilisation, building, status, libelle, explication, author, created_at, modified_at)
SELECT 'TECH_TREE', 'MULTIPLE', 'CHINESE', 'NONE', 'CREATED_REVIEW', 'Comment commencent les Chinois ?', 'Les autres réponses sont les bonus de départ des Mayas, des Perses et des Aztèques.', @author_id, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM question WHERE libelle = 'Comment commencent les Chinois ?');
SET @q = (SELECT id FROM question WHERE libelle = 'Comment commencent les Chinois ?' ORDER BY id DESC LIMIT 1);
INSERT INTO answer (question_id, value, correct)
SELECT @q, t.value, t.correct FROM (
SELECT 'Avec 3 villageois de plus, mais 200 nourriture et 50 bois de moins' AS value, 1 AS correct
  UNION ALL SELECT 'Avec 1 villageois de plus, mais 50 nourriture de moins', 0
  UNION ALL SELECT 'Avec 50 bois et 50 nourriture de plus', 0
  UNION ALL SELECT 'Avec 50 or de plus', 0
) t
WHERE NOT EXISTS (SELECT 1 FROM answer WHERE question_id = @q);

-- 35. Quelle civilisation possède les technologies uniques « Anarchie » et « Mobilisation rapide » ?
INSERT INTO question (theme, type, civilisation, building, status, libelle, explication, author, created_at, modified_at)
SELECT 'TECH_TREE', 'MULTIPLE', 'GOTHS', 'BARRACKS', 'CREATED_REVIEW', 'Quelle civilisation possède les technologies uniques « Anarchie » et « Mobilisation rapide » ?', 'Anarchie permet de former des huskarls à la caserne, et Mobilisation rapide double la vitesse de travail de la caserne.', @author_id, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM question WHERE libelle = 'Quelle civilisation possède les technologies uniques « Anarchie » et « Mobilisation rapide » ?');
SET @q = (SELECT id FROM question WHERE libelle = 'Quelle civilisation possède les technologies uniques « Anarchie » et « Mobilisation rapide » ?' ORDER BY id DESC LIMIT 1);
INSERT INTO answer (question_id, value, correct)
SELECT @q, t.value, t.correct FROM (
SELECT 'Vikings' AS value, 0 AS correct
  UNION ALL SELECT 'Goths', 1
  UNION ALL SELECT 'Celtes', 0
  UNION ALL SELECT 'Huns', 0
) t
WHERE NOT EXISTS (SELECT 1 FROM answer WHERE question_id = @q);

-- 36. Que fait la technologie unique hunnique « Athéisme » ?
INSERT INTO question (theme, type, civilisation, building, status, libelle, explication, author, created_at, modified_at)
SELECT 'TECH_TREE', 'MULTIPLE', 'HUNS', 'NONE', 'CREATED_REVIEW', 'Que fait la technologie unique hunnique « Athéisme » ?', 'Les reliques ennemies rapportent 50 % de moins, et les victoires par merveille ou par relique prennent 100 ans de plus.', @author_id, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM question WHERE libelle = 'Que fait la technologie unique hunnique « Athéisme » ?');
SET @q = (SELECT id FROM question WHERE libelle = 'Que fait la technologie unique hunnique « Athéisme » ?' ORDER BY id DESC LIMIT 1);
INSERT INTO answer (question_id, value, correct)
SELECT @q, t.value, t.correct FROM (
SELECT 'Affaiblit les reliques ennemies et rallonge les victoires par merveille et par relique' AS value, 1 AS correct
  UNION ALL SELECT 'Supprime tous les moines ennemis', 0
  UNION ALL SELECT 'Rend les monastères moins chers', 0
  UNION ALL SELECT 'Rend les unités hunniques impossibles à convertir', 0
) t
WHERE NOT EXISTS (SELECT 1 FROM answer WHERE question_id = @q);

-- 37. Quel est le bonus d'équipe des Byzantins ?
INSERT INTO question (theme, type, civilisation, building, status, libelle, explication, author, created_at, modified_at)
SELECT 'TECH_TREE', 'MULTIPLE', 'BYZANTINES', 'MONASTERY', 'CREATED_REVIEW', 'Quel est le bonus d''équipe des Byzantins ?', 'Les autres réponses sont des bonus byzantins, mais ils ne profitent qu''au joueur byzantin, pas à ses alliés.', @author_id, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM question WHERE libelle = 'Quel est le bonus d''équipe des Byzantins ?');
SET @q = (SELECT id FROM question WHERE libelle = 'Quel est le bonus d''équipe des Byzantins ?' ORDER BY id DESC LIMIT 1);
INSERT INTO answer (question_id, value, correct)
SELECT @q, t.value, t.correct FROM (
SELECT 'Les navires incendiaires attaquent plus vite' AS value, 0 AS correct
  UNION ALL SELECT 'Les moines soignent 100 % plus vite', 1
  UNION ALL SELECT 'Les chameliers coûtent moins cher', 0
  UNION ALL SELECT 'Les bâtiments ont plus de PV', 0
) t
WHERE NOT EXISTS (SELECT 1 FROM answer WHERE question_id = @q);

-- 38. Chez les Mayas, de combien les ressources durent-elles plus longtemps ?
INSERT INTO question (theme, type, civilisation, building, status, libelle, explication, author, created_at, modified_at)
SELECT 'TECH_TREE', 'MULTIPLE', 'MAYANS', 'NONE', 'CREATED_REVIEW', 'Chez les Mayas, de combien les ressources durent-elles plus longtemps ?', 'Chaque arbre, mine ou baie dure 15 % plus longtemps chez les Mayas.', @author_id, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM question WHERE libelle = 'Chez les Mayas, de combien les ressources durent-elles plus longtemps ?');
SET @q = (SELECT id FROM question WHERE libelle = 'Chez les Mayas, de combien les ressources durent-elles plus longtemps ?' ORDER BY id DESC LIMIT 1);
INSERT INTO answer (question_id, value, correct)
SELECT @q, t.value, t.correct FROM (
SELECT '15 %' AS value, 1 AS correct
  UNION ALL SELECT '25 %', 0
  UNION ALL SELECT '50 %', 0
  UNION ALL SELECT '10 %', 0
) t
WHERE NOT EXISTS (SELECT 1 FROM answer WHERE question_id = @q);

-- 39. Quel bonus de PV ont les navires de pêche japonais ?
INSERT INTO question (theme, type, civilisation, building, status, libelle, explication, author, created_at, modified_at)
SELECT 'TECH_TREE', 'MULTIPLE', 'JAPANESE', 'DOCK', 'CREATED_REVIEW', 'Quel bonus de PV ont les navires de pêche japonais ?', 'Les navires de pêche japonais ont le double de PV et travaillent de plus en plus vite à chaque âge.', @author_id, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM question WHERE libelle = 'Quel bonus de PV ont les navires de pêche japonais ?');
SET @q = (SELECT id FROM question WHERE libelle = 'Quel bonus de PV ont les navires de pêche japonais ?' ORDER BY id DESC LIMIT 1);
INSERT INTO answer (question_id, value, correct)
SELECT @q, t.value, t.correct FROM (
SELECT '+25 %' AS value, 0 AS correct
  UNION ALL SELECT '+50 %', 0
  UNION ALL SELECT 'Aucun', 0
  UNION ALL SELECT '+100 %', 1
) t
WHERE NOT EXISTS (SELECT 1 FROM answer WHERE question_id = @q);

-- 40. Quelle technologie permet aux moines de convertir les bâtiments et les armes de siège ennemis ?
INSERT INTO question (theme, type, civilisation, building, status, libelle, explication, author, created_at, modified_at)
SELECT 'TECH_TREE', 'MULTIPLE', 'NONE', 'MONASTERY', 'CREATED_REVIEW', 'Quelle technologie permet aux moines de convertir les bâtiments et les armes de siège ennemis ?', 'Rédemption pour les bâtiments et le siège (sauf forums, châteaux, merveilles…), Expiation pour convertir les moines ennemis.', @author_id, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM question WHERE libelle = 'Quelle technologie permet aux moines de convertir les bâtiments et les armes de siège ennemis ?');
SET @q = (SELECT id FROM question WHERE libelle = 'Quelle technologie permet aux moines de convertir les bâtiments et les armes de siège ennemis ?' ORDER BY id DESC LIMIT 1);
INSERT INTO answer (question_id, value, correct)
SELECT @q, t.value, t.correct FROM (
SELECT 'Ferveur' AS value, 0 AS correct
  UNION ALL SELECT 'Rédemption', 1
  UNION ALL SELECT 'Expiation', 0
  UNION ALL SELECT 'Théocratie', 0
) t
WHERE NOT EXISTS (SELECT 1 FROM answer WHERE question_id = @q);

-- 41. Combien coûte le passage à l'Âge féodal ?
INSERT INTO question (theme, type, civilisation, building, status, libelle, explication, author, created_at, modified_at)
SELECT 'TECH_TREE', 'MULTIPLE', 'NONE', 'TOWN_CENTER', 'CREATED_REVIEW', 'Combien coûte le passage à l''Âge féodal ?', '500 nourriture, puis 800 nourriture et 200 or pour l''Âge des châteaux, et 1000 nourriture et 800 or pour l''Âge impérial.', @author_id, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM question WHERE libelle = 'Combien coûte le passage à l''Âge féodal ?');
SET @q = (SELECT id FROM question WHERE libelle = 'Combien coûte le passage à l''Âge féodal ?' ORDER BY id DESC LIMIT 1);
INSERT INTO answer (question_id, value, correct)
SELECT @q, t.value, t.correct FROM (
SELECT '500 nourriture et 200 or' AS value, 0 AS correct
  UNION ALL SELECT '500 nourriture', 1
  UNION ALL SELECT '1000 nourriture', 0
  UNION ALL SELECT '800 nourriture et 200 or', 0
) t
WHERE NOT EXISTS (SELECT 1 FROM answer WHERE question_id = @q);

-- 42. Combien de bâtiments de l'Âge sombre faut-il avoir construit pour passer à l'Âge féodal ?
INSERT INTO question (theme, type, civilisation, building, status, libelle, explication, author, created_at, modified_at)
SELECT 'TECH_TREE', 'MULTIPLE', 'NONE', 'TOWN_CENTER', 'CREATED_REVIEW', 'Combien de bâtiments de l''Âge sombre faut-il avoir construit pour passer à l''Âge féodal ?', 'Deux bâtiments de l''Âge sombre sont requis, par exemple un moulin et une caserne.', @author_id, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM question WHERE libelle = 'Combien de bâtiments de l''Âge sombre faut-il avoir construit pour passer à l''Âge féodal ?');
SET @q = (SELECT id FROM question WHERE libelle = 'Combien de bâtiments de l''Âge sombre faut-il avoir construit pour passer à l''Âge féodal ?' ORDER BY id DESC LIMIT 1);
INSERT INTO answer (question_id, value, correct)
SELECT @q, t.value, t.correct FROM (
SELECT '1' AS value, 0 AS correct
  UNION ALL SELECT '3', 0
  UNION ALL SELECT '2', 1
  UNION ALL SELECT 'Aucun', 0
) t
WHERE NOT EXISTS (SELECT 1 FROM answer WHERE question_id = @q);

-- 43. Combien de villageois un forum teuton peut-il abriter en garnison ?
INSERT INTO question (theme, type, civilisation, building, status, libelle, explication, author, created_at, modified_at)
SELECT 'TECH_TREE', 'MULTIPLE', 'TEUTONS', 'TOWN_CENTER', 'CREATED_REVIEW', 'Combien de villageois un forum teuton peut-il abriter en garnison ?', 'Un forum abrite normalement 15 unités ; les Teutons ont +10 de capacité de garnison.', @author_id, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM question WHERE libelle = 'Combien de villageois un forum teuton peut-il abriter en garnison ?');
SET @q = (SELECT id FROM question WHERE libelle = 'Combien de villageois un forum teuton peut-il abriter en garnison ?' ORDER BY id DESC LIMIT 1);
INSERT INTO answer (question_id, value, correct)
SELECT @q, t.value, t.correct FROM (
SELECT '25' AS value, 1 AS correct
  UNION ALL SELECT '30', 0
  UNION ALL SELECT '15', 0
  UNION ALL SELECT '20', 0
) t
WHERE NOT EXISTS (SELECT 1 FROM answer WHERE question_id = @q);

COMMIT;
