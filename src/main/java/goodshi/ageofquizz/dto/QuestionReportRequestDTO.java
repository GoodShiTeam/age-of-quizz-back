package goodshi.ageofquizz.dto;

import goodshi.ageofquizz.entity.QuestionReport;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Corps de POST /questions/{id}/reports. Le commentaire est obligatoire pour OTHER. */
public record QuestionReportRequestDTO(@NotNull QuestionReport.Reason reason,
		@Size(max = QuestionReport.COMMENT_MAX_LENGTH) String comment) {
}
