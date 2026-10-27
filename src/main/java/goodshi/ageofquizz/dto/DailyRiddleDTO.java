package goodshi.ageofquizz.dto;

import java.time.LocalDate;
import java.util.List;

import goodshi.ageofquizz.model.DailyRiddleData.Hint;
import goodshi.ageofquizz.model.DailyRiddleData.TargetType;

/**
 * Énigme du jour telle que vue par le joueur : uniquement les indices
 * débloqués, jamais la réponse (sauf partie terminée). {@code progress} est
 * null pour un anonyme ou un utilisateur qui n'a pas encore joué.
 */
public record DailyRiddleDTO(LocalDate date, int number, TargetType targetType, int maxAttempts, int totalHints,
		List<Hint> hints, List<String> candidates, DailyRiddleProgressDTO progress) {
}
