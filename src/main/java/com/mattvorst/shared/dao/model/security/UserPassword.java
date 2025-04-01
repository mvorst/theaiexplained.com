package com.mattvorst.shared.dao.model.security;

import java.util.UUID;

import com.mattvorst.shared.constant.Status;
import com.mattvorst.shared.model.DefaultAuditable;
import software.amazon.awssdk.enhanced.dynamodb.extensions.annotations.DynamoDbVersionAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@DynamoDbBean
public class UserPassword extends DefaultAuditable {

	public static final String TABLE_NAME = "user_password";

	private UUID passwordUuid;
	private long revision;
	private Status status;
	private String encodedPassword;
	private Long version;

	@DynamoDbPartitionKey
	public UUID getPasswordUuid() {
		return passwordUuid;
	}

	public void setPasswordUuid(UUID passwordUuid) {
		this.passwordUuid = passwordUuid;
	}

	@DynamoDbSortKey
	public long getRevision() {
		return revision;
	}

	public void setRevision(long revision) {
		this.revision = revision;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public String getEncodedPassword() {
		return encodedPassword;
	}

	public void setEncodedPassword(String encodedPassword) {
		this.encodedPassword = encodedPassword;
	}

	@DynamoDbVersionAttribute
	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}
}
