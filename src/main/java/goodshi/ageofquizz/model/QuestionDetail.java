package goodshi.ageofquizz.model;

import java.util.Map;

public class QuestionDetail {

    private Integer questionId;
    private String questionText;
    private Map<String, Object> playerResponses; // key: username or userId as string, value: response detail

    public QuestionDetail() {}

    public Integer getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Integer questionId) {
        this.questionId = questionId;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public Map<String, Object> getPlayerResponses() {
        return playerResponses;
    }

    public void setPlayerResponses(Map<String, Object> playerResponses) {
        this.playerResponses = playerResponses;
    }
}
