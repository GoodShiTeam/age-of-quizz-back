package goodshi.ageofquizz.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "game_session")
public class GameSessionEntity {

    @Id
    @Column(name = "game_session_id", length = 36)
    private String gameSessionId;

    @Column(name = "room_code")
    private String roomCode;

    @Column(name = "game_mode")
    private String gameMode;

    @Column(name = "status")
    private String status;

    @Column(name = "start_time")
    private Instant startTime;

    @Column(name = "end_time")
    private Instant endTime;

    @Column(name = "total_duration_s")
    private Integer totalDurationS;

    @Column(name = "question_count")
    private Integer questionCount;

    @Column(name = "quiz_theme")
    private String quizTheme;

    @Column(name = "quiz_difficulty")
    private String quizDifficulty;

    @Column(name = "statistics", columnDefinition = "JSON")
    private String statistics;

    public String getGameSessionId() {
        return gameSessionId;
    }

    public void setGameSessionId(String gameSessionId) {
        this.gameSessionId = gameSessionId;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
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

    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
    }

    public Integer getTotalDurationS() {
        return totalDurationS;
    }

    public void setTotalDurationS(Integer totalDurationS) {
        this.totalDurationS = totalDurationS;
    }

    public Integer getQuestionCount() {
        return questionCount;
    }

    public void setQuestionCount(Integer questionCount) {
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

    public String getStatistics() {
        return statistics;
    }

    public void setStatistics(String statistics) {
        this.statistics = statistics;
    }
}
