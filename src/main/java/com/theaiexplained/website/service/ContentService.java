package com.theaiexplained.website.service;

import java.util.Map;
import java.util.UUID;

import com.mattvorst.shared.exception.ValidationException;
import com.mattvorst.shared.model.DynamoResultList;
import com.mattvorst.shared.util.FieldValidator;
import com.theaiexplained.website.dao.ContentDao;
import com.theaiexplained.website.dao.model.Content;
import com.theaiexplained.website.model.ViewContent;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

@Service
public class ContentService {

	@Autowired private MessageSource messageSource;
	@Autowired private ContentDao contentDao;

	public Content getContent(UUID contentUuid) {
		return contentDao.getContent(contentUuid).join();
	}

	public Content createContent(ViewContent viewContent) throws ValidationException {
		validateContent(viewContent);

		// Create a new Content entity
		int count = 0;
		do {
			UUID contentUuid = UUID.randomUUID();
			Content existingContent = contentDao.getContent(contentUuid).join();
			if (existingContent == null) {
				viewContent.setContentUuid(contentUuid);
				break;
			}
		} while (count++ < 10);

		Content content = new Content();
		BeanUtils.copyProperties(viewContent, content);

		contentDao.saveContent(content).join();

		return content;
	}

	public Content updateContent(UUID contentUuid, ViewContent viewContent) throws ValidationException {
		validateContent(viewContent);

		// Get existing content
		Content content = getContent(contentUuid);
		if (content == null) {
			return null;
		}

		// Update fields
		BeanUtils.copyProperties(viewContent, content, "contentUuid", "createdDate", "createdBySubject");

		// Save changes
		contentDao.saveContent(content).join();

		return content;
	}

	public Content deleteContent(UUID contentUuid) {
		Content content = getContent(contentUuid);
		if (content != null) {
			contentDao.deleteContent(content).join();
		}
		return content;
	}

	public DynamoResultList<Content> getAllContent(int count, Map<String, AttributeValue> attributeValueMap) {
		return contentDao.getAllContent(count, attributeValueMap).join();
	}

	public DynamoResultList<Content> getContentListByDate(int count, Map<String, AttributeValue> attributeValueMap) {
		return contentDao.getAllContent(count, attributeValueMap).join();
	}

	private void validateContent(ViewContent viewContent) throws ValidationException {
		FieldValidator.get(messageSource, LocaleContextHolder.getLocale())
				.validateNotEmpty("cardTitle", viewContent.getCardTitle())
				.validateNotEmpty("title", viewContent.getTitle())
				.apply();
	}
}