package goodshi.ageofquizz.exception;

public class EmailAlreadyVerifiedException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public EmailAlreadyVerifiedException(String message) {
		super(message);
	}
}