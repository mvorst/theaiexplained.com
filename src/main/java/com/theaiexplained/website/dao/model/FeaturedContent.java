package com.theaiexplained.website.dao.model;

import java.util.Date;
import java.util.UUID;

import com.mattvorst.shared.dao.convert.DateAttributeConverter;
import com.mattvorst.shared.model.DefaultAuditable;
import com.mattvorst.shared.util.Utils;
import com.theaiexplained.website.constant.ContentCategoryType;
import com.theaiexplained.website.dao.convert.ContentCategoryTypeAttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbConvertedBy;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@DynamoDbBean
public class FeaturedContent extends DefaultAuditable {
	public static final String TABLE_NAME = "featured_content";

	private UUID contentUuid;
	private ContentCategoryType contentCategoryType;
	private String cardHeaderImageUrl;
	private String cardTitle;
	private String cardSubtitle;
	private String cardCTATitle;
	private String referenceUrl;
	private String referenceUrlTitle;
	private Date publishedDate;

	// Constructors
	public FeaturedContent() {}

	public FeaturedContent(ContentCategoryType contentCategoryType, UUID contentUuid) {
		this.contentCategoryType = contentCategoryType;
		this.contentUuid = contentUuid;
	}

	// Getters and Setters
	@DynamoDbPartitionKey
	@DynamoDbSecondaryPartitionKey(indexNames = {"contentCategoryType-publishedDateAndContentUuid-index"})
	@DynamoDbSecondarySortKey(indexNames = {"contentUuid-contentCategoryType-index"})
	@DynamoDbConvertedBy(ContentCategoryTypeAttributeConverter.class)
	public ContentCategoryType getContentCategoryType() {
		return contentCategoryType;
	}

	public void setContentCategoryType(ContentCategoryType contentCategoryType) {
		this.contentCategoryType = contentCategoryType;
	}

	@DynamoDbSortKey
	@DynamoDbSecondaryPartitionKey(indexNames = {"contentUuid-contentCategoryType-index"})
	public UUID getContentUuid() {
		return contentUuid;
	}

	public void setContentUuid(UUID contentUuid) {
		this.contentUuid = contentUuid;
	}

	public String getCardHeaderImageUrl() {
		return cardHeaderImageUrl;
	}

	public void setCardHeaderImageUrl(String cardHeaderImageUrl) {
		this.cardHeaderImageUrl = cardHeaderImageUrl;
	}

	public String getCardTitle() {
		return cardTitle;
	}

	public void setCardTitle(String cardTitle) {
		this.cardTitle = cardTitle;
	}

	public String getCardSubtitle() {
		return cardSubtitle;
	}

	public void setCardSubtitle(String cardSubtitle) {
		this.cardSubtitle = cardSubtitle;
	}

	public String getCardCTATitle() {
		return cardCTATitle;
	}

	public void setCardCTATitle(String cardCTATitle) {
		this.cardCTATitle = cardCTATitle;
	}

	public String getReferenceUrl() {
		return referenceUrl;
	}

	public void setReferenceUrl(String referenceUrl) {
		this.referenceUrl = referenceUrl;
	}

	public String getReferenceUrlTitle() {
		return referenceUrlTitle;
	}

	public void setReferenceUrlTitle(String referenceUrlTitle) {
		this.referenceUrlTitle = referenceUrlTitle;
	}

	@DynamoDbConvertedBy(DateAttributeConverter.class)
	public Date getPublishedDate() {
		return publishedDate;
	}

	public void setPublishedDate(Date publishedDate) {
		this.publishedDate = publishedDate;
	}
	@DynamoDbSecondarySortKey(indexNames = {"contentCategoryType-publishedDateAndContentUuid-index"})
	public String getPublishedDateAndContentUuid() {
		return Utils.toUtcTimestamp(publishedDate) + "|" + contentUuid;
	}

	public void setPublishedDateAndContentUuid(String publishedDateAndContentUuid) {
	}
}