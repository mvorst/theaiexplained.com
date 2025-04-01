package com.mattvorst.shared.exception;

import com.mattvorst.shared.constant.AuthorizationExceptionType;
import com.mattvorst.shared.security.constant.Permission;
import org.springframework.security.access.AccessDeniedException;

public class AuthorizationException extends AccessDeniedException
{
	private static final long serialVersionUID = 202502091840L;
	private AuthorizationExceptionType authorizationExceptionType;
	private Permission permission;

	public AuthorizationException(String message) {
		super(message);
	}

	public AuthorizationException(AuthorizationExceptionType authorizationExceptionType) {
		super(authorizationExceptionType.getErrorCode());
		this.authorizationExceptionType = authorizationExceptionType;
	}

	public AuthorizationException(Permission permission)
	{
		super("Permission not found: " + permission);

		this.permission = permission;
	}

	public Permission getPermission() {
		return permission;
	}

	public void setPermission(Permission permission) {
		this.permission = permission;
	}

	public AuthorizationExceptionType getAuthorizationExceptionType() {
		return authorizationExceptionType;
	}

	public void setAuthorizationExceptionType(AuthorizationExceptionType authorizationExceptionType) {
		this.authorizationExceptionType = authorizationExceptionType;
	}
}
