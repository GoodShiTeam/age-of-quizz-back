package goodshi.ageofquizz.model;

/**
 * Request payload when creating a room. Extracted from controller nested class.
 */
public class CreateRoomRequest {

    private String displayName;

    public CreateRoomRequest() {}

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
