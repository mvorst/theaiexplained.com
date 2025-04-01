package com.mattvorst.shared.dao.model.security;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.mattvorst.shared.constant.CountryCode;
import com.mattvorst.shared.constant.Status;
import com.mattvorst.shared.dao.convert.CountryCodeAttributeConverter;
import com.mattvorst.shared.dao.convert.DateAttributeConverter;
import com.mattvorst.shared.model.DefaultAuditable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import software.amazon.awssdk.enhanced.dynamodb.extensions.annotations.DynamoDbVersionAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbConvertedBy;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbIgnore;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@DynamoDbBean
public class User extends DefaultAuditable implements UserDetails {

	public static final String TABLE_NAME = "user";

	private UUID userUuid;
	private UUID passwordUuid;
	private String defaultEmailAddress;
	private Status status;
	private String firstName;
	private String lastName;
	private boolean registered;
	private String timeZone;
	private CountryCode defaultPhoneCountry;
	private String defaultPhoneNumber;
	private String defaultLocale;
	private Date joinedDate;
	private String encodedPassword;
	private boolean emailVerified;
	private List<GrantedAuthority> authorities;
	private boolean accountNonExpired;
	private boolean accountNonLocked;
	private boolean credentialsNonExpired;
	private Long version;

	@DynamoDbVersionAttribute
	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	@DynamoDbPartitionKey
	public UUID getUserUuid() {
		return userUuid;
	}

	public void setUserUuid(UUID userUuid) {
		this.userUuid = userUuid;
	}

	public UUID getPasswordUuid() {
		return passwordUuid;
	}

	public void setPasswordUuid(UUID passwordUuid) {
		this.passwordUuid = passwordUuid;
	}

	public String getDefaultEmailAddress() {
		return defaultEmailAddress;
	}

	public void setDefaultEmailAddress(String defaultEmailAddress) {
		this.defaultEmailAddress = defaultEmailAddress;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public boolean isRegistered() {
		return registered;
	}

	public void setRegistered(boolean registered) {
		this.registered = registered;
	}

	public String getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}

	@DynamoDbConvertedBy(CountryCodeAttributeConverter.class)
	public CountryCode getDefaultPhoneCountry() {
		return defaultPhoneCountry;
	}

	public void setDefaultPhoneCountry(CountryCode defaultPhoneCountry) {
		this.defaultPhoneCountry = defaultPhoneCountry;
	}

	public String getDefaultPhoneNumber() {
		return defaultPhoneNumber;
	}

	public void setDefaultPhoneNumber(String defaultPhoneNumber) {
		this.defaultPhoneNumber = defaultPhoneNumber;
	}

	@DynamoDbConvertedBy(DateAttributeConverter.class)
	public Date getJoinedDate() {
		return joinedDate;
	}

	public void setJoinedDate(Date joinedDate) {
		this.joinedDate = joinedDate;
	}

	public String getEncodedPassword() {
		return encodedPassword;
	}

	public void setEncodedPassword(String encodedPassword) {
		this.encodedPassword = encodedPassword;
	}

	public String getDefaultLocale() {
		return defaultLocale;
	}

	public void setDefaultLocale(String defaultLocale) {
		this.defaultLocale = defaultLocale;
	}

	@DynamoDbIgnore
	public boolean isEmailVerified() {
		return emailVerified;
	}

	@DynamoDbIgnore
	public void setEmailVerified(boolean emailVerified) {
		this.emailVerified = emailVerified;
	}

	@DynamoDbIgnore
	public void setAuthorities(List<GrantedAuthority> authorities) {
		this.authorities = authorities;
	}

	@DynamoDbIgnore
	public void setAccountNonExpired(boolean accountNonExpired) {
		this.accountNonExpired = accountNonExpired;
	}

	@DynamoDbIgnore
	public void setAccountNonLocked(boolean accountNonLocked) {
		this.accountNonLocked = accountNonLocked;
	}

	@DynamoDbIgnore
	public void setCredentialsNonExpired(boolean credentialsNonExpired) {
		this.credentialsNonExpired = credentialsNonExpired;
	}

	@DynamoDbIgnore
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@DynamoDbIgnore
	@Override
	public String getPassword() {
		return getEncodedPassword();
	}

	@DynamoDbIgnore
	@Override
	public String getUsername() {
		return null;
	}

	@DynamoDbIgnore
	@Override
	public boolean isAccountNonExpired() {
		return accountNonExpired;
	}

	@DynamoDbIgnore
	@Override
	public boolean isAccountNonLocked() {
		return accountNonLocked;
	}

	@DynamoDbIgnore
	@Override
	public boolean isCredentialsNonExpired() {
		return credentialsNonExpired;
	}

	@DynamoDbIgnore
	@Override
	public boolean isEnabled() {
		return Status.ACTIVE.equals(getStatus());
	}
}
