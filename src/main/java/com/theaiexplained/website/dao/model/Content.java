package com.theaiexplained.website.dao.model;

import java.util.UUID;

import com.mattvorst.shared.model.DefaultAuditable;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@DynamoDbBean
public class Content extends DefaultAuditable {
	public static final String TABLE_NAME = "content";

	private UUID contentUuid;
	private String cardHeaderImageUrl;
	private UUID cardHeaderImageFileUuid;
	private String cardTitle;
	private String cardSubtitle;
	private String cardCTATitle;
	private String headerImageUrl;
	private UUID headerImageFileUuid;
	private String title;
	private String subtitle;
	private String referenceUrl;
	private String referenceUrlTitle;
	private String markupContent;
	private String audioContentUrl;
	private UUID audioContentFileUuid;

	// SEO and meta tags
	private String metaTitle;
	private String metaDescription;
	private String metaType;
	private String metaUrl;
	private String metaImage;
	private String metaTwitterImageAltText;
	private String metaTwiterCard;
	private String metaFBAppId;
	private String metaTwitterSite;

	// Constructors
	public Content() {}

	public Content(UUID contentUuid) {
		this.contentUuid = contentUuid;
	}

	// Getters and Setters
	@DynamoDbPartitionKey
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

	public UUID getCardHeaderImageFileUuid() {
		return cardHeaderImageFileUuid;
	}

	public void setCardHeaderImageFileUuid(UUID cardHeaderImageFileUuid) {
		this.cardHeaderImageFileUuid = cardHeaderImageFileUuid;
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

	public String getHeaderImageUrl() {
		return headerImageUrl;
	}

	public void setHeaderImageUrl(String headerImageUrl) {
		this.headerImageUrl = headerImageUrl;
	}

	public UUID getHeaderImageFileUuid() {
		return headerImageFileUuid;
	}

	public void setHeaderImageFileUuid(UUID headerImageFileUuid) {
		this.headerImageFileUuid = headerImageFileUuid;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSubtitle() {
		return subtitle;
	}

	public void setSubtitle(String subtitle) {
		this.subtitle = subtitle;
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

	public String getMarkupContent() {
		return markupContent;
	}

	public void setMarkupContent(String markupContent) {
		this.markupContent = markupContent;
	}

	public String getAudioContentUrl() {
		return audioContentUrl;
	}

	public void setAudioContentUrl(String audioContentUrl) {
		this.audioContentUrl = audioContentUrl;
	}

	public UUID getAudioContentFileUuid() {
		return audioContentFileUuid;
	}

	public void setAudioContentFileUuid(UUID audioContentFileUuid) {
		this.audioContentFileUuid = audioContentFileUuid;
	}

	public String getMetaTitle() {
		return metaTitle;
	}

	public void setMetaTitle(String metaTitle) {
		this.metaTitle = metaTitle;
	}

	public String getMetaDescription() {
		return metaDescription;
	}

	public void setMetaDescription(String metaDescription) {
		this.metaDescription = metaDescription;
	}

	public String getMetaType() {
		return metaType;
	}

	public void setMetaType(String metaType) {
		this.metaType = metaType;
	}

	public String getMetaUrl() {
		return metaUrl;
	}

	public void setMetaUrl(String metaUrl) {
		this.metaUrl = metaUrl;
	}

	public String getMetaImage() {
		return metaImage;
	}

	public void setMetaImage(String metaImage) {
		this.metaImage = metaImage;
	}

	public String getMetaTwitterImageAltText() {
		return metaTwitterImageAltText;
	}

	public void setMetaTwitterImageAltText(String metaTwitterImageAltText) {
		this.metaTwitterImageAltText = metaTwitterImageAltText;
	}

	public String getMetaTwiterCard() {
		return metaTwiterCard;
	}

	public void setMetaTwiterCard(String metaTwiterCard) {
		this.metaTwiterCard = metaTwiterCard;
	}

	public String getMetaFBAppId() {
		return metaFBAppId;
	}

	public void setMetaFBAppId(String metaFBAppId) {
		this.metaFBAppId = metaFBAppId;
	}

	public String getMetaTwitterSite() {
		return metaTwitterSite;
	}

	public void setMetaTwitterSite(String metaTwitterSite) {
		this.metaTwitterSite = metaTwitterSite;
	}
}