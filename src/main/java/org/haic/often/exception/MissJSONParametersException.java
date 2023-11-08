package org.haic.often.exception;

public class MissJSONParametersException extends RuntimeException {

	public MissJSONParametersException() {
		super();
	}

	public MissJSONParametersException(String message) {
		super(message);
	}

	public MissJSONParametersException(String message, Throwable cause) {
		super(message, cause);
	}

	public MissJSONParametersException(Throwable cause) {
		super(cause);
	}

}
