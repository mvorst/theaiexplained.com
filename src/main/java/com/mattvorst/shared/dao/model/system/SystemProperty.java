package com.mattvorst.shared.dao.model.system;

import com.mattvorst.shared.constant.PropertyType;
import com.mattvorst.shared.dao.convert.PropertyTypeAttributeConverter;
import com.mattvorst.shared.model.DefaultAuditable;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbConvertedBy;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@DynamoDbBean
public class SystemProperty extends DefaultAuditable {

	public static final String TABLE_NAME = "system_properties";

	private PropertyType propertyType;
	private String value;
	private String description;

	@DynamoDbPartitionKey
	@DynamoDbConvertedBy(PropertyTypeAttributeConverter.class)
	public PropertyType getPropertyType() {
		return propertyType;
	}

	public void setPropertyType(PropertyType propertyType) {
		this.propertyType = propertyType;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
