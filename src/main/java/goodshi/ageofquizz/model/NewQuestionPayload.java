package goodshi.ageofquizz.model;

import goodshi.ageofquizz.dto.GameQuestionDto;
import goodshi.ageofquizz.entity.Question;

public class NewQuestionPayload {

    private GameQuestionDto question;
    private long questionStartTime;
    private long questionEndTime;

    public NewQuestionPayload() {
    }

    public NewQuestionPayload(Question question, long questionStartTime, long questionEndTime) {
        this.question = GameQuestionDto.fromEntity(question);
        this.questionStartTime = questionStartTime;
        this.questionEndTime = questionEndTime;
    }

    public GameQuestionDto getQuestion() {
        return question;
    }

    public void setQuestion(GameQuestionDto question) {
        this.question = question;
    }

    public long getQuestionStartTime() {
        return questionStartTime;
    }

    public void setQuestionStartTime(long questionStartTime) {
        this.questionStartTime = questionStartTime;
    }

    public long getQuestionEndTime() {
        return questionEndTime;
    }

    public void setQuestionEndTime(long questionEndTime) {
        this.questionEndTime = questionEndTime;
    }

}
