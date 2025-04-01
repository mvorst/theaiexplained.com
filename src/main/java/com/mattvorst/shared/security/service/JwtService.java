package com.mattvorst.shared.security.service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.mattvorst.shared.dao.model.security.User;
import com.mattvorst.shared.security.JwtAuthenticationProvider;
import com.mattvorst.shared.security.constant.AccountType;
import com.mattvorst.shared.security.token.UserToken;
import com.mattvorst.shared.util.Utils;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public class JwtService implements JwtDecoder {
	private final JwtEncoder jwtEncoder;
	private final JwtDecoder jwtDecoder;
	private final AuthorizationServerSettings providerSettings;

	/**
	 * Constructs a {@code JwtGenerator} using the provided parameters.
	 *
	 * @param jwtEncoder the jwt encoder
	 */
	public JwtService(JwtDecoder jwtDecoder, JwtEncoder jwtEncoder, AuthorizationServerSettings providerSettings) {
		Assert.notNull(jwtEncoder, "jwtEncoder cannot be null");
		this.jwtDecoder = jwtDecoder;
		this.jwtEncoder = jwtEncoder;
		this.providerSettings = providerSettings;
	}

	public Jwt generateAdminJwt(UUID userUuid, String name, int expiryMinutes, String timeZone) {
		return generate(userUuid, name, expiryMinutes, timeZone, AccountType.ADMIN, null);
	}

	public Jwt generateUserJwt(User user, UUID homeUuid){
		String displayName = !Utils.empty(user.getFirstName()) && !Utils.empty(user.getFirstName()) ? user.getFirstName() + " " + user.getLastName() : "Anonymous";
		return generate(user.getUserUuid(), displayName, -1, user.getTimeZone(), AccountType.USER, Map.of(JwtAuthenticationProvider.HOME_UUID, homeUuid.toString()));
	}

	private Jwt generate(UUID userUuid, String name, int expiryMinutes, String timeZone, AccountType accountType, Map<String, Object> additionalClaimMap) {
		String issuer = null;
		if (this.providerSettings != null) {
			issuer = this.providerSettings.getIssuer();
		}

		Instant issuedAt = Instant.now();
		Instant expiresAt = expiryMinutes > 0 ?  issuedAt.plus(expiryMinutes, ChronoUnit.MINUTES) : null;

		JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder();
		if (StringUtils.hasText(issuer)) {
			claimsBuilder.issuer(issuer);
		}

		// In some cases (i.e. anonymous patients) we don't want an expiration date
		if (expiresAt != null) {
			claimsBuilder.expiresAt(expiresAt);
		}

		claimsBuilder.subject(userUuid.toString())
				.audience(List.of("MV"))
				.issuedAt(issuedAt)
				.claim(JwtAuthenticationProvider.SUB_TYPE, accountType.name())
				.claim(JwtAuthenticationProvider.NAME, name)
				.claim(JwtAuthenticationProvider.ACCOUNT_TYPE, accountType.name())
				.claim(JwtAuthenticationProvider.TIME_ZONE, timeZone != null ? timeZone : ZoneOffset.UTC.getId());

		if(!Utils.empty(additionalClaimMap)) {
			additionalClaimMap.forEach(claimsBuilder::claim);
		}

		JwsHeader.Builder headersBuilder = JwsHeader.with(SignatureAlgorithm.RS256);
		JwsHeader headers = headersBuilder.build();
		JwtClaimsSet claims = claimsBuilder.build();

		return this.jwtEncoder.encode(JwtEncoderParameters.from(headers, claims));
	}

	public Jwt updateUserTokenJwt(UserToken userToken, UUID homeUuid) {
		return generate(userToken.getUserUuid(), userToken.getName(), -1, userToken.getTimeZone(), AccountType.USER, Map.of(JwtAuthenticationProvider.HOME_UUID, homeUuid.toString()));
	}

	@Override
	public Jwt decode(String token) throws JwtException {

		return this.jwtDecoder.decode(token);
	}
}
