package com.mattvorst.shared.dao.convert;


import com.mattvorst.shared.constant.EnvironmentType;
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class EnvironmentTypeAttributeConverter implements AttributeConverter<EnvironmentType> {

	@Override
	public AttributeValue transformFrom(EnvironmentType input) {
		return AttributeValue.builder().s(input.toString()).build();
	}

	@Override
	public EnvironmentType transformTo(AttributeValue input) {
		return EnvironmentType.valueOf(input.s());
	}

	@Override
	public EnhancedType<EnvironmentType> type() {
		return EnhancedType.of(EnvironmentType.class);
	}

	@Override
	public AttributeValueType attributeValueType() {
		return AttributeValueType.S;
	}
}

