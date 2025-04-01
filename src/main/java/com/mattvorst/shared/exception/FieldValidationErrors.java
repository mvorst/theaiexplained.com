package com.mattvorst.shared.exception;

/*
 * Copyright © 2025 Matthew A Vorst.
 * All Rights Reserved.
 * Permission to use, copy, modify, and distribute this software is strictly prohibited without the explicit written consent of Matthew A Vorst.
 *
 */

import java.util.List;

import org.springframework.validation.FieldError;

public class FieldValidationErrors
{
	private String message;
	private List<FieldError> fieldErrorList;

	public FieldValidationErrors()
	{
		super();
	}

	public FieldValidationErrors(String message, List<FieldError> fieldErrorList)
	{
		super();

		this.message = message;
		this.fieldErrorList = fieldErrorList;
	}

	public List<FieldError> getFieldErrorList() {
		return fieldErrorList;
	}

	public void setFieldErrorList(List<FieldError> fieldErrorList) {
		this.fieldErrorList = fieldErrorList;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
