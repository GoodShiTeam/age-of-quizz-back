package goodshi.ageofquizz.model;

/**
 * Payload used to set a participant ready/unready in multiplayer mode.
 */
public class ReadyRequest {

    private String participantId;
    private boolean ready = true;

    public ReadyRequest() {}

    public String getParticipantId() {
        return participantId;
    }

    public void setParticipantId(String participantId) {
        this.participantId = participantId;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }
}
