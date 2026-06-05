package goodshi.ageofquizz.model;

/**
 * Simple wrapper used when creating a room to optionally return a generated
 * participantId for anonymous hosts.
 */
public class CreateRoomResult {

	private Room room;
	private String participantId; // nullable - present when host was anonymous

	public CreateRoomResult(Room room, String participantId) {
		this.room = room;
		this.participantId = participantId;
	}

	public Room getRoom() {
		return room;
	}

	public void setRoom(Room room) {
		this.room = room;
	}

	public String getParticipantId() {
		return participantId;
	}

	public void setParticipantId(String participantId) {
		this.participantId = participantId;
	}

}
