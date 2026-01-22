package goodshi.ageofquizz.exception;

public class UserWithSameEmailExistException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UserWithSameEmailExistException(String email) {
		super("L'email  : " + email + " est déjà pris");
	}

}
