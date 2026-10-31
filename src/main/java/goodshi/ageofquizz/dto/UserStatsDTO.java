package goodshi.ageofquizz.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Statistiques publiques d'un joueur, affichées sur sa page profil.
 * {@code dailyRiddle} et {@code author} sont null quand le joueur n'a jamais
 * joué à l'énigme du jour / créé de question.
 */
public record UserStatsDTO(Overview overview, List<CategoryStat> byTheme, List<CategoryStat> byCivilisation,
		List<WeeklyProgress> progress, MultiplayerStats multiplayer, DailyRiddleStatsDTO dailyRiddle,
		AuthorStats author) {

	/** Toutes les questions répondues, solo et multijoueur confondus. */
	public record Overview(int questionsAnswered, int correctAnswers, double averageResponseTimeSeconds) {
	}

	/** {@code key} = valeur de l'enum (thème ou civilisation). */
	public record CategoryStat(String key, int answered, int correct) {
	}

	/**
	 * Questions répondues pendant la semaine commençant le lundi
	 * {@code weekStart} (semaines sans réponse incluses, answered = 0).
	 */
	public record WeeklyProgress(LocalDate weekStart, int answered, int correct) {
	}

	/** {@code perfectGames} = parties où le joueur a eu toutes les bonnes réponses. */
	public record MultiplayerStats(int gamesPlayed, int wins, int podiums, int perfectGames, int bestPoints,
			double averageAccuracy, List<RecentGame> recentGames) {
	}

	public record RecentGame(String gameSessionId, Instant playedAt, int rank, int playerCount, int points,
			int correctAnswers, int totalQuestions) {
	}

	public record AuthorStats(int created, int validated, int inReview, int rejected) {
	}
}
