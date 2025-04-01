package com.mattvorst.shared.exception;

/*
 * Copyright © 2025 Matthew A Vorst.
 * All Rights Reserved.
 * Permission to use, copy, modify, and distribute this software is strictly prohibited without the explicit written consent of Matthew A Vorst.
 *
 */
public class NoContentException extends RuntimeException {
	private static final long serialVersionUID = 201411201220L;

	public NoContentException()
	{
		super();
	}

	public NoContentException(String message)
	{
		super(message);
	}

	public NoContentException(Throwable cause)
	{
		super(cause);
	}

	public NoContentException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public NoContentException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
