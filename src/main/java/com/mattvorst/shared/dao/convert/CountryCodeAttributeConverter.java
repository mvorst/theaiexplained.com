package com.mattvorst.shared.dao.convert;

import com.mattvorst.shared.constant.CountryCode;
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class CountryCodeAttributeConverter implements AttributeConverter<CountryCode> {

	@Override
	public AttributeValue transformFrom(CountryCode input) {
		return AttributeValue.builder().s(input.toString()).build();
	}

	@Override
	public CountryCode transformTo(AttributeValue input) {
		return CountryCode.valueOf(input.s());
	}

	@Override
	public EnhancedType<CountryCode> type() {
		return EnhancedType.of(CountryCode.class);
	}

	@Override
	public AttributeValueType attributeValueType() {
		return AttributeValueType.S;
	}
}

