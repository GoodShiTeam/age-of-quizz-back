package goodshi.ageofquizz.dto;

import goodshi.ageofquizz.entity.Answer;

/**
 * Answer DTO for game play: does NOT expose the correct flag.
 * The backend is the sole authority for answer verification.
 */
public record GameAnswerDto(Integer id, String value) {
    public static GameAnswerDto fromEntity(Answer answer) {
        return new GameAnswerDto(answer.getId(), answer.getValue());
    }
}
