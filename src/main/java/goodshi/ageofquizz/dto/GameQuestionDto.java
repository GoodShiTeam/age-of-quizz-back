package goodshi.ageofquizz.dto;

import java.util.List;

import goodshi.ageofquizz.entity.Question;

/**
 * Question DTO for game play: answers do NOT include the correct flag.
 * Explication is also withheld until after the question is answered.
 */
public record GameQuestionDto(
        Integer id,
        String libelle,
        Question.QuestionTheme theme,
        Question.QuestionType type,
        Question.QuestionCivilisation civilisation,
        Question.QuestionBuilding building,
        String fileUrl,
        List<GameAnswerDto> answers) {

    public static GameQuestionDto fromEntity(Question q) {
        return new GameQuestionDto(
                q.getId(),
                q.getLibelle(),
                q.getTheme(),
                q.getType(),
                q.getCivilisation(),
                q.getBuilding(),
                q.getFileUrl(),
                q.getAnswers().stream().map(GameAnswerDto::fromEntity).toList());
    }
}
