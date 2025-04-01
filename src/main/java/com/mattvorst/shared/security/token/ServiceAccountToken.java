package com.mattvorst.shared.security.token;

import java.util.UUID;

import com.mattvorst.shared.security.JwtAuthenticationProvider;
import com.mattvorst.shared.security.constant.SubjectType;
import com.mattvorst.shared.util.Utils;
import org.springframework.security.oauth2.jwt.Jwt;

public class ServiceAccountToken extends AuthToken {
	private final UUID serviceUuid;
	private final String name;
	private final String tokenValue;
	private final String subject;

	public ServiceAccountToken(Jwt jwt, boolean authenticated) {
		super(jwt);

		this.name = (String) jwt.getClaims().get(JwtAuthenticationProvider.NAME);

		this.tokenValue = jwt.getTokenValue();
		this.subject = jwt.getSubject();

		this.serviceUuid = Utils.safeToUuid(jwt.getSubject());
		this.setAuthenticated(authenticated);
	}

	@Override
	public String getName() {
		return name;
	}


	@Override
	public SubjectType getSubjectType() {
		return SubjectType.SERVICE_ACCOUNT;
	}

	@Override
	public UUID getSubjectUuid() {
		return serviceUuid;
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
