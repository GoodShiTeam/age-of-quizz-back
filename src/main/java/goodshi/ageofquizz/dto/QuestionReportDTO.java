package goodshi.ageofquizz.dto;

import java.time.Instant;

import goodshi.ageofquizz.entity.QuestionReport;
import goodshi.ageofquizz.entity.User;

public record QuestionReportDTO(Long id, Integer questionId, QuestionReport.Reason reason, String comment,
		String reporterName, Instant createdAt) {

	public static QuestionReportDTO fromEntity(QuestionReport r) {
		return new QuestionReportDTO(r.getId(), r.getQuestion().getId(), r.getReason(), r.getComment(),
				displayName(r.getReporter()), r.getCreatedAt());
	}

	private static String displayName(User user) {
		if (user.getUserProfile() != null && user.getUserProfile().getPseudo() != null) {
			return user.getUserProfile().getPseudo();
		}
		return user.getUsername();
	}
}
