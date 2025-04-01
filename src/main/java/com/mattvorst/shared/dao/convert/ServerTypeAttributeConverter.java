package com.mattvorst.shared.dao.convert;


import com.mattvorst.shared.constant.ServerType;
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class ServerTypeAttributeConverter implements AttributeConverter<ServerType> {

	@Override
	public AttributeValue transformFrom(ServerType input) {
		return AttributeValue.builder().s(input.toString()).build();
	}

	@Override
	public ServerType transformTo(AttributeValue input) {
		return ServerType.valueOf(input.s());
	}

	@Override
	public EnhancedType<ServerType> type() {
		return EnhancedType.of(ServerType.class);
	}

	@Override
	public AttributeValueType attributeValueType() {
		return AttributeValueType.S;
	}
}

