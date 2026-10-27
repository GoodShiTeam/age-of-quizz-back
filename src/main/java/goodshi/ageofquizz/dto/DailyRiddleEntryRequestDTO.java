package goodshi.ageofquizz.dto;

import java.util.List;

import goodshi.ageofquizz.model.DailyRiddleData.Hint;
import goodshi.ageofquizz.model.DailyRiddleData.TargetType;

/** Création ou modification manuelle d'une énigme. */
public record DailyRiddleEntryRequestDTO(String name, TargetType type, boolean active, List<Hint> hints) {
}
