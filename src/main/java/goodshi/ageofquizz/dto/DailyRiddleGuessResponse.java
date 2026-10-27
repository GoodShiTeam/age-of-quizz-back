package goodshi.ageofquizz.dto;

import java.util.List;

import goodshi.ageofquizz.model.DailyRiddleData.Hint;

/**
 * Résultat d'une tentative. {@code known} vaut false si la proposition ne fait
 * pas partie des candidats ou a déjà été faite (la tentative n'est alors pas
 * comptée). {@code answer} n'est renseigné qu'une fois la partie terminée.
 * {@code guesses} contient les propositions enregistrées pour un utilisateur
 * connecté, null pour un anonyme.
 */
public record DailyRiddleGuessResponse(boolean known, boolean correct, String answer, List<Hint> hints,
		List<String> guesses) {
}
