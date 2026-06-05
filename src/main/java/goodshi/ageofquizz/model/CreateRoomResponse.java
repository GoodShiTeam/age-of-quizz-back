package goodshi.ageofquizz.model;

import java.util.List;

/**
 * Response DTO returned when a room is created. Keeps a typed structure instead
 * of using a raw map in controller.
 */
public class CreateRoomResponse {

    private String code;
    private String host;
    private List<String> players;
    private String participantId; // nullable for authenticated hosts

    public CreateRoomResponse() {}

    public CreateRoomResponse(String code, String host, List<String> players, String participantId) {
        this.code = code;
        this.host = host;
        this.players = players;
        this.participantId = participantId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public List<String> getPlayers() {
        return players;
    }

    public void setPlayers(List<String> players) {
        this.players = players;
    }

    public String getParticipantId() {
        return participantId;
    }

    public void setParticipantId(String participantId) {
        this.participantId = participantId;
    }
}
