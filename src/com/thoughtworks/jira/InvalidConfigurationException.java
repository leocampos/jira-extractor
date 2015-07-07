package com.thoughtworks.jira;

public class InvalidConfigurationException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public InvalidConfigurationException() {
		super();
	}

	public InvalidConfigurationException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public InvalidConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidConfigurationException(String message) {
		super(message);
	}

	public InvalidConfigurationException(Throwable cause) {
		super(cause);
	}
}
