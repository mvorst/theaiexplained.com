package com.theaiexplained.website.controller;

import java.util.Map;
import java.util.UUID;

import com.mattvorst.shared.constant.Status;
import com.mattvorst.shared.controller.BaseRestController;
import com.mattvorst.shared.exception.ValidationException;
import com.mattvorst.shared.model.DynamoResultList;
import com.mattvorst.shared.util.CursorUtils;
import com.mattvorst.shared.util.Streams;
import com.theaiexplained.website.constant.TemplateCategory;
import com.theaiexplained.website.dao.model.Newsletter;
import com.theaiexplained.website.dao.model.NewsletterTemplate;
import com.theaiexplained.website.model.ViewNewsletter;
import com.theaiexplained.website.model.ViewNewsletterTemplate;
import com.theaiexplained.website.service.NewsletterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

@RestController
@RequestMapping("/rest/api/{version}/newsletter")
public class NewsletterController extends BaseRestController {

    @Autowired private NewsletterService newsletterService;

    @GetMapping("/{newsletterUuid}")
    public ResponseEntity<ViewNewsletter> getNewsletter(@PathVariable UUID newsletterUuid) {
        Newsletter newsletter = newsletterService.getNewsletter(newsletterUuid);
        if (newsletter == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new ViewNewsletter(newsletter));
    }

    @GetMapping("/")
    public ResponseEntity<DynamoResultList<ViewNewsletter>> getAllNewsletters(
            @RequestParam(required = false) String cursor, 
            @RequestParam(required = false, defaultValue = "10") int count) {
        
        Map<String, AttributeValue> attributeValueMap = CursorUtils.decodeLastEvaluatedKeyFromCursor(cursor);
        DynamoResultList<Newsletter> dynamoResultList = newsletterService.getNewsletterListByCreatedDate(count, attributeValueMap);

        return ResponseEntity.ok(new DynamoResultList<>(
                Streams.of(dynamoResultList.getList()).map(ViewNewsletter::new).toList(),
                dynamoResultList.getLastEvaluatedKey()));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<DynamoResultList<ViewNewsletter>> getNewsletterListByStatusAndCreatedDate(
            @PathVariable Status status,
            @RequestParam(required = false) String cursor, 
            @RequestParam(required = false, defaultValue = "10") int count) {
        
        Map<String, AttributeValue> attributeValueMap = CursorUtils.decodeLastEvaluatedKeyFromCursor(cursor);
        DynamoResultList<Newsletter> dynamoResultList = newsletterService.getNewsletterListByStatusAndCreatedDate(status, count, attributeValueMap);

        return ResponseEntity.ok(new DynamoResultList<>(
                Streams.of(dynamoResultList.getList()).map(ViewNewsletter::new).toList(),
                dynamoResultList.getLastEvaluatedKey()));
    }

    @PostMapping("/")
    public ResponseEntity<?> createNewsletter(@RequestBody ViewNewsletter viewNewsletter) throws ValidationException {
        Newsletter newsletter = newsletterService.createNewsletter(viewNewsletter);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ViewNewsletter(newsletter));
    }

    @PutMapping("/{newsletterUuid}")
    public ResponseEntity<?> updateNewsletter(@PathVariable UUID newsletterUuid, @RequestBody ViewNewsletter viewNewsletter) throws ValidationException {
        Newsletter newsletter = newsletterService.updateNewsletter(newsletterUuid, viewNewsletter);
        if (newsletter == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new ViewNewsletter(newsletter));
    }

    @DeleteMapping("/{newsletterUuid}")
    public ResponseEntity<?> deleteNewsletter(@PathVariable UUID newsletterUuid) {
        Newsletter newsletter = newsletterService.deleteNewsletter(newsletterUuid);
        if (newsletter == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new ViewNewsletter(newsletter));
    }

    @PostMapping("/{newsletterUuid}/schedule")
    public ResponseEntity<?> scheduleNewsletter(@PathVariable UUID newsletterUuid) throws ValidationException {
        Newsletter newsletter = newsletterService.scheduleNewsletter(newsletterUuid);
        return ResponseEntity.ok(new ViewNewsletter(newsletter));
    }

    @PostMapping("/{newsletterUuid}/send")
    public ResponseEntity<?> sendNewsletter(@PathVariable UUID newsletterUuid) throws ValidationException {
        Newsletter newsletter = newsletterService.sendNewsletter(newsletterUuid);
        return ResponseEntity.ok(new ViewNewsletter(newsletter));
    }

