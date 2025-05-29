package com.thebridgetoai.website.model;

import java.util.Date;
import java.util.UUID;

import com.mattvorst.shared.constant.Status;
import com.thebridgetoai.website.dao.model.Newsletter;
import org.springframework.beans.BeanUtils;

public class ViewNewsletter {
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
    private Date createdDate;
    private Date updatedDate;

    // Default constructor
    public ViewNewsletter() {}

    // Constructor from Newsletter entity
    public ViewNewsletter(Newsletter newsletter) {
        if (newsletter != null) {
            BeanUtils.copyProperties(newsletter, this);
        }
    }

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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Date getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(Date scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

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

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }

    // Helper methods for UI
    public String getStatusDisplay() {
        if (status == null) return "Draft";
        switch (status) {
            case ACTIVE:
                return "Scheduled";
            case INACTIVE:
                return "Draft";
            case ARCHIVED:
                return "Sent";
            default:
                return status.name();
        }
    }

    public double getOpenRate() {
        if (deliveredCount == null || deliveredCount == 0 || openedCount == null) return 0.0;
        return (double) openedCount / deliveredCount * 100;
    }

    public double getClickRate() {
        if (deliveredCount == null || deliveredCount == 0 || clickedCount == null) return 0.0;
        return (double) clickedCount / deliveredCount * 100;
    }

    public double getBounceRate() {
        if (totalRecipients == null || totalRecipients == 0 || bouncedCount == null) return 0.0;
        return (double) bouncedCount / totalRecipients * 100;
    }
}