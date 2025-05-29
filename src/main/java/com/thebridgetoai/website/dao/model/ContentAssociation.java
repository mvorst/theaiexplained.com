package com.thebridgetoai.website.dao.model;

import java.util.Date;
import java.util.UUID;

import com.mattvorst.shared.dao.convert.DateAttributeConverter;
import com.mattvorst.shared.model.DefaultAuditable;
import com.mattvorst.shared.util.Utils;
import com.thebridgetoai.website.constant.ContentCategoryType;
import com.thebridgetoai.website.dao.convert.ContentCategoryTypeAttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbConvertedBy;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;

@DynamoDbBean
public class ContentAssociation extends DefaultAuditable {
	public static final String TABLE_NAME = "content_association";

	private UUID associationUuid;
	private UUID associatedContentUuid;
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
	public ContentAssociation() {}

	public ContentAssociation(UUID associationUuid) {
		this.associationUuid = associationUuid;
	}

	// Getters and Setters
	@DynamoDbPartitionKey
	@DynamoDbSecondarySortKey(indexNames = {"contentUuid-associationUuid-index"})
	public UUID getAssociationUuid() {
		return associationUuid;
	}

	public void setAssociationUuid(UUID associationUuid) {
		this.associationUuid = associationUuid;
	}

	@DynamoDbSecondaryPartitionKey(indexNames = {"associatedContentUuid-publishedDateAndContentUuid-index"})
	public UUID getAssociatedContentUuid() {
		return associatedContentUuid;
	}

	public void setAssociatedContentUuid(UUID associatedContentUuid) {
		this.associatedContentUuid = associatedContentUuid;
	}

	@DynamoDbSecondaryPartitionKey(indexNames = {"contentUuid-associationUuid-index"})
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

	@DynamoDbConvertedBy(ContentCategoryTypeAttributeConverter.class)
	public ContentCategoryType getContentCategoryType() {
		return contentCategoryType;
	}

	public void setContentCategoryType(ContentCategoryType contentCategoryType) {
		this.contentCategoryType = contentCategoryType;
	}

	@DynamoDbConvertedBy(DateAttributeConverter.class)
	public Date getPublishedDate() {
		return publishedDate;
	}

	public void setPublishedDate(Date publishedDate) {
		this.publishedDate = publishedDate;
	}

	@DynamoDbSecondarySortKey(indexNames = {"associatedContentUuid-publishedDateAndContentUuid-index", "associatedContentUuidAndContentCategoryType-publishedDateAndContentUuid-index"})
	public String getPublishedDateAndContentUuid() {
		return Utils.toUtcTimestamp(publishedDate) + "|" + contentUuid;
	}

	public void setPublishedDateAndContentUuid(String publishedDateAndContentUuid) {
	}

	@DynamoDbSecondaryPartitionKey(indexNames = {"associatedContentUuidAndContentCategoryType-publishedDateAndContentUuid-index"})
	public String getAssociatedContentUuidAndContentCategoryType() {
		return associatedContentUuid + "|" + contentCategoryType;
	}

	public void setAssociatedContentUuidAndContentCategoryType(String associatedContentUuidAndContentCategoryType) {
	}
}