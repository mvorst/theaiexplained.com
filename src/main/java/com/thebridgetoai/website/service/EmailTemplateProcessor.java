package com.thebridgetoai.website.service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.thebridgetoai.website.dao.model.Newsletter;
import com.thebridgetoai.website.dao.model.NewsletterTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Email template processor service.
 * Uses a dedicated Thymeleaf engine separate from web page rendering.
 * Web pages continue to use JSP as configured in WebConfig.
 */
@Service
public class EmailTemplateProcessor {

    @Autowired
    @Qualifier("emailTemplateEngine")
    private TemplateEngine emailTemplateEngine;

    @Autowired
    private NewsletterService newsletterService;

    /**
     * Generate the final HTML email by combining template wrapper with newsletter content
     * and replacing all variables with actual values.
     */
    public String generateHtmlEmail(UUID newsletterUuid, Map<String, String> recipientData) {
        Newsletter newsletter = newsletterService.getNewsletter(newsletterUuid);
        if (newsletter == null) {
            throw new IllegalArgumentException("Newsletter not found: " + newsletterUuid);
        }

        // If no template selected, return newsletter content as-is
        if (newsletter.getTemplateId() == null || newsletter.getTemplateId().isEmpty()) {
            return processVariablesInContent(newsletter.getHtmlContent(), newsletter, recipientData);
        }

        // Get the template
        NewsletterTemplate template = newsletterService.getTemplate(UUID.fromString(newsletter.getTemplateId()));
        if (template == null) {
            // Fallback to newsletter content if template not found
            return processVariablesInContent(newsletter.getHtmlContent(), newsletter, recipientData);
        }

        // Convert template syntax and process
        String thymeleafTemplate = convertToThymeleafSyntax(template.getHtmlContent());
        Context context = createTemplateContext(newsletter, recipientData);
        
        return emailTemplateEngine.process(thymeleafTemplate, context);
    }

    /**
     * Generate the final plain text email by combining template wrapper with newsletter content
     * and replacing all variables with actual values.
     */
    public String generateTextEmail(UUID newsletterUuid, Map<String, String> recipientData) {
        Newsletter newsletter = newsletterService.getNewsletter(newsletterUuid);
        if (newsletter == null) {
            throw new IllegalArgumentException("Newsletter not found: " + newsletterUuid);
        }

        // If no template selected, return newsletter text content as-is
        if (newsletter.getTemplateId() == null || newsletter.getTemplateId().isEmpty()) {
            return processVariablesInContent(newsletter.getTextContent(), newsletter, recipientData);
        }

        // Get the template
        NewsletterTemplate template = newsletterService.getTemplate(UUID.fromString(newsletter.getTemplateId()));
        if (template == null || template.getTextContent() == null) {
            // Fallback to newsletter text content if template not found or has no text version
            return processVariablesInContent(newsletter.getTextContent(), newsletter, recipientData);
        }

        // Convert template syntax and process
        String thymeleafTemplate = convertToThymeleafSyntax(template.getTextContent());
        Context context = createTemplateContext(newsletter, recipientData);
        
        return emailTemplateEngine.process(thymeleafTemplate, context);
    }

    /**
     * Generate preview HTML for the newsletter combined with its template.
     * Uses sample data for variables.
     */
    public String generatePreviewHtml(UUID newsletterUuid) {
        Map<String, String> sampleData = createSampleRecipientData();
        return generateHtmlEmail(newsletterUuid, sampleData);
    }

    /**
     * Convert {{variable}} syntax to Thymeleaf ${variable} syntax
     */
    private String convertToThymeleafSyntax(String content) {
        if (content == null) {
            return "";
        }
        // Convert {{variable}} to ${variable}
        return content.replaceAll("\\{\\{([^}]+)\\}\\}", "\\${$1}");
    }

    /**
     * Create Thymeleaf context with all available variables
     */
    private Context createTemplateContext(Newsletter newsletter, Map<String, String> recipientData) {
        Context context = new Context();
        
        // Add newsletter-specific variables
        context.setVariable("content", newsletter.getHtmlContent() != null ? newsletter.getHtmlContent() : "");
        context.setVariable("textContent", newsletter.getTextContent() != null ? newsletter.getTextContent() : "");
        context.setVariable("title", newsletter.getTitle() != null ? newsletter.getTitle() : "");
        context.setVariable("subject", newsletter.getSubject() != null ? newsletter.getSubject() : "");
        context.setVariable("previewText", newsletter.getPreviewText() != null ? newsletter.getPreviewText() : "");
        
        // Add system variables
        context.setVariable("currentDate", new Date());
        context.setVariable("newsletterUuid", newsletter.getNewsletterUuid().toString());
        
        // Add recipient-specific variables
        if (recipientData != null) {
            recipientData.forEach(context::setVariable);
        }
        
        // Add common email variables with defaults
        context.setVariable("unsubscribeLink", recipientData != null ? recipientData.get("unsubscribeLink") : "#unsubscribe");
        context.setVariable("webViewLink", recipientData != null ? recipientData.get("webViewLink") : "#webview");
        
        return context;
    }

    /**
     * Process variables in content when no template is used
     */
    private String processVariablesInContent(String content, Newsletter newsletter, Map<String, String> recipientData) {
        if (content == null) {
            return "";
        }
        
        String processedContent = content;
        
        // Replace newsletter variables
        processedContent = processedContent.replace("{{title}}", newsletter.getTitle() != null ? newsletter.getTitle() : "");
        processedContent = processedContent.replace("{{subject}}", newsletter.getSubject() != null ? newsletter.getSubject() : "");
        processedContent = processedContent.replace("{{previewText}}", newsletter.getPreviewText() != null ? newsletter.getPreviewText() : "");
        
        // Replace recipient variables
        if (recipientData != null) {
            for (Map.Entry<String, String> entry : recipientData.entrySet()) {
                processedContent = processedContent.replace("{{" + entry.getKey() + "}}", entry.getValue());
            }
        }
        
        // Replace common variables with defaults
        processedContent = processedContent.replace("{{unsubscribeLink}}", 
            recipientData != null ? recipientData.getOrDefault("unsubscribeLink", "#unsubscribe") : "#unsubscribe");
        processedContent = processedContent.replace("{{webViewLink}}", 
            recipientData != null ? recipientData.getOrDefault("webViewLink", "#webview") : "#webview");
        
        return processedContent;
    }

    /**
     * Create sample recipient data for previews
     */
    private Map<String, String> createSampleRecipientData() {
        Map<String, String> sampleData = new HashMap<>();
        sampleData.put("firstName", "John");
        sampleData.put("lastName", "Doe");
        sampleData.put("email", "john.doe@example.com");
        sampleData.put("companyName", "Example Company");
        sampleData.put("unsubscribeLink", "https://example.com/unsubscribe");
        sampleData.put("webViewLink", "https://example.com/newsletter/view");
        return sampleData;
    }
}