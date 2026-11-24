package goodshi.ageofquizz.event.advent;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

/**
 * Question d'une case du calendrier de l'avent. Ces questions sont écrites en
 * dur dans {@link AdventQuestions} et n'ont aucun lien avec la table question,
 * sauf la case « rattrapage » ({@link Format#QUIZ_RETRY}) qui pose, en lecture
 * seule, une question du quiz ratée par le joueur.
 *
 * Pour ajouter un format : une valeur dans {@link Format}, une fabrique
 * statique ici, éventuellement des règles dans {@link #isValidAnswer} et
 * {@link #isCorrect}, et le composant d'affichage correspondant côté front
 * (src/features/advent/formats).
 *
 * @param mediaUrl        image facultative : URL absolue, ou chemin servi par
 *                        le front (ex. /advent/jour-3.png dans public/)
 * @param choices         propositions affichées (formats à choix / à
 *                        classer, colonne de gauche à relier), sinon vide
 * @param choiceImages    image de chaque proposition (même ordre que
 *                        {@code choices}), ou vide
 * @param targets         colonne de droite des questions à relier, sinon vide
 * @param targetImages    image de chaque cible (même ordre que
 *                        {@code targets}), ou vide
 * @param acceptedAnswers réponses acceptées, la première étant celle affichée
 *                        comme bonne réponse ; comparées sans accents ni casse
 * @param quiz            données propres aux formats QUIZ_RETRY / QUIZ, sinon
 *                        null
 * @param numbers         valeurs et marge du format NUMBERS, sinon null
 * @param grid            image et cases du format GRID, sinon null
 * @param zoomImages      format ZOOM : une image par essai, de la plus zoomée
 *                        à l'image entière (jamais envoyées d'un bloc au
 *                        front), sinon vide
 */
