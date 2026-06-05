package goodshi.ageofquizz.model;

public class GameSessionMetadata {

    private String gameSessionId;
    private String gameMode;
    private String status;
    private String startTime; // ISO
    private String endTime;
    private long totalDuration;
    private int questionCount;
    private String quizTheme;
    private String quizDifficulty;

    public GameSessionMetadata() {}

    // getters/setters

    public String getGameSessionId() {
        return gameSessionId;
    }

    public void setGameSessionId(String gameSessionId) {
        this.gameSessionId = gameSessionId;
    }

    public String getGameMode() {
        return gameMode;
    }

    public void setGameMode(String gameMode) {
        this.gameMode = gameMode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public long getTotalDuration() {
        return totalDuration;
    }

    public void setTotalDuration(long totalDuration) {
        this.totalDuration = totalDuration;
    }

    public int getQuestionCount() {
        return questionCount;
    }

    public void setQuestionCount(int questionCount) {
        this.questionCount = questionCount;
    }

    public String getQuizTheme() {
        return quizTheme;
    }

    public void setQuizTheme(String quizTheme) {
        this.quizTheme = quizTheme;
    }

    public String getQuizDifficulty() {
        return quizDifficulty;
    }

    public void setQuizDifficulty(String quizDifficulty) {
        this.quizDifficulty = quizDifficulty;
    }
}
