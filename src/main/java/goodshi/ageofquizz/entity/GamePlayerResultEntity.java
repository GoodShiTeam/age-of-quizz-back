package goodshi.ageofquizz.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "game_player_result")
public class GamePlayerResultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "game_session_id", length = 36)
    private String gameSessionId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "username")
    private String username;

    @Column(name = "`rank`")
    private Integer rank;

    @Column(name = "score")
    private Integer score;

    @Column(name = "points")
    private Integer points;

    @Column(name = "correct_answers")
    private Integer correctAnswers;

    @Column(name = "total_questions")
    private Integer totalQuestions;

    @Column(name = "accuracy")
    private Double accuracy;

    @Column(name = "total_time_seconds")
    private Long totalTimeSeconds;

    @Column(name = "average_time_per_question")
    private Double averageTimePerQuestion;

    @Column(name = "avatar")
    private String avatar;

    @Column(name = "additional", columnDefinition = "JSON")
    private String additional;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getGameSessionId() {
        return gameSessionId;
    }

    public void setGameSessionId(String gameSessionId) {
        this.gameSessionId = gameSessionId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public Integer getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(Integer correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public Integer getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(Integer totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public Double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(Double accuracy) {
        this.accuracy = accuracy;
    }

    public Long getTotalTimeSeconds() {
        return totalTimeSeconds;
    }

    public void setTotalTimeSeconds(Long totalTimeSeconds) {
        this.totalTimeSeconds = totalTimeSeconds;
    }

    public Double getAverageTimePerQuestion() {
        return averageTimePerQuestion;
    }

    public void setAverageTimePerQuestion(Double averageTimePerQuestion) {
        this.averageTimePerQuestion = averageTimePerQuestion;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getAdditional() {
        return additional;
    }

    public void setAdditional(String additional) {
        this.additional = additional;
    }
}
