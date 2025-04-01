package com.mattvorst.shared.security.model;

import java.time.Instant;
import java.time.OffsetDateTime;

import com.mattvorst.shared.security.AuthorizationUtils;
import com.mattvorst.shared.security.model.user.ViewAuthToken;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.jwt.Jwt;

public class EncryptedJWT implements OAuth2Token {
	private Jwt jwt;
	public EncryptedJWT(Jwt jwt) {
		this.jwt = jwt;
	}

	@Override
	public String getTokenValue() {
		return AuthorizationUtils.encryptToken(jwt.getTokenValue());
	}

	@Override
	public Instant getIssuedAt() {
		return jwt.getIssuedAt();
	}

	@Override
	public Instant getExpiresAt() {
		return jwt.getExpiresAt();
	}

	public ViewAuthToken toView() {
		ViewAuthToken viewAuthToken = new ViewAuthToken();
		viewAuthToken.setTokenValue(this.getTokenValue());
		viewAuthToken.setTokenType("Bearer");
		if (this.getExpiresAt() != null) {
			viewAuthToken.setExpiresIn(getExpiresAt().getEpochSecond() - OffsetDateTime.now().toEpochSecond());
		}

		return viewAuthToken;
	}
}
