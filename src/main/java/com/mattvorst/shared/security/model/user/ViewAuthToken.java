package com.mattvorst.shared.security.model.user;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ViewAuthToken {
	private String tokenValue;
	private String tokenType;
	private Long expiresIn;

	@JsonProperty("access_token")
	public String getTokenValue() {
		return tokenValue;
	}

	public void setTokenValue(String tokenValue) {
		this.tokenValue = tokenValue;
	}

	@JsonProperty("token_type")
	public String getTokenType() {
		return tokenType;
	}

	public void setTokenType(String tokenType) {
		this.tokenType = tokenType;
	}

	@JsonProperty("expires_in")
	public Long getExpiresIn() {
		return expiresIn;
	}

	public void setExpiresIn(Long expiresIn) {
		this.expiresIn = expiresIn;
	}
}
