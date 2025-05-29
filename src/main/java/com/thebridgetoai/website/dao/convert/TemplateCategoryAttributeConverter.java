package com.thebridgetoai.website.dao.convert;

import com.thebridgetoai.website.constant.TemplateCategory;
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class TemplateCategoryAttributeConverter implements AttributeConverter<TemplateCategory> {

    @Override
    public AttributeValue transformFrom(TemplateCategory input) {
        return input == null ? AttributeValue.builder().nul(true).build() 
                            : AttributeValue.builder().s(input.name()).build();
    }

    @Override
    public TemplateCategory transformTo(AttributeValue input) {
        if (input.nul() != null && input.nul()) {
            return null;
        }
        return TemplateCategory.valueOf(input.s());
    }

    @Override
    public EnhancedType<TemplateCategory> type() {
        return EnhancedType.of(TemplateCategory.class);
    }

    @Override
    public AttributeValueType attributeValueType() {
        return AttributeValueType.S;
    }
}