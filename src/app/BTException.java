package app;

public class BTException extends Exception
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public BTException() {
        super();
    }

    public BTException(String message) {
        super(message);
    }

    public BTException(String message, Throwable cause) {
        super(message, cause);
    }

    public BTException(Throwable cause) {
        super(cause);
    }
}
