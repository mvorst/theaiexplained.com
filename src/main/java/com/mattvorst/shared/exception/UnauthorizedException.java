package com.mattvorst.shared.exception;

/*
 * Copyright © 2025 Matthew A Vorst.
 * All Rights Reserved.
 * Permission to use, copy, modify, and distribute this software is strictly prohibited without the explicit written consent of Matthew A Vorst.
 *
 */

public class UnauthorizedException extends RuntimeException
{
	private static final long serialVersionUID = 201410061327L;
	private String errorCode;

	public UnauthorizedException() {
		super();
	}

	public UnauthorizedException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnauthorizedException(String message) {
		super(message);
	}

	public UnauthorizedException(String errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}

	public UnauthorizedException(Throwable cause) {
		super(cause);
	}

	public String getErrorCode() {
		return errorCode;
	}
}
