package com.mattvorst.shared.dao.convert;


import com.mattvorst.shared.constant.AssetType;
import com.mattvorst.shared.util.Utils;
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class AssetTypeAttributeConverter implements AttributeConverter<AssetType> {

	@Override
	public AttributeValue transformFrom(AssetType assetType) {
		return AttributeValue.builder().s(assetType.toString()).build();
	}

	@Override
	public AssetType transformTo(AttributeValue input) {
		if(input == null || Utils.empty(input.s())){
			return null;
		}

		try {
			return Utils.enumFromString(input.s(), AssetType.class);
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public EnhancedType<AssetType> type() {
		return EnhancedType.of(AssetType.class);
	}

	@Override
	public AttributeValueType attributeValueType() {
		return AttributeValueType.S;
	}
}

