package com.thebridgetoai.website.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for template processing and variable management.
 */
public class TemplateUtils {

    // Pattern to match {{variableName}} syntax
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{([^}]+)\\}\\}");
    
    // Pattern to match ${variableName} Thymeleaf syntax
    private static final Pattern THYMELEAF_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");

    /**
     * Convert {{variable}} syntax to Thymeleaf ${variable} syntax
     */
    public static String convertToThymeleafSyntax(String content) {
        if (content == null) {
            return "";
        }
        return content.replaceAll("\\{\\{([^}]+)\\}\\}", "\\${$1}");
    }

    /**
     * Convert Thymeleaf ${variable} syntax back to {{variable}} syntax
     * Useful for storing templates in the database with consistent syntax
     */
    public static String convertFromThymeleafSyntax(String content) {
        if (content == null) {
            return "";
        }
        return content.replaceAll("\\$\\{([^}]+)\\}", "{{$1}}");
    }

    /**
     * Extract all variable names from template content using {{variable}} syntax
     */
    public static Set<String> extractVariables(String content) {
        Set<String> variables = new HashSet<>();
        if (content == null) {
            return variables;
        }

        Matcher matcher = VARIABLE_PATTERN.matcher(content);
        while (matcher.find()) {
            variables.add(matcher.group(1).trim());
        }
        
        return variables;
    }

    /**
     * Extract all variable names from Thymeleaf template content using ${variable} syntax
     */
    public static Set<String> extractThymeleafVariables(String content) {
        Set<String> variables = new HashSet<>();
        if (content == null) {
            return variables;
        }

        Matcher matcher = THYMELEAF_PATTERN.matcher(content);
        while (matcher.find()) {
            variables.add(matcher.group(1).trim());
        }
        
        return variables;
    }

    /**
     * Validate that a template contains the required {{content}} variable
     */
    public static boolean hasContentVariable(String templateContent) {
        if (templateContent == null) {
            return false;
        }
        return templateContent.contains("{{content}}") || templateContent.contains("${content}");
    }

    /**
     * Get a list of standard newsletter template variables
     */
    public static List<String> getStandardTemplateVariables() {
        List<String> standardVars = new ArrayList<>();
        standardVars.add("content");          // Main newsletter content (REQUIRED)
        standardVars.add("title");           // Newsletter title
        standardVars.add("subject");         // Email subject
        standardVars.add("previewText");     // Email preview text
        standardVars.add("firstName");       // Recipient first name
        standardVars.add("lastName");        // Recipient last name
        standardVars.add("email");           // Recipient email
        standardVars.add("companyName");     // Recipient company
        standardVars.add("unsubscribeLink"); // Unsubscribe URL
        standardVars.add("webViewLink");     // Web view URL
        standardVars.add("currentDate");     // Current date
        standardVars.add("newsletterUuid");  // Newsletter ID
        return standardVars;
    }

    /**
     * Validate template content for common issues
     */
    public static List<String> validateTemplate(String templateContent) {
        List<String> issues = new ArrayList<>();
        
        if (templateContent == null || templateContent.trim().isEmpty()) {
            issues.add("Template content is empty");
            return issues;
        }
        
        // Check for required content variable
        if (!hasContentVariable(templateContent)) {
            issues.add("Template must contain {{content}} variable for newsletter content insertion");
        }
        
        // Check for unmatched braces
        long openBraces = templateContent.chars().filter(ch -> ch == '{').count();
        long closeBraces = templateContent.chars().filter(ch -> ch == '}').count();
        if (openBraces != closeBraces) {
            issues.add("Unmatched braces in template - check your variable syntax");
        }
        
        // Check for malformed variables
        Pattern malformedPattern = Pattern.compile("\\{[^{]|[^}]\\}");
        if (malformedPattern.matcher(templateContent).find()) {
            issues.add("Possible malformed variables - use {{variableName}} syntax");
        }
        
        return issues;
    }

    /**
     * Generate a variables string from template content for storage in database
     */
    public static String generateVariablesString(String templateContent) {
        Set<String> variables = extractVariables(templateContent);
        variables.addAll(extractThymeleafVariables(templateContent));
        
        // Remove the content variable as it's automatically handled
        variables.remove("content");
        variables.remove("textContent");
        
        return String.join(",", variables);
    }

    /**
     * Create a sample template with common structure
     */
    public static String createSampleTemplate(String templateName, String category) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <title>{{title}} - TheBridgeToAI.com Newsletter</title>
                <meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; margin: 0; padding: 0; }
                    .container { max-width: 600px; margin: 0 auto; background: #ffffff; }
                    .header { background: #0099B3; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; }
                    .footer { background: #f8f9fa; padding: 15px; text-align: center; font-size: 12px; color: #666; }
                    .footer a { color: #0099B3; text-decoration: none; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>%s Newsletter</h1>
                        <p>{{subject}}</p>
                    </div>
                    
                    <div class="content">
                        {{content}}
                    </div>
                    
                    <div class="footer">
                        <p>Hello {{firstName}},</p>
                        <p>Thanks for reading our newsletter!</p>
                        <p>
                            <a href="{{webViewLink}}">View in browser</a> | 
                            <a href="{{unsubscribeLink}}">Unsubscribe</a>
                        </p>
                        <p>&copy; 2025 TheBridgeToAI.com</p>
                    </div>
                </div>
            </body>
            </html>
            """, category.toLowerCase());
    }
}