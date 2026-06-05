package goodshi.ageofquizz.controller;

import java.util.List;

public class AnswerPayload {

    private Long userId;
    /** Single answer id – kept for backward compatibility (mono-réponse). */
    private Integer answerId;
    /** Multiple answer ids – à utiliser pour les questions à réponses multiples. */
    private List<Integer> answerIds;
    private String answerValue;
    private String participantId;
    private Integer questionId;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getAnswerId() {
        return answerId;
    }

    public void setAnswerId(Integer answerId) {
        this.answerId = answerId;
    }

    public List<Integer> getAnswerIds() {
        return answerIds;
    }

    public void setAnswerIds(List<Integer> answerIds) {
        this.answerIds = answerIds;
    }

    public String getAnswerValue() {
        return answerValue;
    }

    public void setAnswerValue(String answerValue) {
        this.answerValue = answerValue;
    }

    public String getParticipantId() {
        return participantId;
    }

    public void setParticipantId(String participantId) {
        this.participantId = participantId;
    }

    public Integer getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Integer questionId) {
        this.questionId = questionId;
    }

}
