package mb.exceptions;

public class ConnectionError extends Exception {

	private static final long serialVersionUID = 2782475695475451690L;

	public ConnectionError(String message) {
		super(message);
	}

	public ConnectionError(String message, Throwable cause) {
		super(message, cause);
	}

}
