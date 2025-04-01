package com.mattvorst.shared.dao.convert;


import com.mattvorst.shared.security.constant.Source;
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class SourceAttributeConverter implements AttributeConverter<Source> {

	@Override
	public AttributeValue transformFrom(Source input) {
		return AttributeValue.builder().s(input.toString()).build();
	}

	@Override
	public Source transformTo(AttributeValue input) {
		return Source.valueOf(input.s());
	}

	@Override
	public EnhancedType<Source> type() {
		return EnhancedType.of(Source.class);
	}

	@Override
	public AttributeValueType attributeValueType() {
		return AttributeValueType.S;
	}
}

