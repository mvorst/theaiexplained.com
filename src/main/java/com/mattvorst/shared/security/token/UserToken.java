package com.mattvorst.shared.security.token;

import java.util.UUID;

import com.mattvorst.shared.security.JwtAuthenticationProvider;
import com.mattvorst.shared.security.constant.SubjectType;
import com.mattvorst.shared.util.Utils;
import org.springframework.security.oauth2.jwt.Jwt;

public class UserToken extends AuthToken {
	private final UUID userUuid;
	private final UUID defaultHomeUuid;
	private final String tokenValue;
	private final String name;
	private final String subject;
	private final String timeZone;

	public UserToken(Jwt jwt, boolean authenticated) {
		super(jwt);

		this.name = (String) jwt.getClaims().get(JwtAuthenticationProvider.NAME);


		this.tokenValue = jwt.getTokenValue();
		this.subject = jwt.getSubject();

		this.userUuid = Utils.safeToUuid(jwt.getSubject());

		this.timeZone = (String) jwt.getClaims().get(JwtAuthenticationProvider.TIME_ZONE);

		this.defaultHomeUuid = Utils.safeToUuid(jwt.getClaims().get(JwtAuthenticationProvider.HOME_UUID));

		this.setAuthenticated(authenticated);
	}

	@Override
	public SubjectType getSubjectType() {
		return SubjectType.USER;
	}

	@Override
	public UUID getSubjectUuid() {
		return this.userUuid;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Object getCredentials() {
		return this.tokenValue;
	}

	@Override
	public Object getPrincipal() {
		return this.subject;
	}

	@Override
	public String getTimeZone() {
		return timeZone;
	}

	public UUID getUserUuid() {
		return userUuid;
	}

	public UUID getDefaultHomeUuid() {
		return defaultHomeUuid;
	}

	public String getTokenValue() {
		return tokenValue;
	}
}
