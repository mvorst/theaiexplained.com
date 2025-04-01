package com.mattvorst.shared.constant;

public enum AuthorizationExceptionType {
	SITE("error.not.authorized.site"); /* Not authorized to change resources for site */

	private String errorCode;
	AuthorizationExceptionType(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorCode() {
		return errorCode;
	}
}
