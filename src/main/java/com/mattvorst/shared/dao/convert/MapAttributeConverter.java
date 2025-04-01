package com.mattvorst.shared.dao.convert;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.io.IOException;
import java.util.Map;

public class MapAttributeConverter implements AttributeConverter<Map> {
	private static final ObjectMapper MAPPER = new ObjectMapper();

	@Override
	public AttributeValue transformFrom(Map input) {
		try {
			return AttributeValue.builder().s(MAPPER.writeValueAsString(input)).build();
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Unable to convert map to JSON string", e);
		}
	}

	@Override
	public Map<String, Object> transformTo(AttributeValue input) {
		try {
			return MAPPER.readValue(input.s(), Map.class);
		} catch (IOException e) {
			throw new RuntimeException("Unable to convert JSON string to map", e);
		}
	}

	@Override
	public EnhancedType<Map> type() {
		return EnhancedType.of(Map.class);
	}

	@Override
	public AttributeValueType attributeValueType() {
		return AttributeValueType.S;
	}
}