public record AdventQuestion(int day, Format format, String prompt, String mediaUrl, List<String> choices,
		List<String> choiceImages, List<String> targets, List<String> targetImages, List<String> acceptedAnswers,
		String explanation, Quiz quiz, Numbers numbers, Grid grid, List<String> zoomImages) {

	/** Sépare les éléments d'une réponse ORDER (même valeur côté front). */
	public static final String ORDER_SEPARATOR = " → ";
	/** Sépare les paires d'une réponse MATCH (même valeur côté front). */
	public static final String PAIR_SEPARATOR = " | ";
	/** Sépare les deux membres d'une paire MATCH (même valeur côté front). */
	public static final String PAIR_LINK = " = ";
	/** Sépare les nombres d'une réponse NUMBERS (même valeur côté front). */
	public static final String NUMBER_SEPARATOR = ",";
	/** Sépare les indices cochés d'une réponse QUIZ (même valeur côté front). */
	public static final String INDEX_SEPARATOR = ",";

	public enum Format {
		/** Une bonne réponse parmi des propositions (QCM, vrai/faux...). */
		CHOICE,
		/** Réponse tapée librement. */
		TEXT,
		/** Propositions à remettre dans le bon ordre (tout ou rien). */
		ORDER,
		/** Chaque élément de gauche à relier à un élément de droite (tout ou rien). */
		MATCH,
		/**
		 * Modèle de case « rattrapage » : jamais envoyé tel quel, remplacé pour
		 * chaque joueur par une question {@link #QUIZ} (voir AdventService).
		 */
		QUIZ_RETRY,
		/** Question du quiz classique : cocher la ou les bonnes réponses (tout ou rien). */
		QUIZ,
		/**
		 * Un nombre à donner par élément, juste s'il est dans la marge ; la case
		 * est réussie avec au moins {@link Numbers#minCorrect()} nombres justes.
		 */
		NUMBERS,
		/**
		 * Cocher toutes les propositions qui conviennent (ex. les affirmations
		 * fausses), sans savoir combien il y en a ; tout ou rien.
		 */
		SELECT,
		/**
		 * Façon captcha : une image découpée en cases, cocher celles qui
		 * contiennent ce qui est demandé (voir {@link Grid}).
		 */
		GRID,
		/**
		 * Réponse libre avec plusieurs essais gratuits : l'image se dézoome à
		 * chaque erreur (voir {@link #zoomImages}).
		 */
		ZOOM
	}

	/**
	 * Image découpée en {@code columns} colonnes ; cases numérotées à partir de
	 * 0, de gauche à droite puis de haut en bas. Les cases {@code required}
	 * doivent être cochées, les {@code optional} (bout d'unité visible...)
	 * peuvent l'être ou non, toute autre case cochée rend la réponse fausse.
	 */
	public record Grid(String image, int columns, int rows, Set<Integer> required, Set<Integer> optional) {
		public Grid {
			required = Set.copyOf(new TreeSet<>(required));
			optional = Set.copyOf(new TreeSet<>(optional));
		}

		/** « 3,9,15|16 » : obligatoires puis au choix, pour l'affichage de la correction. */
		public String describe() {
			return join(required) + "|" + join(optional);
		}

		private static String join(Set<Integer> indices) {
			return String.join(INDEX_SEPARATOR, new TreeSet<>(indices).stream().map(String::valueOf).toList());
		}
	}

	/**
	 * Valeurs attendues (même ordre que {@code choices}), marge relative
	 * (0.2 = ±20 %, arrondie à l'unité) et nombre minimum de valeurs justes.
	 */
	public record Numbers(List<Integer> values, double tolerance, int minCorrect) {
		public Numbers {
			values = List.copyOf(values);
		}

		/** Écart accepté pour une valeur, au moins 1. */
		public int marginFor(int value) {
			return Math.max(1, (int) Math.round(Math.abs(value) * tolerance));
		}

		public boolean accepts(int index, int given) {
			int expected = values.get(index);
			return Math.abs(given - expected) <= marginFor(expected);
		}
	}

	/** Proposition d'une question SELECT : à cocher ou non. */
	public record Statement(String text, boolean toSelect) {
		/** Proposition à cocher (ex. une affirmation fausse). */
		public static Statement select(String text) {
			return new Statement(text, true);
		}

		/** Proposition à laisser décochée. */
		public static Statement keep(String text) {
			return new Statement(text, false);
		}
	}

	/** Raccourci pour les formats sans valeurs numériques. */
	public AdventQuestion(int day, Format format, String prompt, String mediaUrl, List<String> choices,
			List<String> choiceImages, List<String> targets, List<String> targetImages, List<String> acceptedAnswers,
			String explanation, Quiz quiz) {
		this(day, format, prompt, mediaUrl, choices, choiceImages, targets, targetImages, acceptedAnswers,
				explanation, quiz, null, null, List.of());
	}

	/** Élément affiché, avec son image facultative. */
	public record Item(String label, String image) {
		public static Item of(String label, String image) {
			return new Item(label, image);
		}
	}

	/**
	 * Bonne paire d'une question à relier. Sans {@code leftLabel}, l'élément de
	 * gauche n'a qu'une image : son libellé (« A », « B »...) est attribué à
	 * l'affichage pour ne rien révéler.
	 */
	public record Pair(String leftLabel, String leftImage, Item right) {
		public static Pair of(String leftImage, String rightLabel, String rightImage) {
			return new Pair(null, leftImage, new Item(rightLabel, rightImage));
		}

		/** Image nommée (ex. portrait d'un héros) à relier à un simple nom. */
		public static Pair named(String leftLabel, String leftImage, String rightLabel) {
			return new Pair(leftLabel, leftImage, new Item(rightLabel, null));
		}

		/** Image à relier à un simple nom. */
		public static Pair of(String leftImage, String rightLabel) {
			return of(leftImage, rightLabel, null);
		}
	}

	/**
	 * Case qui pose une question du quiz classique.
	 *
	 * @param title          titre affiché au-dessus du texte d'introduction
	 * @param intro          introduction quand une question ratée a été trouvée
	 * @param fallbackIntro  introduction quand le joueur n'a rien raté (question
	 *                       tirée au hasard)
	 * @param questionId     question du quiz posée (QUIZ uniquement)
	 * @param fromFailure    question tirée parmi les erreurs du joueur
	 * @param mediaType      type de la question du quiz (IMAGE, SOUND...) pour
	 *                       afficher son média
	 * @param fileUrl        média de la question du quiz, tel que stocké
	 * @param correctIndices indices (dans {@code choices}) des bonnes réponses
	 */
	public record Quiz(String title, String intro, String fallbackIntro, Integer questionId, boolean fromFailure,
			String mediaType, String fileUrl, Set<Integer> correctIndices) {

		public Quiz {
			correctIndices = correctIndices == null ? Set.of() : Set.copyOf(correctIndices);
		}

		/** Introduction à afficher pour cette question. */
		public String shownIntro() {
			return fromFailure ? intro : fallbackIntro;
		}

		public boolean multiple() {
			return correctIndices.size() > 1;
		}
	}

	public AdventQuestion {
		choices = copy(choices);
		choiceImages = copy(choiceImages);
		targets = copy(targets);
		targetImages = copy(targetImages);
		zoomImages = copy(zoomImages);
		acceptedAnswers = List.copyOf(acceptedAnswers);
		if (acceptedAnswers.isEmpty()) {
			throw new IllegalArgumentException("Jour " + day + " : aucune réponse acceptée");
		}
		if (!choiceImages.isEmpty() && choiceImages.size() != choices.size()) {
			throw new IllegalArgumentException("Jour " + day + " : une image par proposition");
		}
		if (!targetImages.isEmpty() && targetImages.size() != targets.size()) {
			throw new IllegalArgumentException("Jour " + day + " : une image par cible");
		}
		if (format == Format.CHOICE && !choices.contains(acceptedAnswers.get(0))) {
			throw new IllegalArgumentException("Jour " + day + " : la bonne réponse n'est pas dans les propositions");
		}
		if (format == Format.ORDER && !isPermutation(acceptedAnswers.get(0), choices)) {
			throw new IllegalArgumentException("Jour " + day + " : l'ordre attendu ne reprend pas les propositions");
		}
		if (format == Format.MATCH && parsePairs(acceptedAnswers.get(0), choices, targets) == null) {
			throw new IllegalArgumentException("Jour " + day + " : les paires attendues sont incohérentes");
		}
		if ((format == Format.QUIZ_RETRY || format == Format.QUIZ) && quiz == null) {
			throw new IllegalArgumentException("Jour " + day + " : données du quiz manquantes");
		}
		int cellCount = choices.size();
		if (format == Format.GRID && (grid == null || grid.columns() * grid.rows() != cellCount
				|| grid.required().isEmpty()
				|| !java.util.stream.Stream.concat(grid.required().stream(), grid.optional().stream())
						.allMatch(i -> i >= 0 && i < cellCount)
				|| grid.required().stream().anyMatch(grid.optional()::contains))) {
			throw new IllegalArgumentException("Jour " + day + " : grille incohérente");
		}
		if (format == Format.ZOOM && zoomImages.size() < 2) {
			throw new IllegalArgumentException("Jour " + day + " : au moins deux images à dézoomer");
		}
		if (format == Format.NUMBERS && (numbers == null || numbers.values().size() != choices.size()
				|| numbers.minCorrect() < 1 || numbers.minCorrect() > choices.size())) {
			throw new IllegalArgumentException("Jour " + day + " : valeurs numériques incohérentes");
		}
	}

	/* ======================= Fabriques ======================= */

	/** QCM : {@code correctIndex} est l'indice (à partir de 0) de la bonne proposition. */
	public static AdventQuestion choice(int day, String prompt, List<String> choices, int correctIndex,
			String explanation) {
		return new AdventQuestion(day, Format.CHOICE, prompt, null, choices, List.of(), List.of(), List.of(),
				List.of(choices.get(correctIndex)), explanation, null);
	}

	public static AdventQuestion trueFalse(int day, String prompt, boolean answer, String explanation) {
		return choice(day, prompt, List.of("Vrai", "Faux"), answer ? 0 : 1, explanation);
	}

	/** Réponse libre : la première réponse acceptée est celle affichée après coup. */
	public static AdventQuestion text(int day, String prompt, List<String> acceptedAnswers, String explanation) {
		return new AdventQuestion(day, Format.TEXT, prompt, null, List.of(), List.of(), List.of(), List.of(),
				acceptedAnswers, explanation, null);
	}

	/**
	 * Classement : les éléments sont écrits <b>dans le bon ordre</b> ; ils sont
	 * affichés mélangés (toujours de la même façon pour une case donnée, et
	 * jamais déjà dans le bon ordre).
	 */
	public static AdventQuestion order(int day, String prompt, List<Item> itemsInOrder, String explanation) {
		if (itemsInOrder.size() < 2) {
			throw new IllegalArgumentException("Jour " + day + " : au moins deux éléments à classer");
		}
		List<Item> shown = shuffled(itemsInOrder, day * 7919L);
		boolean withImages = itemsInOrder.stream().anyMatch(i -> i.image() != null);
		String expected = String.join(ORDER_SEPARATOR, itemsInOrder.stream().map(Item::label).toList());
		return new AdventQuestion(day, Format.ORDER, prompt, null, shown.stream().map(Item::label).toList(),
				withImages ? shown.stream().map(Item::image).toList() : List.of(), List.of(), List.of(),
				List.of(expected), explanation, null);
	}

	/**
	 * Relier : les bonnes paires sont écrites telles quelles ; la colonne de
	 * gauche (libellés « A », « B »... selon l'ordre affiché) et celle de droite
	 * sont mélangées indépendamment.
	 */
	public static AdventQuestion match(int day, String prompt, List<Pair> pairs, String explanation) {
		if (pairs.size() < 2 || pairs.size() > 26) {
			throw new IllegalArgumentException("Jour " + day + " : entre 2 et 26 paires à relier");
		}
		List<Pair> left = shuffled(pairs, day * 104729L);
		List<Item> right = shuffled(pairs.stream().map(Pair::right).toList(), day * 1299709L);
		// Jamais déjà alignées ligne à ligne
		if (right.equals(left.stream().map(Pair::right).toList())) {
			Collections.rotate(right, 1);
		}

		List<String> leftLabels = new ArrayList<>();
		List<String> expected = new ArrayList<>();
		for (int i = 0; i < left.size(); i++) {
			String label = left.get(i).leftLabel() != null ? left.get(i).leftLabel()
					: String.valueOf((char) ('A' + i));
			leftLabels.add(label);
			expected.add(label + PAIR_LINK + left.get(i).right().label());
		}
		// Cibles sans image (ex. simples noms) : pas de liste d'images
		boolean targetsWithImages = right.stream().anyMatch(i -> i.image() != null);
		return new AdventQuestion(day, Format.MATCH, prompt, null, leftLabels,
				left.stream().map(Pair::leftImage).toList(), right.stream().map(Item::label).toList(),
				targetsWithImages ? right.stream().map(Item::image).toList() : List.of(),
				List.of(String.join(PAIR_SEPARATOR, expected)), explanation, null);
	}

	/** Paire de libellés (texte des deux côtés) d'une question à relier. */
	public record NamePair(String left, String right) {
		public static NamePair of(String left, String right) {
			return new NamePair(left, right);
		}
	}

	/**
	 * Relier, texte des deux côtés : les bonnes paires sont écrites telles
	 * quelles ; les deux colonnes sont mélangées indépendamment.
	 */
	public static AdventQuestion matchNames(int day, String prompt, List<NamePair> pairs, String explanation) {
		if (pairs.size() < 2) {
			throw new IllegalArgumentException("Jour " + day + " : au moins deux paires à relier");
		}
		List<NamePair> left = shuffled(pairs, day * 104729L);
		List<String> right = shuffled(pairs.stream().map(NamePair::right).toList(), day * 1299709L);
		// Jamais déjà alignées ligne à ligne
		if (right.equals(left.stream().map(NamePair::right).toList())) {
			Collections.rotate(right, 1);
		}
		String expected = String.join(PAIR_SEPARATOR,
				left.stream().map(p -> p.left() + PAIR_LINK + p.right()).toList());
		return new AdventQuestion(day, Format.MATCH, prompt, null, left.stream().map(NamePair::left).toList(),
				List.of(), right, List.of(), List.of(expected), explanation, null);
	}

	/**
	 * Rattrapage : la case pose une question du quiz ratée par le joueur (ou,
	 * s'il n'a rien raté, une question au hasard).
	 */
	public static AdventQuestion quizRetry(int day, String title, String intro, String fallbackIntro) {
		return new AdventQuestion(day, Format.QUIZ_RETRY, title, null, List.of(), List.of(), List.of(), List.of(),
				List.of("-"), null, new Quiz(title, intro, fallbackIntro, null, false, null, null, Set.of()));
	}

	/**
	 * Question concrète d'une case rattrapage pour un joueur.
	 *
	 * @param answers        réponses de la question du quiz, dans l'ordre à
	 *                       afficher : [libellé, bonne réponse ?]
	 */
	public AdventQuestion toQuiz(int questionId, boolean fromFailure, String libelle, String quizExplanation,
			String mediaType, String fileUrl, List<Map.Entry<String, Boolean>> answers) {
		List<String> labels = answers.stream().map(Map.Entry::getKey).toList();
		Set<Integer> correct = new TreeSet<>();
		for (int i = 0; i < answers.size(); i++) {
			if (Boolean.TRUE.equals(answers.get(i).getValue())) {
				correct.add(i);
			}
		}
		String shown = String.join(", ", correct.stream().map(labels::get).toList());
		return new AdventQuestion(day, Format.QUIZ, libelle, null, labels, List.of(), List.of(), List.of(),
				List.of(shown.isEmpty() ? "-" : shown), quizExplanation,
				new Quiz(quiz.title(), quiz.intro(), quiz.fallbackIntro(), questionId, fromFailure, mediaType,
						fileUrl, correct));
	}

	/** Élément du format NUMBERS : libellé, image facultative et valeur attendue. */
	public record Amount(String label, String image, int value) {
		public static Amount of(String label, int value) {
			return new Amount(label, null, value);
		}

		public static Amount of(String label, String image, int value) {
			return new Amount(label, image, value);
		}
	}

	/**
	 * Nombres à donner : chaque valeur est juste si elle est à ±{@code tolerance}
	 * près (0.2 = ±20 %) ; la case est réussie avec au moins
	 * {@code minCorrect} valeurs justes. Éléments affichés dans l'ordre donné.
	 */
	public static AdventQuestion numbers(int day, String prompt, List<Amount> amounts, double tolerance,
			int minCorrect, String explanation) {
		boolean withImages = amounts.stream().anyMatch(a -> a.image() != null);
		List<Integer> values = amounts.stream().map(Amount::value).toList();
		String expected = String.join(NUMBER_SEPARATOR, values.stream().map(String::valueOf).toList());
		return new AdventQuestion(day, Format.NUMBERS, prompt, null, amounts.stream().map(Amount::label).toList(),
				withImages ? amounts.stream().map(Amount::image).toList() : List.of(), List.of(), List.of(),
				List.of(expected), explanation, null, new Numbers(values, tolerance, minCorrect), null, List.of());
	}

	/**
	 * Cocher toutes les propositions marquées {@link Statement#select} ;
	 * affichées dans l'ordre donné, au moins une à cocher.
	 */
	public static AdventQuestion select(int day, String prompt, List<Statement> statements, String explanation) {
		List<String> texts = statements.stream().map(Statement::text).toList();
		Set<Integer> toSelect = new TreeSet<>();
		for (int i = 0; i < statements.size(); i++) {
			if (statements.get(i).toSelect()) {
				toSelect.add(i);
			}
		}
		if (toSelect.isEmpty()) {
			throw new IllegalArgumentException("Jour " + day + " : au moins une proposition à cocher");
		}
		String shown = String.join(" / ", toSelect.stream().map(texts::get).toList());
		return new AdventQuestion(day, Format.SELECT, prompt, null, texts, List.of(), List.of(), List.of(),
				List.of(shown), explanation, new Quiz(null, null, null, null, false, null, null, toSelect));
	}

	/**
	 * Captcha : {@code image} découpée en {@code columns} × {@code rows} cases,
	 * numérotées <b>à partir de 1</b> ici (comme on les compte à l'œil), de
	 * gauche à droite puis de haut en bas.
	 */
	public static AdventQuestion grid(int day, String prompt, String image, int columns, int rows,
			List<Integer> requiredFrom1, List<Integer> optionalFrom1, String explanation) {
		List<String> cells = new ArrayList<>();
		for (int i = 0; i < columns * rows; i++) {
			cells.add(String.valueOf(i + 1));
		}
		Grid grid = new Grid(image, columns, rows, toIndices(requiredFrom1), toIndices(optionalFrom1));
		return new AdventQuestion(day, Format.GRID, prompt, null, cells, List.of(), List.of(), List.of(),
				List.of(grid.describe()), explanation, null, null, grid, List.of());
	}

	private static Set<Integer> toIndices(List<Integer> from1) {
		Set<Integer> result = new TreeSet<>();
		for (int n : from1) {
			result.add(n - 1);
		}
		return result;
	}

	public AdventQuestion withMedia(String url) {
		return new AdventQuestion(day, format, prompt, url, choices, choiceImages, targets, targetImages,
				acceptedAnswers, explanation, quiz, numbers, grid, zoomImages);
	}

	/**
	 * Image zoomée à deviner en réponse libre : un essai gratuit par image de
	 * {@code imagesFromZoomed} (de la plus zoomée à l'image entière), l'image
	 * suivante s'affichant après chaque erreur.
	 */
	public static AdventQuestion zoom(int day, String prompt, List<String> imagesFromZoomed,
			List<String> acceptedAnswers, String explanation) {
		return new AdventQuestion(day, Format.ZOOM, prompt, null, List.of(), List.of(), List.of(), List.of(),
				acceptedAnswers, explanation, null, null, null, imagesFromZoomed);
	}

	/** Essais gratuits (hors jokers) : un par image pour ZOOM, sinon un seul. */
	public int maxAttempts() {
		return format == Format.ZOOM ? zoomImages.size() : 1;
	}

	/**
	 * Image ZOOM à montrer après {@code attemptsUsed} essais ; l'image entière
	 * une fois la case terminée ({@code finished}). Null pour les autres formats.
	 */
	public String zoomImage(int attemptsUsed, boolean finished) {
		if (format != Format.ZOOM) {
			return null;
		}
		int last = zoomImages.size() - 1;
		return zoomImages.get(finished ? last : Math.min(attemptsUsed, last));
	}

	/* ======================= Vérification ======================= */

	/** Réponse recevable (sinon le joueur ne doit pas perdre son unique essai). */
	public boolean isValidAnswer(String answer) {
		if (answer == null || answer.isBlank()) {
			return false;
		}
		return switch (format) {
		case CHOICE -> choices.contains(answer);
		case ORDER -> isPermutation(answer, choices);
		case MATCH -> parsePairs(answer, choices, targets) != null;
		case QUIZ, SELECT, GRID -> parseIndices(answer) != null;
		case NUMBERS -> parseNumbers(answer) != null;
		case QUIZ_RETRY -> false;
		case TEXT, ZOOM -> true;
		};
	}

	public boolean isCorrect(String answer) {
		if (format == Format.MATCH) {
			// Les paires peuvent arriver dans n'importe quel ordre
			Map<String, String> given = parsePairs(answer, choices, targets);
			return given != null && given.equals(parsePairs(correctAnswer(), choices, targets));
		}
		if (format == Format.NUMBERS) {
			return countCorrectNumbers(answer) >= numbers.minCorrect();
		}
		if (format == Format.GRID) {
			Set<Integer> given = parseIndices(answer);
			return given != null && given.containsAll(grid.required()) && given.stream()
					.allMatch(i -> grid.required().contains(i) || grid.optional().contains(i));
		}
		if (format == Format.QUIZ || format == Format.SELECT) {
			Set<Integer> given = parseIndices(answer);
			return given != null && !quiz.correctIndices().isEmpty() && given.equals(quiz.correctIndices());
		}
		String normalized = normalize(answer);
		return acceptedAnswers.stream().anyMatch(a -> normalize(a).equals(normalized));
	}

	/** Réponse telle qu'elle est enregistrée et réaffichée au joueur. */
	public String displayAnswer(String answer) {
		if (format == Format.QUIZ || format == Format.SELECT) {
			Set<Integer> indices = parseIndices(answer);
			String separator = format == Format.SELECT ? " / " : ", ";
			return indices == null ? answer : String.join(separator, indices.stream().map(choices::get).toList());
		}
		return answer;
	}

	public String correctAnswer() {
		return acceptedAnswers.get(0);
	}

	/** {@code answer} reprend chaque proposition exactement une fois. */
	private static boolean isPermutation(String answer, List<String> choices) {
		List<String> parts = Arrays.stream(answer.trim().split(ORDER_SEPARATOR.trim())).map(String::trim).toList();
		return parts.size() == choices.size() && new HashSet<>(parts).equals(new HashSet<>(choices));
	}

	/** Nombre de valeurs dans la marge (0 si la réponse est invalide). */
	public int countCorrectNumbers(String answer) {
		List<Integer> given = parseNumbers(answer);
		if (given == null) {
			return 0;
		}
		int count = 0;
		for (int i = 0; i < given.size(); i++) {
			if (numbers.accepts(i, given.get(i))) {
				count++;
			}
		}
		return count;
	}

	/** « 350,800,... » (une valeur entière >= 0 par élément), ou null. */
	private List<Integer> parseNumbers(String answer) {
		String[] parts = answer.split(NUMBER_SEPARATOR, -1);
		if (parts.length != choices.size()) {
			return null;
		}
		List<Integer> result = new ArrayList<>();
		for (String part : parts) {
			try {
				int value = Integer.parseInt(part.trim());
				if (value < 0 || value > 1_000_000) {
					return null;
				}
				result.add(value);
			} catch (NumberFormatException e) {
				return null;
			}
		}
		return result;
	}

	/** « 0,2 » -> {0, 2}, ou null si un indice est invalide ou en double. */
	private Set<Integer> parseIndices(String answer) {
		Set<Integer> result = new TreeSet<>();
		for (String part : answer.split(INDEX_SEPARATOR)) {
			try {
				int index = Integer.parseInt(part.trim());
				if (index < 0 || index >= choices.size() || !result.add(index)) {
					return null;
				}
			} catch (NumberFormatException e) {
				return null;
			}
		}
		return result.isEmpty() ? null : result;
	}

	/**
	 * « A = X | B = Y... » vers gauche -> droite, ou null si chaque élément de
	 * gauche et de droite n'apparaît pas exactement une fois.
	 */
	private static Map<String, String> parsePairs(String answer, List<String> left, List<String> right) {
		Map<String, String> pairs = new LinkedHashMap<>();
		for (String part : answer.split(Pattern.quote(PAIR_SEPARATOR.trim()))) {
			String[] sides = part.split(Pattern.quote(PAIR_LINK.trim()), 2);
			if (sides.length != 2) {
				return null;
			}
			String l = sides[0].trim();
			String r = sides[1].trim();
			if (!left.contains(l) || !right.contains(r) || pairs.put(l, r) != null) {
				return null;
			}
		}
		boolean complete = pairs.size() == left.size()
				&& new HashSet<>(pairs.values()).equals(new HashSet<>(right));
		return complete ? new HashMap<>(pairs) : null;
	}

	/** Mélange stable pour une graine donnée, jamais identique à l'original. */
	static <T> List<T> shuffled(List<T> items, long seed) {
		List<T> result = new ArrayList<>(items);
		Collections.shuffle(result, new Random(seed));
		if (result.size() > 1 && result.equals(items)) {
			Collections.rotate(result, 1);
		}
		return result;
	}

	private static List<String> copy(List<String> list) {
		return list == null ? List.of() : List.copyOf(list);
	}

	static String normalize(String value) {
		if (value == null) {
			return "";
		}
		return Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}", "").replace('’', '\'')
				.replaceAll("\\s+", " ").trim().toLowerCase(Locale.ROOT);
	}
}
