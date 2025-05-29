package com.thebridgetoai.website.dao.model;

import java.util.Date;
import java.util.UUID;

import com.mattvorst.shared.constant.Status;
import com.mattvorst.shared.dao.convert.DateAttributeConverter;
import com.mattvorst.shared.model.DefaultAuditable;
import software.amazon.awssdk.enhanced.dynamodb.extensions.annotations.DynamoDbVersionAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbConvertedBy;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;

@DynamoDbBean
public class Newsletter extends DefaultAuditable {

    public static final String TABLE_NAME = "newsletter";
    public static final String GSI_STATUS_CREATED_DATE = "status-createdDate-index";
    public static final String GSI_CREATED_DATE_NEWSLETTER_UUID = "createdDate-newsletterUuid-index";


    private UUID newsletterUuid;
    private String title;
    private String subject;
    private String previewText;
    private String htmlContent;
    private String textContent;
    private Status status;
    private Date scheduledDate;
    private Date sentDate;
    private Integer totalRecipients;
    private Integer deliveredCount;
    private Integer openedCount;
    private Integer clickedCount;
    private Integer bouncedCount;
    private String templateId;
    private String campaignTags;
    private Long version;

    @DynamoDbPartitionKey
    @DynamoDbSecondarySortKey(indexNames = {GSI_CREATED_DATE_NEWSLETTER_UUID})
    public UUID getNewsletterUuid() {
        return newsletterUuid;
    }

    public void setNewsletterUuid(UUID newsletterUuid) {
        this.newsletterUuid = newsletterUuid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getPreviewText() {
        return previewText;
    }

    public void setPreviewText(String previewText) {
        this.previewText = previewText;
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

    @DynamoDbSecondaryPartitionKey(indexNames = {GSI_STATUS_CREATED_DATE})
    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @DynamoDbConvertedBy(DateAttributeConverter.class)
    public Date getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(Date scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    @DynamoDbConvertedBy(DateAttributeConverter.class)
    public Date getSentDate() {
        return sentDate;
    }

    public void setSentDate(Date sentDate) {
        this.sentDate = sentDate;
    }

    public Integer getTotalRecipients() {
        return totalRecipients;
    }

    public void setTotalRecipients(Integer totalRecipients) {
        this.totalRecipients = totalRecipients;
    }

    public Integer getDeliveredCount() {
        return deliveredCount;
    }

    public void setDeliveredCount(Integer deliveredCount) {
        this.deliveredCount = deliveredCount;
    }

    public Integer getOpenedCount() {
        return openedCount;
    }

    public void setOpenedCount(Integer openedCount) {
        this.openedCount = openedCount;
    }

    public Integer getClickedCount() {
        return clickedCount;
    }

    public void setClickedCount(Integer clickedCount) {
        this.clickedCount = clickedCount;
    }

    public Integer getBouncedCount() {
        return bouncedCount;
    }

    public void setBouncedCount(Integer bouncedCount) {
        this.bouncedCount = bouncedCount;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getCampaignTags() {
        return campaignTags;
    }

    public void setCampaignTags(String campaignTags) {
        this.campaignTags = campaignTags;
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
    @DynamoDbSecondaryPartitionKey(indexNames = {GSI_CREATED_DATE_NEWSLETTER_UUID})
    public Date getCreatedDate() {
        return super.getCreatedDate();
    }
}