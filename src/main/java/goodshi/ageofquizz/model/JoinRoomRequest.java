package goodshi.ageofquizz.model;

/**
 * Request payload for joining a room. Accepts code and optional participantId/displayName for anonymous users.
 */
public class JoinRoomRequest {

    private String code;
    private String participantId;
    private String displayName;

    public JoinRoomRequest() {}

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getParticipantId() {
        return participantId;
    }

    public void setParticipantId(String participantId) {
        this.participantId = participantId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
