package goodshi.ageofquizz.exception;

public class UserWithSamePseudoExistException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5043074849074631303L;

	public UserWithSamePseudoExistException(String pseudo) {
		super("Le pseudo : " + pseudo + " est déjà pris");
	}

}
