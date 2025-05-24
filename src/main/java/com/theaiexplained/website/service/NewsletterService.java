package com.theaiexplained.website.service;

import java.util.Map;
import java.util.UUID;

import com.mattvorst.shared.constant.Status;
import com.mattvorst.shared.exception.ValidationException;
import com.mattvorst.shared.model.DynamoResultList;
import com.mattvorst.shared.util.FieldValidator;
import com.theaiexplained.website.dao.NewsletterDao;
import com.theaiexplained.website.dao.model.Newsletter;
import com.theaiexplained.website.model.ViewNewsletter;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

@Service
public class NewsletterService {

    @Autowired private MessageSource messageSource;
    @Autowired private NewsletterDao newsletterDao;

    public Newsletter getNewsletter(UUID newsletterUuid) {
        return newsletterDao.getNewsletter(newsletterUuid).join();
    }

    public Newsletter createNewsletter(ViewNewsletter viewNewsletter) throws ValidationException {
        validateNewsletter(viewNewsletter);

        // Generate UUID for new newsletter
        int count = 0;
        do {
            UUID newsletterUuid = UUID.randomUUID();
            Newsletter existingNewsletter = newsletterDao.getNewsletter(newsletterUuid).join();
            if (existingNewsletter == null) {
                viewNewsletter.setNewsletterUuid(newsletterUuid);
                break;
            }
        } while (count++ < 10);

        Newsletter newsletter = new Newsletter();
        BeanUtils.copyProperties(viewNewsletter, newsletter);

        // Set default status if not provided
        if (newsletter.getStatus() == null) {
            newsletter.setStatus(Status.INACTIVE); // Draft status
        }

        newsletterDao.saveNewsletter(newsletter).join();

        return newsletter;
    }

    public Newsletter updateNewsletter(UUID newsletterUuid, ViewNewsletter viewNewsletter) throws ValidationException {
        validateNewsletter(viewNewsletter);

        Newsletter newsletter = getNewsletter(newsletterUuid);
        if (newsletter == null) {
            return null;
        }

        // Update fields
        BeanUtils.copyProperties(viewNewsletter, newsletter, "newsletterUuid", "createdDate", "createdBySubject");

        // Save changes
        newsletterDao.saveNewsletter(newsletter).join();

        return newsletter;
    }

    public Newsletter deleteNewsletter(UUID newsletterUuid) {
        Newsletter newsletter = getNewsletter(newsletterUuid);
        if (newsletter != null) {
            return newsletterDao.deleteNewsletter(newsletter).join();
        }
        return newsletter;
    }

    public DynamoResultList<Newsletter> getAllNewsletters(int count, Map<String, AttributeValue> attributeValueMap) {
        return newsletterDao.getAllNewsletterList(count, attributeValueMap).join();
    }


    public DynamoResultList<Newsletter> getNewsletterListByCreatedDate(int count, Map<String, AttributeValue> attributeValueMap) {
        return newsletterDao.getNewsletterListByCreatedDate(count, attributeValueMap).join();
    }

    public DynamoResultList<Newsletter> getNewsletterListByStatusAndCreatedDate(Status status, int count, Map<String, AttributeValue> attributeValueMap) {
        return newsletterDao.getNewsletterListByStatusAndCreatedDate(status, count, attributeValueMap).join();
    }

    public Newsletter scheduleNewsletter(UUID newsletterUuid) throws ValidationException {
        Newsletter newsletter = getNewsletter(newsletterUuid);
        if (newsletter == null) {
            throw new ValidationException("Newsletter not found");
        }

        // Validate newsletter is ready to be scheduled
        validateNewsletterForScheduling(newsletter);

        newsletter.setStatus(Status.ACTIVE); // Scheduled status
        newsletterDao.saveNewsletter(newsletter).join();

        return newsletter;
    }

    public Newsletter sendNewsletter(UUID newsletterUuid) throws ValidationException {
        Newsletter newsletter = getNewsletter(newsletterUuid);
        if (newsletter == null) {
            throw new ValidationException("Newsletter not found");
        }

        // Validate newsletter is ready to be sent
        validateNewsletterForSending(newsletter);

        newsletter.setStatus(Status.ARCHIVED); // Sent status
        newsletter.setSentDate(new java.util.Date());
        newsletterDao.saveNewsletter(newsletter).join();

        // TODO: Implement actual email sending logic here
        // This would integrate with SES or another email service

        return newsletter;
    }

    private void validateNewsletter(ViewNewsletter viewNewsletter) throws ValidationException {
        FieldValidator.get(messageSource, LocaleContextHolder.getLocale())
                .validateNotEmpty("title", viewNewsletter.getTitle())
                .validateNotEmpty("subject", viewNewsletter.getSubject())
                .apply();
    }

    private void validateNewsletterForScheduling(Newsletter newsletter) throws ValidationException {
        FieldValidator validator = FieldValidator.get(messageSource, LocaleContextHolder.getLocale());

        validator.validateNotEmpty("title", newsletter.getTitle())
                .validateNotEmpty("subject", newsletter.getSubject())
                .validateNotEmpty("htmlContent", newsletter.getHtmlContent());

        if (newsletter.getScheduledDate() == null) {
            validator.validateNotEmpty("scheduledDate", "scheduledDate");
        }

        validator.apply();
    }

    private void validateNewsletterForSending(Newsletter newsletter) throws ValidationException {
        FieldValidator validator = FieldValidator.get(messageSource, LocaleContextHolder.getLocale());

        validator.validateNotEmpty("title", newsletter.getTitle())
                .validateNotEmpty("subject", newsletter.getSubject())
                .validateNotEmpty("htmlContent", newsletter.getHtmlContent());

        if (!Status.ACTIVE.equals(newsletter.getStatus())) {
            validator.validateNotEmpty("status", "status");
        }

        validator.apply();
    }
}