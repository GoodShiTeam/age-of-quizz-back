package goodshi.ageofquizz.model;

public class PlayerResult {

    private Long userId;
    private String username;
    private int rank;
    private int score;
    private int points;
    private int correctAnswers;
    private int totalQuestions;
    private double accuracy;
    private long totalTimeSeconds;
    private double averageTimePerQuestion;
    private String avatar;

    public PlayerResult() {}

    // getters/setters

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(int correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    public long getTotalTimeSeconds() {
        return totalTimeSeconds;
    }

    public void setTotalTimeSeconds(long totalTimeSeconds) {
        this.totalTimeSeconds = totalTimeSeconds;
    }

    public double getAverageTimePerQuestion() {
        return averageTimePerQuestion;
    }

    public void setAverageTimePerQuestion(double averageTimePerQuestion) {
        this.averageTimePerQuestion = averageTimePerQuestion;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

}
