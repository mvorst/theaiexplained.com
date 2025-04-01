package com.mattvorst.shared.security.token;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import com.google.gson.JsonArray;
import com.mattvorst.shared.security.JwtAuthenticationProvider;
import com.mattvorst.shared.security.constant.Permission;
import com.mattvorst.shared.security.constant.SubjectType;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

public abstract class AuthToken extends AbstractAuthenticationToken {
	private List<Permission> permisisons;

	public AuthToken(Jwt jwt) {
		super(buildAuthorities(jwt));
	}

	private static Collection<? extends GrantedAuthority> buildAuthorities(Jwt jwt) {
		JsonArray scopeArray = (JsonArray) jwt.getClaims().get(JwtAuthenticationProvider.SCOPES);

		List<SimpleGrantedAuthority> authorities = new ArrayList<>();
		if (scopeArray != null) {
			scopeArray.forEach(s -> authorities.add(new SimpleGrantedAuthority(s.toString())));
		}

		return authorities;
	}

	private static Collection<? extends GrantedAuthority> buildAuthorities(List<Permission> permissionList) {
		List<SimpleGrantedAuthority> authorities = new ArrayList<>();

		if (permissionList != null) {
			permissionList.forEach(p -> authorities.add(new SimpleGrantedAuthority(p.toString())));
		}

		return authorities;
	}

	public abstract SubjectType getSubjectType();
	public abstract UUID getSubjectUuid();

	public String getSubject() {
		return buildSubject(getSubjectType(), getSubjectUuid());
	}

	public String getTimeZone() {
		return ZoneOffset.UTC.toString();
	}

	public static String buildSubject(SubjectType subjectType, UUID subjectUuid) {
		return subjectType.name() + "/" + subjectUuid;
	}
}
