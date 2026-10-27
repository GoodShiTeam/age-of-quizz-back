package goodshi.ageofquizz.dto;

import java.util.List;

import goodshi.ageofquizz.entity.DailyRiddleResult.Status;

/** Progression enregistrée d'un utilisateur connecté ; {@code answer} n'est renseigné qu'en fin de partie. */
public record DailyRiddleProgressDTO(List<String> guesses, Status status, String answer) {
}
