package goodshi.ageofquizz.event.advent;

import java.time.LocalDate;
import java.util.List;

import goodshi.ageofquizz.event.advent.AdventQuestion.Format;

/** Échanges JSON du calendrier de l'avent. */
public final class AdventDtos {

	private AdventDtos() {
	}

	/**
	 * État du calendrier. {@code active} vaut false hors de la période de
	 * l'event : le front masque alors l'entrée. {@code score} et
	 * {@code answeredCount} sont null pour un visiteur non connecté.
	 * {@code preview} : admin en mode test (toutes les cases ouvertes, même
	 * hors période ; absent du classement).
	 */
	public record CalendarDTO(boolean active, LocalDate startDate, LocalDate endDate, LocalDate today,
			int totalDays, int unlockedDays, List<DaySummaryDTO> days, Integer score, Integer answeredCount,
			JokersDTO jokers, boolean preview) {
	}

	/**
	 * Jokers du joueur connecté (null pour un visiteur).
	 *
	 * @param questionsPlayed  questions de quiz jouées depuis le début de l'event
	 * @param nextJokerIn      questions restantes avant le prochain joker
	 */
	public record JokersDTO(int available, int earned, int used, long questionsPlayed, int questionsPerJoker,
			int nextJokerIn) {
	}

	/**
	 * @param available  une question existe pour cette case
	 * @param correct    null tant que le joueur n'a pas répondu
	 * @param jokersUsed jokers dépensés sur cette case
	 * @param inProgress format ZOOM : erreur, mais il reste des essais gratuits
	 */
	public record DaySummaryDTO(int day, boolean unlocked, boolean available, boolean answered, Boolean correct,
			int jokersUsed, boolean inProgress) {
	}

	/**
	 * Question d'une case, sans les réponses ; {@code result} est rempli une
	 * fois répondu. Case rattrapage (format QUIZ) : {@code title} et
	 * {@code intro} précèdent la question, {@code quizMediaType} /
	 * {@code quizFileUrl} décrivent son média tel que stocké pour le quiz, et
	 * {@code multiple} indique plusieurs bonnes réponses à cocher. Format
	 * NUMBERS : {@code tolerance} (0.2 = ±20 %) et {@code minCorrect} valeurs
	 * justes pour réussir. Format GRID : {@code gridImage} découpée en
	 * {@code gridColumns} colonnes (cases = {@code choices}). Format ZOOM :
	 * {@code maxAttempts} essais gratuits, {@code mediaUrl} étant l'image de
	 * l'essai en cours.
	 */
	public record DayDTO(int day, Format format, String prompt, String mediaUrl, List<String> choices,
			List<String> choiceImages, List<String> targets, List<String> targetImages, String title, String intro,
			String quizMediaType, String quizFileUrl, boolean multiple, Double tolerance, Integer minCorrect,
			String gridImage, Integer gridColumns, int maxAttempts, ResultDTO result) {
	}

	/**
	 * Après une erreur, la bonne réponse et l'explication restent null tant
	 * que le joueur ne les a pas révélées : il peut encore rejouer la case avec
	 * un joker (maintenant ou plus tard). Les révéler ferme la case.
	 *
	 * @param canRetry     erreur non révélée et au moins un joker disponible
	 * @param attemptsLeft essais gratuits restants (format ZOOM), sinon 0
	 * @param mediaUrl     format ZOOM : image à afficher désormais, sinon null
	 */
	public record ResultDTO(String answer, boolean correct, boolean revealed, String correctAnswer,
			String explanation, int jokersUsed, boolean canRetry, int attempts, int attemptsLeft, String mediaUrl) {
	}

	/** {@code useJoker} : rejouer une case ratée en dépensant un joker. */
	public record AnswerRequest(String answer, boolean useJoker) {
	}

	public record LeaderboardEntryDTO(int rank, Long userId, Long userProfileId, String username, int score,
			int answeredCount, int jokersUsed, boolean me) {
	}
}
