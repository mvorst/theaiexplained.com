package com.mattvorst.shared.security;

import com.mattvorst.shared.dao.SecurityDao;
import com.mattvorst.shared.security.constant.SubjectType;
import com.mattvorst.shared.security.token.ControllerToken;
import com.mattvorst.shared.security.token.ServiceAccountToken;
import com.mattvorst.shared.security.token.UserToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public class JwtAuthenticationProvider implements AuthenticationProvider {
	public static final String SUB_TYPE = "styp";
	public static final String NAME = "name";
	public static final String SCOPES = "scp";
	public static final String ACCOUNT_TYPE = "atyp";
	public static final String TIME_ZONE = "tzn";
	public static final String HOME_UUID = "homeid";
	private final JwtAuthenticationProvider jwtAuthenticationProvider;
	@Autowired private SecurityDao securityDao;

	public JwtAuthenticationProvider(JwtDecoder jwtDecoder) {
		this.jwtAuthenticationProvider = new JwtAuthenticationProvider(jwtDecoder);
	}

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		if (authentication instanceof BearerTokenAuthenticationToken bearerToken) {
			String rawToken = bearerToken.getToken();
			String unencryptedToken = AuthorizationUtils.decryptToken(rawToken);
			if (unencryptedToken != null) {
				Authentication bearerTokenAuthenticationToken = new BearerTokenAuthenticationToken(unencryptedToken);

				Jwt jwt = ((JwtAuthenticationToken) jwtAuthenticationProvider.authenticate(bearerTokenAuthenticationToken)).getToken();

				Object subjectTypeObj = jwt.getClaims().get(SUB_TYPE).toString();
				if (subjectTypeObj != null && SubjectType.valueOf(subjectTypeObj.toString().toUpperCase()) == SubjectType.CONTROLLER) {
					return new ControllerToken(jwt, true);
				} else if (subjectTypeObj != null && SubjectType.valueOf(subjectTypeObj.toString().toUpperCase()) == SubjectType.USER) {
					return new UserToken(jwt, true);
				} else if (subjectTypeObj != null && SubjectType.valueOf(subjectTypeObj.toString().toUpperCase()) == SubjectType.SERVICE_ACCOUNT) {
					return new ServiceAccountToken(jwt, true);
				}
			}
		}

		return null;
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return BearerTokenAuthenticationToken.class.isAssignableFrom(authentication);
	}

}
