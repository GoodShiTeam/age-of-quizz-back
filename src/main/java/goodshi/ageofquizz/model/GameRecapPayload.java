package goodshi.ageofquizz.model;

import java.util.List;
import java.util.Map;

public class GameRecapPayload {

    private GameSessionMetadata session;
    private Map<String, Object> results; // will contain playerResults list under "playerResults"
    private List<QuestionDetail> questions;
    private boolean allowRematch;
    private int rematchExpireIn;
    private Map<String, Object> statistics;

    public GameRecapPayload() {}

    public GameSessionMetadata getSession() {
        return session;
    }

    public void setSession(GameSessionMetadata session) {
        this.session = session;
    }

    public Map<String, Object> getResults() {
        return results;
    }

    public void setResults(Map<String, Object> results) {
        this.results = results;
    }

    public List<QuestionDetail> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionDetail> questions) {
        this.questions = questions;
    }

    public boolean isAllowRematch() {
        return allowRematch;
    }

    public void setAllowRematch(boolean allowRematch) {
        this.allowRematch = allowRematch;
    }

    public int getRematchExpireIn() {
        return rematchExpireIn;
    }

    public void setRematchExpireIn(int rematchExpireIn) {
        this.rematchExpireIn = rematchExpireIn;
    }

    public Map<String, Object> getStatistics() {
        return statistics;
    }

    public void setStatistics(Map<String, Object> statistics) {
        this.statistics = statistics;
    }
}
