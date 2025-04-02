package com.mattvorst.shared.model.error;

import java.util.List;

public class ViewError {
	private String errorCode;
	private String errorTitle;
	private List<ViewFieldError> fieldErrorList;

	public ViewError(String errorCode, String errorTitle) {
		this.errorCode = errorCode;
		this.errorTitle = errorTitle;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorTitle() {
		return errorTitle;
	}

	public void setErrorTitle(String errorTitle) {
		this.errorTitle = errorTitle;
	}

	public List<ViewFieldError> getFieldErrorList() {
		return fieldErrorList;
	}

	public void setFieldErrorList(List<ViewFieldError> fieldErrorList) {
		this.fieldErrorList = fieldErrorList;
	}
}
