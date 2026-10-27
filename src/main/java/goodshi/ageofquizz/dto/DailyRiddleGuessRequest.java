package goodshi.ageofquizz.dto;

import java.time.LocalDate;

/**
 * Tentative d'un joueur. {@code attempt} est le numéro de la tentative
 * (1-based) : la progression est conservée côté client.
 */
public record DailyRiddleGuessRequest(LocalDate date, String guess, int attempt) {
}
