package goodshi.ageofquizz.dto;

import java.time.LocalDateTime;
import java.util.List;

import goodshi.ageofquizz.entity.Question;

public record QuestionDTO(Integer id, String libelle, Question.QuestionTheme theme, Question.QuestionType type,
		Question.QuestionStatus status, Question.QuestionCivilisation civilisation, String fileUrl,
		String authorUsername, List<AnswerDTO> answers, Question.QuestionBuilding building, LocalDateTime createdAt,
		LocalDateTime modifiedAt) {
	public static QuestionDTO fromEntity(Question q) {
		return new QuestionDTO(q.getId(), q.getLibelle(), q.getTheme(), q.getType(), q.getStatus(), q.getCivilisation(),
				q.getFileUrl(), q.getAuthor().getUsername(),
				q.getAnswers().stream().map(AnswerDTO::fromEntity).toList(), q.getBuilding(), q.getCreatedAt(),
				q.getModifiedAt());
	}
}
