package goodshi.ageofquizz.dto;

import java.util.List;

/**
 * Statistiques d'un joueur connecté. {@code distribution.get(i)} = nombre de
 * victoires en i + 1 tentatives.
 */
public record DailyRiddleStatsDTO(int played, int won, int currentStreak, int maxStreak,
		List<Integer> distribution) {
}
