package goodshi.ageofquizz.model;

import java.util.List;

/**
 * Payload used to submit an answer in multiplayer mode. Extracted from controller.
 */
public class AnswerSubmitRequest {

    private Integer questionId;
    private String participantId;
    private List<String> selected;

    public AnswerSubmitRequest() {}

    public Integer getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Integer questionId) {
        this.questionId = questionId;
    }

    public String getParticipantId() {
        return participantId;
    }

    public void setParticipantId(String participantId) {
        this.participantId = participantId;
    }

    public List<String> getSelected() {
        return selected;
    }

    public void setSelected(List<String> selected) {
        this.selected = selected;
    }
}
