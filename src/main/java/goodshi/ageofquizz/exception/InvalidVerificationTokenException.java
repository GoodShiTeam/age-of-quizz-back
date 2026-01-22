package goodshi.ageofquizz.exception;

public class InvalidVerificationTokenException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InvalidVerificationTokenException(String message) {
		super(message);
	}
}