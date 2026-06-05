package goodshi.ageofquizz.model;

import java.util.List;

public class PlayerAnswerRecord {

	private Integer questionId;
	private Long userId;
	private String participantId;
	private String username;
	private List<String> selected;
	private List<String> correct;
	private boolean isCorrect;
	private long timeSeconds;

	public PlayerAnswerRecord() {}

	public PlayerAnswerRecord(Integer questionId, Long userId, String participantId, String username, List<String> selected, List<String> correct, boolean isCorrect, long timeSeconds) {
		this.questionId = questionId;
		this.userId = userId;
		this.participantId = participantId;
		this.username = username;
		this.selected = selected;
		this.correct = correct;
		this.isCorrect = isCorrect;
		this.timeSeconds = timeSeconds;
	}

	public Integer getQuestionId() {
		return questionId;
	}

	public void setQuestionId(Integer questionId) {
		this.questionId = questionId;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getParticipantId() {
		return participantId;
	}

	public void setParticipantId(String participantId) {
		this.participantId = participantId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public List<String> getSelected() {
		return selected;
	}

	public void setSelected(List<String> selected) {
		this.selected = selected;
	}

	public List<String> getCorrect() {
		return correct;
	}

	public void setCorrect(List<String> correct) {
		this.correct = correct;
	}

	public boolean isCorrect() {
		return isCorrect;
	}

	public void setCorrect(boolean correct) {
		isCorrect = correct;
	}

	public long getTimeSeconds() {
		return timeSeconds;
	}

	public void setTimeSeconds(long timeSeconds) {
		this.timeSeconds = timeSeconds;
	}

}

