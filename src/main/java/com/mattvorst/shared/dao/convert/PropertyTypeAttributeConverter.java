package com.mattvorst.shared.dao.convert;

import com.mattvorst.shared.constant.PropertyType;
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class PropertyTypeAttributeConverter implements AttributeConverter<PropertyType> {

	@Override
	public AttributeValue transformFrom(PropertyType input) {
		return AttributeValue.builder().s(input.toString()).build();
	}

	@Override
	public PropertyType transformTo(AttributeValue input) {
		return PropertyType.valueOf(input.s());
	}

	@Override
	public EnhancedType<PropertyType> type() {
		return EnhancedType.of(PropertyType.class);
	}

	@Override
	public AttributeValueType attributeValueType() {
		return AttributeValueType.S;
	}
}

