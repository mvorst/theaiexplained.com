package com.mattvorst.shared.dao.convert;


import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.mattvorst.shared.util.Utils;
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class DateAttributeConverter implements AttributeConverter<Date> {

	private final static DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	@Override
	public AttributeValue transformFrom(Date date) {
		return AttributeValue.builder().s(DATE_FORMAT.format(date)).build();
	}

	@Override
	public Date transformTo(AttributeValue input) {
		if(input == null || Utils.empty(input.s())){
			return null;
		}

		try {
			return DATE_FORMAT.parse(input.s());
		} catch (Exception e) {
			try {
				return new Date(Long.parseLong(input.n()));
			}catch (NumberFormatException nfe) {
				return null;
			}
		}
	}

	@Override
	public EnhancedType<Date> type() {
		return EnhancedType.of(Date.class);
	}

	@Override
	public AttributeValueType attributeValueType() {
		return AttributeValueType.S;
	}
}

