package goodshi.ageofquizz.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Contenu de daily-riddle/riddle-data.json, généré par
 * scripts/generate-daily-riddle-data.mjs. Les indices de chaque entrée sont
 * déjà ordonnés du plus vague au plus précis.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record DailyRiddleData(List<Entry> civilizations, List<Entry> units) {

	public record Entry(String name, List<Hint> hints) {
	}

	public record Hint(String label, String text) {
	}

	public enum TargetType {
		CIVILIZATION, UNIT
	}
}
