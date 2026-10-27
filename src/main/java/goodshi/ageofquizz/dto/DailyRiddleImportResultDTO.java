package goodshi.ageofquizz.dto;

import java.util.List;

/**
 * Bilan d'un import : {@code skipped} compte les entrées modifiées à la main
 * laissées intactes, {@code warnings} les entrées ignorées ou incomplètes.
 */
public record DailyRiddleImportResultDTO(int civilizations, int units, int created, int updated, int skipped,
		List<String> warnings) {
}
