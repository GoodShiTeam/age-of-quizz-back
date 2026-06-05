package goodshi.ageofquizz.model;

/**
 * Payload used to request starting a game; may contain participantId for anonymous host.
 */
public class StartGameRequest {

    private String participantId;

    public StartGameRequest() {}

    public String getParticipantId() {
        return participantId;
    }

    public void setParticipantId(String participantId) {
        this.participantId = participantId;
    }
}
