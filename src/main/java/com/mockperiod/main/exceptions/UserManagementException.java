package com.mockperiod.main.exceptions;

public class UserManagementException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UserManagementException(String message) {
		super(message);
	}

	public UserManagementException(String message, Throwable cause) {
		super(message, cause);
	}
}
