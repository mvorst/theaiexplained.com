package com.theaiexplained.website.model;

import java.util.Date;
import java.util.UUID;

import com.theaiexplained.website.constant.TemplateCategory;
import com.theaiexplained.website.dao.model.NewsletterTemplate;
import org.springframework.beans.BeanUtils;

public class ViewNewsletterTemplate {
    private UUID templateUuid;
    private String name;
    private String description;
    private String htmlContent;
    private String textContent;
    private String variables;
    private TemplateCategory category;
    private Date createdDate;
    private Date modifiedDate;

    // Default constructor
    public ViewNewsletterTemplate() {}

    // Constructor from NewsletterTemplate entity
    public ViewNewsletterTemplate(NewsletterTemplate template) {
        if (template != null) {
            BeanUtils.copyProperties(template, this);
        }
    }

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

    public TemplateCategory getCategory() {
        return category;
    }

    public void setCategory(TemplateCategory category) {
        this.category = category;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    // Helper methods for UI
    public String getCategoryDisplay() {
        if (category == null) return "General";
        switch (category) {
            case GENERAL:
                return "General";
            case MARKETING:
                return "Marketing";
            case ANNOUNCEMENT:
                return "Announcement";
            case NEWSLETTER:
                return "Newsletter";
            case WELCOME:
                return "Welcome";
            default:
                return category.name();
        }
    }
}