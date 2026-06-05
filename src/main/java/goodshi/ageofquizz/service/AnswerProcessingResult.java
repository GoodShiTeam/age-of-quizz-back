package goodshi.ageofquizz.service;

import java.util.Map;

public class AnswerProcessingResult {

    private final boolean accepted;
    private final boolean correct;
    private final Map<String, Integer> scores;
    private final Long userId;

    public AnswerProcessingResult(boolean accepted, boolean correct, Map<String, Integer> scores, Long userId) {
        this.accepted = accepted;
        this.correct = correct;
        this.scores = scores;
        this.userId = userId;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public boolean isCorrect() {
        return correct;
    }

    public Map<String, Integer> getScores() {
        return scores;
    }

    public Long getUserId() {
        return userId;
    }
}
