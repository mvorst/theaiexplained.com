package com.thebridgetoai.website.dao.model.user;

import java.util.UUID;

import com.mattvorst.shared.model.DefaultAuditable;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@DynamoDbBean
public class UserProperties extends DefaultAuditable {

	public static final String TABLE_NAME = "user_properties";
	private UUID userUuid;
	private UUID defaultHomeUuid;

	@DynamoDbPartitionKey
	public UUID getUserUuid() {
		return userUuid;
	}

	public void setUserUuid(UUID userUuid) {
		this.userUuid = userUuid;
	}

	public UUID getDefaultHomeUuid() {
		return defaultHomeUuid;
	}

	public void setDefaultHomeUuid(UUID defaultHomeUuid) {
		this.defaultHomeUuid = defaultHomeUuid;
	}
}
