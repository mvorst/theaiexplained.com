package com.mattvorst.shared.exception;

/*
 * Copyright © 2025 Matthew A Vorst.
 * All Rights Reserved.
 * Permission to use, copy, modify, and distribute this software is strictly prohibited without the explicit written consent of Matthew A Vorst.
 *
 */

public class InvalidRegistrationException extends Exception {

	private static final long serialVersionUID = 201811260233L;

	public InvalidRegistrationException() {
		super();
	}

	public InvalidRegistrationException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public InvalidRegistrationException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidRegistrationException(String message) {
		super(message);
	}

	public InvalidRegistrationException(Throwable cause) {
		super(cause);
	}
}
