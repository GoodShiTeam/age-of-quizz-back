package goodshi.ageofquizz.service;

import goodshi.ageofquizz.entity.User;

/** Name shown to other players in multiplayer. */
public final class PlayerNames {

	private PlayerNames() {
	}

	/**
	 * The profile pseudo for logged-in players; guests have no profile, their
	 * username already holds the name they typed.
	 */
	public static String displayName(User user) {
		if (user == null) {
			return null;
		}
		if (user.getUserProfile() != null && user.getUserProfile().getPseudo() != null
				&& !user.getUserProfile().getPseudo().isBlank()) {
			return user.getUserProfile().getPseudo();
		}
		return user.getUsername();
	}
}
