package com.mattvorst.shared.dao.convert;

import com.mattvorst.shared.constant.SystemMessageType;
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class SystemMessageTypeAttributeConverter implements AttributeConverter<SystemMessageType> {

	@Override
	public AttributeValue transformFrom(SystemMessageType input) {
		return AttributeValue.builder().s(input.toString()).build();
	}

	@Override
	public SystemMessageType transformTo(AttributeValue input) {
		return SystemMessageType.valueOf(input.s());
	}

	@Override
	public EnhancedType<SystemMessageType> type() {
		return EnhancedType.of(SystemMessageType.class);
	}

	@Override
	public AttributeValueType attributeValueType() {
		return AttributeValueType.S;
	}
}

