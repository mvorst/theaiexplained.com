package com.thebridgetoai.website.dao.convert;

import com.thebridgetoai.website.constant.ContentCategoryType;
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class ContentCategoryTypeAttributeConverter implements AttributeConverter<ContentCategoryType> {

	@Override
	public AttributeValue transformFrom(ContentCategoryType input) {
		return AttributeValue.builder().s(input.toString()).build();
	}

	@Override
	public ContentCategoryType transformTo(AttributeValue input) {
		return ContentCategoryType.valueOf(input.s());
	}

	@Override
	public EnhancedType<ContentCategoryType> type() {
		return EnhancedType.of(ContentCategoryType.class);
	}

	@Override
	public AttributeValueType attributeValueType() {
		return AttributeValueType.S;
	}
}

