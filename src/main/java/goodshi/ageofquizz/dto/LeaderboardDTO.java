package goodshi.ageofquizz.dto;

import java.time.Instant;
import java.util.List;

/**
 * Classements généraux. Seuls les joueurs avec au moins
 * {@code minQuestionsForAccuracy} questions répondues figurent au classement de
 * précision.
 */
public record LeaderboardDTO(List<Entry> accuracy, List<Entry> questionsAnswered, List<Entry> multiplayerWins,
		List<Entry> dailyRiddleStreak, int minQuestionsForAccuracy, Instant computedAt) {

	/**
	 * {@code value} = valeur classée (précision en %, nombre...) ;
	 * {@code sampleSize} = base du calcul (questions répondues, parties jouées,
	 * énigmes jouées).
	 */
	public record Entry(int rank, Long userProfileId, String pseudo, String avatar, double value, int sampleSize) {
	}
}
