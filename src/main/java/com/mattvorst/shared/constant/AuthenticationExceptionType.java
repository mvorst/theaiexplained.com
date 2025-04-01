package com.mattvorst.shared.constant;

public enum AuthenticationExceptionType {
	EXPIRED_PASSWORD("error.password.expired"), /* The user's password had expired */
	UNVERIFIED_SOURCE("error.source.unverified"), /* The source (email address) has not been verified for this user or verification has expired */
	MISSING_SOURCE("error.source.notfound"), /* Unable to find a verified source (email address) for this user */
	SESSION_TIMEOUT("error.session.timeout"),
	EXPIRED_SESSION("error.session.expired");

	private String errorCode;

	AuthenticationExceptionType(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorCode() {
		return errorCode;
	}

}

