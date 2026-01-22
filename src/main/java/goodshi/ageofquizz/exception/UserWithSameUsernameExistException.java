package goodshi.ageofquizz.exception;

public class UserWithSameUsernameExistException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5043074849074631303L;

	public UserWithSameUsernameExistException(String username) {
		super("Le username : " + username + " est déjà pris");
	}

}
