package goodshi.ageofquizz.exception;

public class ResourceNotFoundException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 7194756795281568133L;

	public ResourceNotFoundException(String message) {
        super(message);
    }
}
