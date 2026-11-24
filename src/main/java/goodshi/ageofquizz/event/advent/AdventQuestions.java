package goodshi.ageofquizz.event.advent;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import goodshi.ageofquizz.event.advent.AdventQuestion.Amount;
import goodshi.ageofquizz.event.advent.AdventQuestion.Item;
import goodshi.ageofquizz.event.advent.AdventQuestion.NamePair;
import goodshi.ageofquizz.event.advent.AdventQuestion.Pair;
import goodshi.ageofquizz.event.advent.AdventQuestion.Statement;

/**
 * Les questions du calendrier de l'avent, une par case (jour 1 à
 * {@link #DAYS}). Une case sans question s'affiche « bientôt disponible ».
 *
 * Les réponses restent côté serveur : le front ne reçoit une question qu'une
 * fois sa case débloquée, et jamais les réponses acceptées avant d'avoir
 * répondu.
 */
public final class AdventQuestions {

	public static final int DAYS = 24;

	// Images : public/images/advent/ côté front (pas public/advent : conflit avec la route /advent).
	private static final List<AdventQuestion> ALL = List.of(

			// Éléments écrits dans le bon ordre (du plus ancien au plus récent).
			AdventQuestion.order(1, "Classe ces DLC par ordre de sortie, du plus ancien au plus récent.", List.of(
					Item.of("Return of Rome", "/images/advent/dlc-return-of-rome.png"),
					Item.of("The Mountain Royals", "/images/advent/dlc-mountain-royals.png"),
					Item.of("The Three Kingdoms", "/images/advent/dlc-three-kingdoms.png"),
					Item.of("The Last Chieftains", "/images/advent/dlc-last-chieftains.png"),
					Item.of("The Viking Sagas", "/images/advent/dlc-viking-sagas.png")),
					"Return of Rome (16 mai 2023), The Mountain Royals (31 octobre 2023), The Three Kingdoms "
							+ "(6 mai 2025), The Last Chieftains (17 février 2026) et The Viking Sagas "
							+ "(22 septembre 2026)."),

			// Nom d'image neutre : il est visible dans le navigateur.
			AdventQuestion.text(2, "Quelle unité se cache derrière cette silhouette ?",
					List.of("Le samouraï", "Samouraï", "Samouraïs", "Les samouraïs", "Samurai", "Samurais",
							"Le samurai", "Samouraï d'élite", "Samurai d'élite", "Elite samurai"),
					"C'est le samouraï, l'unité unique des Japonais, redoutable contre les autres unités uniques.")
					.withMedia("/images/advent/jour-02.png"),

			// Images des châteaux aux noms aléatoires : ils ne doivent pas trahir les paires.
			AdventQuestion.match(3, "Relie chaque château à sa civilisation.", List.of(
					Pair.of("/images/advent/jour-03-e7ca5a.png", "Francs", "/images/advent/jour-03-blason-francs.png"),
					Pair.of("/images/advent/jour-03-5c3a07.png", "Vietnamiens", "/images/advent/jour-03-blason-vietnamiens.png"),
					Pair.of("/images/advent/jour-03-3ac643.png", "Byzantins", "/images/advent/jour-03-blason-byzantins.png"),
					Pair.of("/images/advent/jour-03-8329d5.png", "Mongols", "/images/advent/jour-03-blason-mongols.png"),
					Pair.of("/images/advent/jour-03-53c42d.png", "Hindoustanis", "/images/advent/jour-03-blason-hindoustanis.png")),
					"Chaque civilisation a son style de château : tours coniques pour les Francs, coupoles pour "
							+ "les Byzantins, pagodes pour les Mongols..."),

			AdventQuestion.text(4, "Quelle est l'unité unique partagée par les Lituaniens et les Polonais ?",
					List.of("Le hussard ailé", "Hussard ailé", "Hussards ailés", "Les hussards ailés",
							"Hussard aile", "Winged hussar", "Winged hussars", "Hussar ailé"),
					"Le hussard ailé remplace l'amélioration du hussard chez les Polonais et les Lituaniens."),

			// Images des cartes aux noms aléatoires : ils ne doivent pas trahir les paires.
			AdventQuestion.match(5, "Spécial Désert : relie chaque carte à son nom.", List.of(
					Pair.of("/images/advent/jour-05-b7bfee.png", "Arabie"),
					Pair.of("/images/advent/jour-05-68acd5.png", "Haboob"),
					Pair.of("/images/advent/jour-05-723789.png", "Atacama"),
					Pair.of("/images/advent/jour-05-7dd58a.png", "Ruée vers l'or (Gold Rush)")),
					null),

			// Question tirée parmi celles du quiz que le joueur a ratées avant le jour de la case.
			AdventQuestion.quizRetry(6, "Rattrapage de Noël",
					"Oh oh oh ! J'ai vu qu'une question du quiz t'avait échappé et je te donne la "
							+ "possibilité de te rattraper ! Voici la question :",
					"Oh oh oh ! Tu n'as raté aucune question du quiz, bravo ! Alors voici une question tirée "
							+ "au hasard pour toi :"),

			// Photos aux noms aléatoires : ils ne doivent pas trahir les paires.
			AdventQuestion.match(7, "Guess the Star : relie chaque photo au pseudo du joueur.", List.of(
					Pair.of("/images/advent/jour-07-f08b1b.png", "Viper"),
					Pair.of("/images/advent/jour-07-f10db6.png", "Hera"),
					Pair.of("/images/advent/jour-07-857820.png", "Lewis"),
					Pair.of("/images/advent/jour-07-da40e9.png", "MbL"),
					Pair.of("/images/advent/jour-07-658d73.png", "Sitaux")),
					null),

			// Images des cartes aux noms aléatoires : ils ne doivent pas trahir les paires.
			AdventQuestion.match(8, "Guess the Map, spécial Hybrid : relie chaque carte à son nom.", List.of(
					Pair.of("/images/advent/jour-08-79f9ff.png", "Baltic"),
					Pair.of("/images/advent/jour-08-fb3826.png", "Mediterranean"),
					Pair.of("/images/advent/jour-08-ca2ad5.png", "Crater Lake"),
					Pair.of("/images/advent/jour-08-67b0d0.png", "Archipelago"),
					Pair.of("/images/advent/jour-08-4ef0ae.png", "Team Islands")),
					null),

			// Éléments écrits dans le bon ordre (du moins cher au plus cher en nourriture).
			AdventQuestion.order(9, "Classe ces unités par coût en nourriture croissant.", List.of(
					Item.of("Archer", "/images/advent/jour-09-archer.png"),
					Item.of("Tirailleur", "/images/advent/jour-09-tirailleur.png"),
					Item.of("Lancier", "/images/advent/jour-09-lancier.png"),
					Item.of("Villageois", "/images/advent/jour-09-villageois.png"),
					Item.of("Milicien", "/images/advent/jour-09-milicien.png"),
					Item.of("Cavalerie d'éclairage", "/images/advent/jour-09-cavalerie-eclairage.png")),
					"Archer : 0 (25 bois, 45 or) · Tirailleur : 25 · Lancier : 35 · Villageois : 50 · "
							+ "Milicien : 60 · Cavalerie d'éclairage : 80 nourriture."),

			AdventQuestion.matchNames(10, "Relie chaque civilisation à son unité unique.", List.of(
					NamePair.of("Wu", "Archer de feu"),
					NamePair.of("Wei", "Cavalier tigre"),
					NamePair.of("Jurchens", "Pagode de fer"),
					NamePair.of("Danois", "Troupe du foyer")),
					null),

			// ±20 %, réussie avec au moins 8 valeurs justes sur 10.
			AdventQuestion.numbers(11, "Indique la quantité de ressources de chacun de ces éléments.", List.of(
					Amount.of("Mine de pierre", 350),
					Amount.of("Mine d'or", 800),
					Amount.of("Poisson côtier", 200),
					Amount.of("Baobab", 200),
					Amount.of("Sanglier", 340),
					Amount.of("Lapin", 75),
					Amount.of("Renard", 35),
					Amount.of("Mouton", 100),
					Amount.of("Vache", 150),
					Amount.of("Lama", 100)),
					0.2, 8, null),

			// select = affirmation fausse (à cocher), keep = affirmation vraie.
			AdventQuestion.select(12, "Fake News : coche toutes les fausses informations.", List.of(
					Statement.keep("L'Arambai n'est affecté que par une seule UP de la forge."),
					Statement.keep("Le Ratha est affecté par les UP d'attaque des archers, les UP d'attaque de la "
							+ "cavalerie et les UP de défense des archers."),
					Statement.select("L'éléphant de guerre perse est affecté par Expert de siège."),
					Statement.keep("Le Mamelouk est affecté par les UP de défense de la cavalerie."),
					Statement.select("Les Gbetos sont affectés par les UP des archers.")),
					null),

			// Une seule proposition vraie : l'indice 9 (Polonais).
			AdventQuestion.choice(13, "Trouve la vérité : une seule de ces propositions est vraie.", List.of(
					"Un forum malien coûte 225 de bois et 100 de pierre.",
					"Un forum britannique coûte 125 de bois et 100 de pierre.",
					"Les Incas disposent de l'unité guerrier aigle.",
					"Les Jurchens ont le trébuchet à traction.",
					"Les Khitans ont le lou chuan.",
					"Les Turcs ont Expert de siège.",
					"Les Huns ont Expert de siège.",
					"Les Chinois ont le brûlot rapide.",
					"Les Sarrasins ont Caravansérail.",
					"Les Polonais n'ont pas le hallebardier."), 9,
					null),

			// Images des cartes aux noms aléatoires : ils ne doivent pas trahir les paires.
			AdventQuestion.match(14, "Guess the Map, spécial maps fermées : relie chaque carte à son nom.",
					List.of(Pair.of("/images/advent/jour-14-39eee9.png", "Arène"),
							Pair.of("/images/advent/jour-14-94ad40.png", "Clairière fortifiée (Fortified Clearing)"),
							Pair.of("/images/advent/jour-14-585107.png", "Forteresse"),
							Pair.of("/images/advent/jour-14-398310.png", "Refuge (Hideout)"),
							Pair.of("/images/advent/jour-14-8b65fc.png", "Michi")),
					null),

			AdventQuestion.matchNames(15, "Relie chaque numéro de taunt à sa phrase.", List.of(
					NamePair.of("11", "(Rire de Herb)"),
					NamePair.of("14", "Start the game already!"),
					NamePair.of("19", "Long time, no siege."),
					NamePair.of("26", "The wonder, the wonder, the... no!"),
					NamePair.of("30", "Wololo!")),
					null),

			// select = constructible à l'âge sombre (à cocher), keep = âge féodal.
			AdventQuestion.select(16, "Coche tous les bâtiments constructibles dès l'âge sombre.", List.of(
					Statement.select("Maison"),
					Statement.keep("Forge"),
					Statement.select("Moulin"),
					Statement.keep("Écurie"),
					Statement.select("Port"),
					Statement.keep("Marché"),
					Statement.select("Caserne"),
					Statement.keep("Tour de guet"),
					Statement.select("Palissade"),
					Statement.select("Camp de mineurs")),
					"Âge sombre : maison, moulin, port, caserne, palissade et camp de mineurs. La forge, "
							+ "l'écurie, le marché et la tour de guet demandent l'âge féodal."),

			// Captcha : cases numérotées de 1 à 24, de gauche à droite puis de haut en bas.
			// Obligatoires : béliers, onagre, tours de siège, trébuchet ; au choix : engins à peine
			// visibles (12 : sommet d'une tour de siège, 23 : bord d'un bélier).
			AdventQuestion.grid(17, "des unités de siège", "/images/advent/jour-17-4f13a5.jpg", 6, 4,
					List.of(17, 18, 20, 21, 22, 24), List.of(12, 23),
					"Béliers, onagre, tours de siège et trébuchet : il y en avait dans les cases 17, 18, 20, "
							+ "21, 22 et 24."),

			// Portraits nommés (noms de fichiers neutres).
			AdventQuestion.match(18, "Relie chaque héros de campagne à sa civilisation.", List.of(
					Pair.named("Barberousse", "/images/advent/jour-18-9f7fbd.png", "Teutons"),
					Pair.named("Sundjata", "/images/advent/jour-18-84a35f.png", "Maliens"),
					Pair.named("Itzcoatl", "/images/advent/jour-18-676ead.png", "Aztèques"),
					Pair.named("Tamerlan", "/images/advent/jour-18-e144c9.png", "Tatars"),
					Pair.named("Lê Lợi", "/images/advent/jour-18-5abfae.png", "Vietnamiens")),
					"Barberousse mène les Teutons, Sundjata les Maliens, Itzcoatl les Aztèques, Tamerlan les "
							+ "Tatars et Lê Lợi les Vietnamiens."),

			// Images des cartes aux noms aléatoires : ils ne doivent pas trahir les paires.
			AdventQuestion.match(20, "Guess the Map, spécial Glace : relie chaque carte à son nom.", List.of(
					Pair.of("/images/advent/jour-20-d8f84c.png", "Scandinavia"),
					Pair.of("/images/advent/jour-20-ef4349.png", "Lac fantôme (Ghost Lake)"),
					Pair.of("/images/advent/jour-20-422388.png", "Glaciers par équipe (Team Glaciers)"),
					Pair.of("/images/advent/jour-20-bcdd14.png", "Groenland (Greenland)"),
					Pair.of("/images/advent/jour-20-9d2432.png", "Colline du loup (Wolf Hill)")),
					null),

			// ±20 %, réussie avec au moins 8 valeurs justes sur 10. Valeurs de base, sans UP ni bonus.
			AdventQuestion.numbers(21, "Indique les points de vie de chacun de ces éléments, sans aucune "
					+ "amélioration ni bonus de civilisation.", List.of(
							Amount.of("Chevalier", 100),
							Amount.of("Moine", 30),
							Amount.of("Forum", 2400),
							Amount.of("Lancier", 45),
							Amount.of("Villageois", 25),
							Amount.of("Château", 4800),
							Amount.of("Cavalerie d'éclairage", 45),
							Amount.of("Archer", 30),
							Amount.of("Maison (à l'âge sombre)", 550),
							Amount.of("Milicien", 40)),
					0.2, 8, null),

			// Une image par essai, de la plus zoomée à l'icône entière ; noms neutres.
			AdventQuestion.zoom(19, "Zoom extrême : quelle unité se cache derrière cette image ? "
					+ "Tu as 3 essais, l'image se dézoome à chaque erreur.", List.of(
							"/images/advent/jour-19-173f61.png",
							"/images/advent/jour-19-d36681.png",
							"/images/advent/jour-19-8b08fc.png"),
					List.of("Le guerrier à shotel", "Guerrier à shotel", "Guerriers à shotel",
							"Les guerriers à shotel", "Guerrier shotel", "Guerriers shotel", "Shotel",
							"Guerrier a shotel", "Shotel warrior", "Shotel warriors",
							"Guerrier à shotel d'élite", "Elite shotel warrior"),
					"C'est le guerrier à shotel, l'unité unique des Éthiopiens : une infanterie très "
							+ "rapide à produire, armée de deux épées recourbées."),

			// Éléments écrits dans le bon ordre (du plus lent au plus rapide), vitesses de base.
			AdventQuestion.order(22, "Classe ces unités de la plus lente à la plus rapide.", List.of(
					Item.of("Bélier (vide)", null),
					Item.of("Moine", null),
					Item.of("Villageois", null),
					Item.of("Milicien", null),
					Item.of("Cavalerie d'éclairage", null),
					Item.of("Chevalier", null)),
					"Bélier vide : 0,5 · Moine : 0,7 · Villageois : 0,8 · Milicien : 0,9 · "
							+ "Cavalerie d'éclairage : 1,2 · Chevalier : 1,35."),

			// Éléments écrits dans le bon ordre (du plus ancien au plus récent).
			AdventQuestion.order(23, "Classe ces héros de campagne du plus ancien au plus récent.", List.of(
					Item.of("Attila", null),
					Item.of("El Cid", null),
					Item.of("Gengis Khan", null),
					Item.of("Jeanne d'Arc", null),
					Item.of("Bayinnaung", null)),
					"Attila (Ve siècle), El Cid (XIe siècle), Gengis Khan (XIIe-XIIIe siècles), "
							+ "Jeanne d'Arc (XVe siècle) et Bayinnaung (XVIe siècle)."),

			// Énigme piège : tout évoque le Père Noël, mais c'est le moine.
			AdventQuestion.text(24, "Énigme de Noël : qui suis-je ?\n\n"
					+ "Vêtu de rouge (du moins chez le bon joueur), je suis attendu partout avec impatience. "
					+ "Je parcours le monde pour récupérer de précieux trésors, que je dépose ensuite un par un "
					+ "dans une maison bien particulière. Ces cadeaux font le bonheur de toute ma famille : "
					+ "ils lui rapportent de l'or sans qu'elle ait rien à faire. Je réconforte les blessés, "
					+ "je ne me presse jamais, et mon cri célèbre tient en trois syllabes qui se ressemblent. "
					+ "D'un seul mot magique, je transforme même tes ennemis en amis.",
					List.of("Le moine", "Moine", "Moines", "Les moines", "Un moine", "Monk", "Monks",
							"Le moine Wololo", "Wololo"),
					"Ce n'était pas le Père Noël mais le moine ! Robe rouge chez le joueur 2, reliques déposées "
							+ "au monastère qui rapportent de l'or, soins des unités, unité lente... et ce n'est pas "
							+ "« Ho-ho-ho » mais « Wo-lo-lo » !")
	);

	private static final Map<Integer, AdventQuestion> BY_DAY = index(ALL);

	private AdventQuestions() {
	}

	public static Optional<AdventQuestion> forDay(int day) {
		return Optional.ofNullable(BY_DAY.get(day));
	}

	public static Set<Integer> availableDays() {
		return BY_DAY.keySet();
	}

	/** Une erreur de saisie (jour en double ou hors calendrier) empêche le démarrage. */
	private static Map<Integer, AdventQuestion> index(List<AdventQuestion> questions) {
		Set<Integer> seen = new HashSet<>();
		for (AdventQuestion q : questions) {
			if (q.day() < 1 || q.day() > DAYS) {
				throw new IllegalStateException("Calendrier de l'avent : jour hors limites " + q.day());
			}
			if (!seen.add(q.day())) {
				throw new IllegalStateException("Calendrier de l'avent : jour en double " + q.day());
			}
		}
		return questions.stream().collect(Collectors.toUnmodifiableMap(AdventQuestion::day, Function.identity()));
	}
}
