package com.mattvorst.shared.dao.model.file;

import java.util.UUID;

import com.mattvorst.shared.constant.AssetType;
import com.mattvorst.shared.model.DefaultAuditable;
import com.mattvorst.shared.dao.convert.AssetTypeAttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbConvertedBy;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbIgnore;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@DynamoDbBean
public class File extends DefaultAuditable {

	public static final String TABLE_NAME = "file";

	private UUID fileUuid;
	private String name;
	private String details;
	private String contentType;
	private String s3Bucket;
	private String s3Key;
	private String eTag;
	private long size;
	private AssetType assetType;
	private String url;

	@DynamoDbPartitionKey
	public UUID getFileUuid() {
		return fileUuid;
	}

	public void setFileUuid(UUID fileUuid) {
		this.fileUuid = fileUuid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getS3Bucket() {
		return s3Bucket;
	}

	public void setS3Bucket(String s3Bucket) {
		this.s3Bucket = s3Bucket;
	}

	public String getS3Key() {
		return s3Key;
	}

	public void setS3Key(String s3Key) {
		this.s3Key = s3Key;
	}

	public String getETag() {
		return eTag;
	}

	public void setETag(String eTag) {
		this.eTag = eTag;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	@DynamoDbConvertedBy(AssetTypeAttributeConverter.class)
	public AssetType getAssetType() {
		return assetType;
	}

	public void setAssetType(AssetType assetType) {
		this.assetType = assetType;
	}

	@DynamoDbIgnore
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
