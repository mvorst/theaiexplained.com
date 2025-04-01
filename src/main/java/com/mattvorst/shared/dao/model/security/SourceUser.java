package com.mattvorst.shared.dao.model.security;

import java.util.Date;
import java.util.UUID;

import com.mattvorst.shared.dao.convert.DateAttributeConverter;
import com.mattvorst.shared.dao.convert.SourceAttributeConverter;
import com.mattvorst.shared.model.DefaultAuditable;
import com.mattvorst.shared.security.constant.Source;
import com.mattvorst.shared.util.Utils;
import software.amazon.awssdk.enhanced.dynamodb.extensions.annotations.DynamoDbVersionAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbConvertedBy;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;

@DynamoDbBean
public class SourceUser extends DefaultAuditable {
	public static final String TABLE_NAME = "source_user";

	private Source source;
	private String sourceId;
	private UUID sourceUserUuid;
	private UUID userUuid;
	private boolean verified;
	private boolean defaultSource;
	private Date verifiedDate;
	private Date initialVerifiedDate;
	Long version;

	public String getSourceId() {
		return sourceId;
	}

	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}

	@DynamoDbConvertedBy(SourceAttributeConverter.class)
	public Source getSource() {
		return source;
	}

	public void setSource(Source source) {
		this.source = source;
	}

	public UUID getSourceUserUuid() {
		return sourceUserUuid;
	}

	public void setSourceUserUuid(UUID sourceUserUuid) {
		this.sourceUserUuid = sourceUserUuid;
	}

	@DynamoDbSecondaryPartitionKey(indexNames = "userUuid-sourceAndSourceId-index")
	public UUID getUserUuid() {
		return userUuid;
	}

	public void setUserUuid(UUID userUuid) {
		this.userUuid = userUuid;
	}

	public boolean isVerified() {
		return verified;
	}

	public void setVerified(boolean verified) {
		this.verified = verified;
	}

	public boolean isDefaultSource() {
		return defaultSource;
	}

	public void setDefaultSource(boolean defaultSource) {
		this.defaultSource = defaultSource;
	}

	@DynamoDbConvertedBy(DateAttributeConverter.class)
	public Date getVerifiedDate() {
		return verifiedDate;
	}

	public void setVerifiedDate(Date verifiedDate) {
		this.verifiedDate = verifiedDate;
	}

	@DynamoDbConvertedBy(DateAttributeConverter.class)
	public Date getInitialVerifiedDate() {
		return initialVerifiedDate;
	}

	public void setInitialVerifiedDate(Date initialVerifiedDate) {
		this.initialVerifiedDate = initialVerifiedDate;
	}

	@DynamoDbPartitionKey
	@DynamoDbAttribute("sourceAndSourceId")
	@DynamoDbSecondarySortKey(indexNames = "userUuid-sourceAndSourceId-index")
	public String getSourceAndSourceId() {
		return source + "|" + Utils.uppercaseTrimmed(sourceId);
	}

	public void setSourceAndSourceId(String sourceAndSourceId) {}

	@DynamoDbVersionAttribute
	public Long getVersion() {
		return version;
	}
	public void setVersion(Long version) {
		this.version = version;
	}
}
