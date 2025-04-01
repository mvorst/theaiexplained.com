package com.mattvorst.shared.exception;

import java.util.ArrayList;
import java.util.List;

import org.springframework.validation.FieldError;

/*
 * Copyright © 2025 Matthew A Vorst.
 * All Rights Reserved.
 * Permission to use, copy, modify, and distribute this software is strictly prohibited without the explicit written consent of Matthew A Vorst.
 *
 */


public class ValidationException extends Exception
{
	private static final long serialVersionUID = 201411240947L;

	private List<FieldError> fieldErrorList;

	private Object[] args;

	public ValidationException()
	{
		super();
	}

	public ValidationException(String message)
	{
		super(message);
	}

	public ValidationException(String message, Object[] args)
	{
		super(message);
		this.args = args;
	}

	public ValidationException(Throwable cause)
	{
		super(cause);
	}

	public ValidationException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public ValidationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ValidationException(String message, List<FieldError> fieldErrorList)
	{
		super(message);

		this.fieldErrorList = fieldErrorList;
	}

	public ValidationException(String message, FieldError fieldError)
	{
		super(message);

		if(this.fieldErrorList == null)
		{
			this.fieldErrorList = new ArrayList<>();
		}

		this.fieldErrorList.add(fieldError);
	}

	public Object[] getArgs() {
		return args;
	}

	public void setArgs(Object[] args) {
		this.args = args;
	}

	public List<FieldError> getFieldErrorList() {
		return fieldErrorList;
	}

	public void setFieldErrorList(List<FieldError> fieldErrorList) {
		this.fieldErrorList = fieldErrorList;
	}
}
