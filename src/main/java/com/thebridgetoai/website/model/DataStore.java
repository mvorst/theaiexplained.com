package com.thebridgetoai.website.model;

import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mattvorst.shared.model.DefaultAuditable;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbIgnore;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@DynamoDbBean
public class DataStore extends DefaultAuditable {
	public static final String TABLE_NAME = "data_store";
	
	private UUID applicationUuid;
	private String namespace;
	private String id;
	private String dataValue;
	private String className;
	
	private Map<String, Object> data;
	
	@DynamoDbPartitionKey
	@DynamoDbAttribute("dataKey")
	public String getDataKey() {
		if (applicationUuid != null && namespace != null && id != null) {
			return applicationUuid.toString() + "|" + namespace + "|" + id;
		}
		return null;
	}
	
	public void setDataKey(String dataKey) {
		if (dataKey != null) {
			String[] parts = dataKey.split("\\|", 3);
			if (parts.length >= 3) {
				try {
					this.applicationUuid = UUID.fromString(parts[0]);
					this.namespace = parts[1];
					this.id = parts[2];
				} catch (IllegalArgumentException e) {
					// Handle invalid UUID format
				}
			}
		}
	}
	
	public UUID getApplicationUuid() {
		return applicationUuid;
	}
	
	public void setApplicationUuid(UUID applicationUuid) {
		this.applicationUuid = applicationUuid;
	}
	
	public String getNamespace() {
		return namespace;
	}
	
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getDataValue() {
		return dataValue;
	}
	
	public void setDataValue(String dataValue) {
		this.dataValue = dataValue;
	}
	
	public String getClassName() {
		return className;
	}
	
	public void setClassName(String className) {
		this.className = className;
	}

	@DynamoDbIgnore
	@JsonIgnore
	public Map<String, Object> getData() {
		return data;
	}
	
	public void setData(Map<String, Object> data) {
		this.data = data;
	}
}