    @PostMapping("/{newsletterUuid}/duplicate")
    public ResponseEntity<?> duplicateNewsletter(@PathVariable UUID newsletterUuid) throws ValidationException {
        Newsletter originalNewsletter = newsletterService.getNewsletter(newsletterUuid);
        if (originalNewsletter == null) {
            return ResponseEntity.notFound().build();
        }

        ViewNewsletter duplicateViewNewsletter = new ViewNewsletter(originalNewsletter);
        // Clear fields that shouldn't be duplicated
        duplicateViewNewsletter.setNewsletterUuid(null);
        duplicateViewNewsletter.setTitle(duplicateViewNewsletter.getTitle() + " (Copy)");
        duplicateViewNewsletter.setStatus(Status.INACTIVE);
        duplicateViewNewsletter.setScheduledDate(null);
        duplicateViewNewsletter.setSentDate(null);
        duplicateViewNewsletter.setTotalRecipients(null);
        duplicateViewNewsletter.setDeliveredCount(null);
        duplicateViewNewsletter.setOpenedCount(null);
        duplicateViewNewsletter.setClickedCount(null);
        duplicateViewNewsletter.setBouncedCount(null);

        Newsletter duplicatedNewsletter = newsletterService.createNewsletter(duplicateViewNewsletter);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ViewNewsletter(duplicatedNewsletter));
    }

    // Template Endpoints

    @GetMapping("/template/{templateUuid}")
    public ResponseEntity<ViewNewsletterTemplate> getTemplate(@PathVariable UUID templateUuid) {
        NewsletterTemplate template = newsletterService.getTemplate(templateUuid);
        if (template == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new ViewNewsletterTemplate(template));
    }

    @GetMapping("/template/")
    public ResponseEntity<DynamoResultList<ViewNewsletterTemplate>> getAllTemplates(
            @RequestParam(required = false) String cursor, 
            @RequestParam(required = false, defaultValue = "10") int count) {
        
        Map<String, AttributeValue> attributeValueMap = CursorUtils.decodeLastEvaluatedKeyFromCursor(cursor);
        DynamoResultList<NewsletterTemplate> dynamoResultList = newsletterService.getTemplateListByCreatedDate(count, attributeValueMap);

        return ResponseEntity.ok(new DynamoResultList<>(
                Streams.of(dynamoResultList.getList()).map(ViewNewsletterTemplate::new).toList(),
                dynamoResultList.getLastEvaluatedKey()));
    }

    @GetMapping("/template/category/{category}")
    public ResponseEntity<DynamoResultList<ViewNewsletterTemplate>> getTemplateListByCategoryAndCreatedDate(
            @PathVariable TemplateCategory category,
            @RequestParam(required = false) String cursor, 
            @RequestParam(required = false, defaultValue = "10") int count) {
        
        Map<String, AttributeValue> attributeValueMap = CursorUtils.decodeLastEvaluatedKeyFromCursor(cursor);
        DynamoResultList<NewsletterTemplate> dynamoResultList = newsletterService.getTemplateListByCategoryAndCreatedDate(category, count, attributeValueMap);

        return ResponseEntity.ok(new DynamoResultList<>(
                Streams.of(dynamoResultList.getList()).map(ViewNewsletterTemplate::new).toList(),
                dynamoResultList.getLastEvaluatedKey()));
    }

    @PostMapping("/template/")
    public ResponseEntity<?> createTemplate(@RequestBody ViewNewsletterTemplate viewTemplate) throws ValidationException {
        NewsletterTemplate template = newsletterService.createTemplate(viewTemplate);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ViewNewsletterTemplate(template));
    }

    @PutMapping("/template/{templateUuid}")
    public ResponseEntity<?> updateTemplate(@PathVariable UUID templateUuid, @RequestBody ViewNewsletterTemplate viewTemplate) throws ValidationException {
        NewsletterTemplate template = newsletterService.updateTemplate(templateUuid, viewTemplate);
        if (template == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new ViewNewsletterTemplate(template));
    }

    @DeleteMapping("/template/{templateUuid}")
    public ResponseEntity<?> deleteTemplate(@PathVariable UUID templateUuid) {
        NewsletterTemplate template = newsletterService.deleteTemplate(templateUuid);
        if (template == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new ViewNewsletterTemplate(template));
    }

    @PostMapping("/template/{templateUuid}/duplicate")
    public ResponseEntity<?> duplicateTemplate(@PathVariable UUID templateUuid) throws ValidationException {
        NewsletterTemplate duplicatedTemplate = newsletterService.duplicateTemplate(templateUuid);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ViewNewsletterTemplate(duplicatedTemplate));
    }
}