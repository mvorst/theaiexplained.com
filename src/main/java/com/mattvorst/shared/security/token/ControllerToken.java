package com.mattvorst.shared.security.token;

import java.util.UUID;

import com.mattvorst.shared.security.JwtAuthenticationProvider;
import com.mattvorst.shared.security.constant.SubjectType;
import com.mattvorst.shared.util.Utils;
import org.springframework.security.oauth2.jwt.Jwt;

public class ControllerToken extends AuthToken {
	private UUID controllerUuid;
	private String name;
	private final String tokenValue;
	private final String subject;

	public ControllerToken(Jwt jwt, boolean authenticated) {
		super(jwt);

		this.name = (String) jwt.getClaims().get(JwtAuthenticationProvider.NAME);

		this.tokenValue = jwt.getTokenValue();
		this.subject = jwt.getSubject();

		this.controllerUuid = Utils.safeToUuid(jwt.getSubject());
		this.setAuthenticated(authenticated);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public SubjectType getSubjectType() {
		return SubjectType.CONTROLLER;
	}

	@Override
	public UUID getSubjectUuid() {
		return controllerUuid;
	}

	@Override
	public Object getCredentials() {
		return this.tokenValue;
	}

	@Override
	public Object getPrincipal() {
		return this.subject;
	}
}
