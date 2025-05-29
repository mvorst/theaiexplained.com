package com.thebridgetoai.website.controller;

import java.util.Map;
import java.util.UUID;

import com.mattvorst.shared.controller.BaseRestController;
import com.mattvorst.shared.model.DynamoResultList;
import com.mattvorst.shared.util.CursorUtils;
import com.thebridgetoai.website.dao.model.Content;
import com.thebridgetoai.website.model.ViewContent;
import com.thebridgetoai.website.service.ContentService;
import org.apache.commons.lang3.stream.Streams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

@RestController
@RequestMapping("/rest/api/{version}/content")
public class ContentController extends BaseRestController {

	@Autowired private ContentService contentService;

	@GetMapping("/{contentUuid}")
	public ResponseEntity<ViewContent> getContent(@PathVariable UUID contentUuid) {
		Content content = contentService.getContent(contentUuid);
		if (content == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(new ViewContent(content));
	}

	@GetMapping("/")
	public ResponseEntity<DynamoResultList<ViewContent>> getAllContent(@RequestParam(required = false) String cursor, @RequestParam(required = false, defaultValue = "10") int count) {
		Map<String, AttributeValue> attributeValueMap = CursorUtils.decodeLastEvaluatedKeyFromCursor(cursor);

		DynamoResultList<Content> dynamoResultList = contentService.getAllContent(count, attributeValueMap);

		return ResponseEntity.ok(new DynamoResultList<>(
				Streams.of(dynamoResultList.getList()).map(ViewContent::new).toList(),
				dynamoResultList.getLastEvaluatedKey()));
	}
}