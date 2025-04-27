package com.mattvorst.shared.dao.model.system;

import java.util.UUID;

import com.mattvorst.shared.constant.SystemMessageType;
import com.mattvorst.shared.dao.convert.SystemMessageTypeAttributeConverter;
import com.mattvorst.shared.model.DefaultAuditable;
import com.mattvorst.shared.util.Utils;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbConvertedBy;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;

@DynamoDbBean
public class UserSystemMessage extends DefaultAuditable {

	public static final String TABLE_NAME = "user_system_message";

	private UUID userUuid;
	private UUID messageUuid;
	private String title;
	private String message;
	private SystemMessageType systemMessageType;

	@DynamoDbSecondaryPartitionKey(indexNames = {"userUuid-createdDateAndMessageUuid-index","userUuid-messageUuid-index"})
	public UUID getUserUuid() {
		return userUuid;
	}

	public void setUserUuid(UUID userUuid) {
		this.userUuid = userUuid;
	}

	@DynamoDbPartitionKey
	@DynamoDbSecondarySortKey(indexNames = "userUuid-messageUuid-index")
	public UUID getMessageUuid() {
		return messageUuid;
	}

	public void setMessageUuid(UUID messageUuid) {
		this.messageUuid = messageUuid;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@DynamoDbConvertedBy(SystemMessageTypeAttributeConverter.class)
	public SystemMessageType getSystemMessageType() {
		return systemMessageType;
	}

	public void setSystemMessageType(SystemMessageType systemMessageType) {
		this.systemMessageType = systemMessageType;
	}

	@DynamoDbSecondarySortKey(indexNames = {"userUuid-createdDateAndMessageUuid-index"})
	public String getCreatedDateAndMessageUuid() {
		return Utils.toUtcTimestamp(getCreatedDate()) + "|" + getMessageUuid().toString();
	}
	public void setCreatedDateAndMessageUuid(String createdDateAndMessageUuid) {
	}
}

