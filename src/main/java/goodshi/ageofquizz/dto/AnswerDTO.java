package goodshi.ageofquizz.dto;

import goodshi.ageofquizz.entity.Answer;

public record AnswerDTO(Integer id, String value, boolean correct) {
	public static AnswerDTO fromEntity(Answer answer) {
		return new AnswerDTO(answer.getId(), answer.getValue(), answer.isCorrect());
	}
}