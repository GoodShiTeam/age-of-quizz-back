package goodshi.ageofquizz.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "game_recap_snapshot")
public class GameRecapSnapshotEntity {

    @Id
    @Column(name = "game_session_id", length = 36)
    private String gameSessionId;

    @Column(name = "recap_json", columnDefinition = "JSON")
    private String recapJson;

    public String getGameSessionId() {
        return gameSessionId;
    }

    public void setGameSessionId(String gameSessionId) {
        this.gameSessionId = gameSessionId;
    }

    public String getRecapJson() {
        return recapJson;
    }

    public void setRecapJson(String recapJson) {
        this.recapJson = recapJson;
    }
}
