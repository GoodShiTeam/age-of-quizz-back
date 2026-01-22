package goodshi.ageofquizz.exception;

public class ExpiredVerificationTokenException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ExpiredVerificationTokenException(String message) {
		super(message);
	}
}