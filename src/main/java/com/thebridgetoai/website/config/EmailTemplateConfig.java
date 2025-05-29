package com.thebridgetoai.website.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.StringTemplateResolver;

@Configuration
public class EmailTemplateConfig {

    /**
     * Template engine specifically configured for email templates only.
     * This is separate from the web template engine and won't interfere with JSP.
     * Uses StringTemplateResolver to process templates stored as strings in the database.
     */
    @Bean(name = "emailTemplateEngine")
    public TemplateEngine emailTemplateEngine() {
        // Use basic TemplateEngine instead of SpringTemplateEngine to avoid web interference
        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.addTemplateResolver(htmlEmailTemplateResolver());
        templateEngine.addTemplateResolver(textEmailTemplateResolver());
        return templateEngine;
    }

    /**
     * Template resolver for HTML email templates.
     */
    @Bean
    public StringTemplateResolver htmlEmailTemplateResolver() {
        StringTemplateResolver templateResolver = new StringTemplateResolver();
        templateResolver.setName("htmlEmailTemplateResolver");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCacheable(true);
        templateResolver.setCacheTTLMs(3600000L); // 1 hour cache
        templateResolver.setOrder(1);
        return templateResolver;
    }

    /**
     * Template resolver for plain text email templates.
     */
    @Bean
    public StringTemplateResolver textEmailTemplateResolver() {
        StringTemplateResolver templateResolver = new StringTemplateResolver();
        templateResolver.setName("textEmailTemplateResolver");
        templateResolver.setTemplateMode(TemplateMode.TEXT);
        templateResolver.setCacheable(true);
        templateResolver.setCacheTTLMs(3600000L); // 1 hour cache
        templateResolver.setOrder(2);
        return templateResolver;
    }
}