# Newsletter Template System

## Overview

The newsletter template system provides a wrapper-based approach where templates serve as reusable layouts that contain placeholder variables for dynamic content insertion. The primary use case is to maintain consistent branding and formatting across all newsletters while allowing unique content for each newsletter.

## Template Structure

### Core Concept
- **Templates** = Reusable wrappers/layouts with consistent branding, headers, footers, styling
- **Newsletter Content** = Unique content for each newsletter issue
- **Variable Substitution** = Templates contain placeholder variables that get replaced with actual content

### Primary Variable: `{{content}}`
The most important template variable is `{{content}}`, which represents the main newsletter content area where the unique content for each newsletter will be inserted.

## Template Design Pattern

### Example Template Structure:
```html
<!DOCTYPE html>
<html>
<head>
    <title>{{title}} - TheBridgeToAI.com Newsletter</title>
    <style>
        /* Consistent branding styles */
        .header { background: #0099B3; color: white; padding: 20px; }
        .content { padding: 20px; max-width: 600px; margin: 0 auto; }
        .footer { background: #f8f9fa; padding: 15px; text-align: center; }
    </style>
</head>
<body>
    <div class="header">
        <img src="/img/LogoHLight.svg" alt="TheBridgeToAI.com" />
        <h1>Weekly AI Newsletter</h1>
    </div>
    
    <div class="content">
        {{content}}
    </div>
    
    <div class="footer">
        <p>Thanks for reading! {{unsubscribeLink}}</p>
        <p>&copy; 2025 TheBridgeToAI.com</p>
    </div>
</body>
</html>
```

### Newsletter Content Example:
```html
<h2>This Week in AI</h2>
<p>Welcome to this week's edition of our AI newsletter...</p>

<h3>Top Stories</h3>
<ul>
    <li>GPT-5 Announcement Details</li>
    <li>New Robotics Breakthrough</li>
    <li>AI Ethics Guidelines Updated</li>
</ul>

<h3>Featured Article</h3>
<p>Deep dive into the latest machine learning trends...</p>
```

## Variable System

### Standard Template Variables:
- `{{content}}` - Main newsletter content (REQUIRED)
- `{{title}}` - Newsletter title/subject
- `{{unsubscribeLink}}` - Unsubscribe URL
- `{{firstName}}` - Subscriber's first name
- `{{lastName}}` - Subscriber's last name
- `{{date}}` - Publication date
- `{{issueNumber}}` - Newsletter issue number

### Custom Variables:
Templates can define additional variables in the `variables` field as a comma-separated list:
```
firstName,lastName,companyName,unsubscribeLink,date,issueNumber
```

## Implementation Workflow

### 1. Template Creation Process:
1. Designer creates template with consistent branding
2. Template includes `{{content}}` placeholder where main content goes
3. Template saved with category (NEWSLETTER, MARKETING, etc.)
4. Template can include other variables for personalization

### 2. Newsletter Creation Process:
1. Editor selects a template from dropdown
2. Template provides the wrapper/layout structure
3. Editor writes unique content in the newsletter content editor
4. At send time: template's `{{content}}` variable is replaced with newsletter content
5. Other variables ({{firstName}}, etc.) are replaced with recipient data

### 3. Email Generation Process:
```
Template HTML + Newsletter Content + Recipient Data = Final Email
```

**Example Flow:**
```
Template: "<div class="header">Newsletter</div>{{content}}<div class="footer">Unsubscribe</div>"
Newsletter Content: "<h1>Weekly Update</h1><p>This week's news...</p>"
Final Email: "<div class="header">Newsletter</div><h1>Weekly Update</h1><p>This week's news...</p><div class="footer">Unsubscribe</div>"
```

## Database Schema Impact

### NewsletterTemplate Table:
- `templateUuid` - Primary key
- `name` - Template display name
- `htmlContent` - Template HTML with {{variables}}
- `textContent` - Plain text version with {{variables}}
- `variables` - Comma-separated list of supported variables
- `category` - Template category (NEWSLETTER, MARKETING, etc.)

### Newsletter Table:
- `templateId` - Reference to selected template UUID
- `htmlContent` - The unique content for this newsletter (goes into {{content}})
- `textContent` - Plain text version of unique content

## Backend Processing

### Template Variable Replacement Service:
```java
public String processTemplate(NewsletterTemplate template, Newsletter newsletter, Map<String, String> recipientData) {
    String finalHtml = template.getHtmlContent();
    
    // Replace main content
    finalHtml = finalHtml.replace("{{content}}", newsletter.getHtmlContent());
    
    // Replace recipient-specific variables
    for (Map.Entry<String, String> entry : recipientData.entrySet()) {
        finalHtml = finalHtml.replace("{{" + entry.getKey() + "}}", entry.getValue());
    }
    
    return finalHtml;
}
```

## Template Categories

### NEWSLETTER
- Weekly/monthly publication templates
- Consistent branding for regular communications
- Includes standard newsletter sections (header, content, footer)

### MARKETING
- Promotional campaign templates
- Product announcements
- Special offers and sales

### ANNOUNCEMENT
- Company news and updates
- Product launches
- Important notifications

### WELCOME
- New subscriber onboarding
- Welcome series templates
- Introduction to services

## Best Practices

### Template Design:
1. **Always include {{content}} variable** - This is where newsletter content goes
2. **Maintain consistent branding** across all templates
3. **Mobile-responsive design** for email clients
4. **Include unsubscribe mechanism** in footer
5. **Test across email clients** before publishing

### Newsletter Content Creation:
1. **Focus on unique content** - template handles layout/branding
2. **Write content assuming it will be wrapped** by template
3. **Don't duplicate header/footer content** - template provides this
4. **Use semantic HTML** for better email client compatibility

### Variable Management:
1. **Document all template variables** in the variables field
2. **Use descriptive variable names** ({{articleTitle}} vs {{title}})
3. **Provide fallback content** for optional variables
4. **Test variable replacement** before sending

## Future Enhancements

### Planned Features:
- **Visual template editor** with drag-and-drop components
- **Template preview** with sample content
- **Variable validation** to ensure all required variables are provided
- **Template versioning** to track changes over time
- **A/B testing** for different template variations
- **Template analytics** to track performance metrics

### Integration Points:
- **CRM integration** for recipient personalization data
- **Asset management** for template images and resources
- **Email service provider** APIs for delivery
- **Analytics platforms** for tracking open/click rates

## Technical Notes

### Email Client Compatibility:
- Templates must be compatible with major email clients (Gmail, Outlook, Apple Mail)
- Use table-based layouts for better compatibility
- Inline CSS preferred over external stylesheets
- Test with Email on Acid or Litmus

### Security Considerations:
- **Sanitize template content** to prevent XSS attacks
- **Validate variable names** to prevent injection
- **Escape user content** before variable replacement
- **Review templates** before approval for use

### Performance:
- **Cache compiled templates** to avoid repeated processing
- **Optimize images** used in templates
- **Minimize template size** for faster loading
- **Use CDN** for template assets when possible