package goodshi.ageofquizz.model;

import java.util.Map;

public class QuestionDetail {

    private Integer questionId;
    private String questionText;
    private Map<String, Object> playerResponses; // key: username or userId as string, value: response detail
    // shown in the end-of-game recap
    private String explanation;
    private String fileUrl;
    private String questionType;

    public QuestionDetail() {}

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

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
