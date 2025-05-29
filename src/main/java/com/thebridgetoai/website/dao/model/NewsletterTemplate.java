package com.thebridgetoai.website.dao.model;

import java.util.Date;
import java.util.UUID;

import com.mattvorst.shared.dao.convert.DateAttributeConverter;
import com.mattvorst.shared.model.DefaultAuditable;
import com.thebridgetoai.website.constant.TemplateCategory;
import com.thebridgetoai.website.dao.convert.TemplateCategoryAttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.extensions.annotations.DynamoDbVersionAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbConvertedBy;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;

@DynamoDbBean
public class NewsletterTemplate extends DefaultAuditable {

    public static final String TABLE_NAME = "newsletter_template";
    public static final String GSI_CATEGORY_CREATED_DATE = "category-createdDate-index";
    public static final String GSI_CREATED_DATE_TEMPLATE_UUID = "createdDate-templateUuid-index";

    private UUID templateUuid;
    private String name;
    private String description;
    private String htmlContent;
    private String textContent;
    private String variables;
    private TemplateCategory category;
    private Long version;

    @DynamoDbPartitionKey
    @DynamoDbSecondarySortKey(indexNames = {GSI_CREATED_DATE_TEMPLATE_UUID})
    public UUID getTemplateUuid() {
        return templateUuid;
    }

    public void setTemplateUuid(UUID templateUuid) {
        this.templateUuid = templateUuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHtmlContent() {
        return htmlContent;
    }

    public void setHtmlContent(String htmlContent) {
        this.htmlContent = htmlContent;
    }

    public String getTextContent() {
        return textContent;
    }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }

    public String getVariables() {
        return variables;
    }

    public void setVariables(String variables) {
        this.variables = variables;
    }

    @DynamoDbSecondaryPartitionKey(indexNames = {GSI_CATEGORY_CREATED_DATE})
    @DynamoDbConvertedBy(TemplateCategoryAttributeConverter.class)
    public TemplateCategory getCategory() {
        return category;
    }

    public void setCategory(TemplateCategory category) {
        this.category = category;
    }

    @DynamoDbVersionAttribute
    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @Override
    @DynamoDbConvertedBy(DateAttributeConverter.class)
    @DynamoDbSecondaryPartitionKey(indexNames = {GSI_CREATED_DATE_TEMPLATE_UUID})
    public Date getCreatedDate() {
        return super.getCreatedDate();
    }
}