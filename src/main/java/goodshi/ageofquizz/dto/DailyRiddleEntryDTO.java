package goodshi.ageofquizz.dto;

import java.time.LocalDate;
import java.util.List;

import goodshi.ageofquizz.model.DailyRiddleData.Hint;
import goodshi.ageofquizz.model.DailyRiddleData.TargetType;

/** Vue administrateur d'une énigme, réponse et indices compris. */
public record DailyRiddleEntryDTO(Integer id, String name, TargetType type, boolean active, boolean manuallyEdited,
		LocalDate lastPlayedOn, List<Hint> hints) {
}
