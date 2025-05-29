package com.thebridgetoai.website.controller;

import java.util.Map;
import java.util.UUID;

import com.mattvorst.shared.constant.EnvironmentConstants;
import com.mattvorst.shared.exception.ValidationException;
import com.mattvorst.shared.model.DynamoResultList;
import com.mattvorst.shared.model.file.S3UploadComplete;
import com.mattvorst.shared.model.file.S3UploadUrl;
import com.mattvorst.shared.security.AuthorizationUtils;
import com.mattvorst.shared.security.token.UserToken;
import com.mattvorst.shared.service.FileService;
import com.mattvorst.shared.util.CursorUtils;
import com.mattvorst.shared.util.Environment;
import com.mattvorst.shared.util.Utils;
import com.thebridgetoai.website.constant.ContentCategoryType;
import com.thebridgetoai.website.dao.model.Content;
import com.thebridgetoai.website.model.ViewContent;
import com.thebridgetoai.website.service.AdminService;
import org.apache.commons.lang3.stream.Streams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

@RestController
public class AdminController {

	@Autowired private AdminService adminService;
	@Autowired private FileService fileService;

	@RequestMapping(value="/admin.action", method= RequestMethod.GET)
	public ModelAndView error(){
		ModelAndView modelAndView = new ModelAndView("admin");

		return modelAndView;
	}

	@GetMapping("/rest/admin/{version}/content/")
	public ResponseEntity<DynamoResultList<ViewContent>> getContentListByDate(@RequestParam(required = true) ContentCategoryType contentCategoryType, @RequestParam(required = false) String cursor, @RequestParam(required = false, defaultValue = "10") int count) {
		Map<String, AttributeValue> attributeValueMap = CursorUtils.decodeLastEvaluatedKeyFromCursor(cursor);

		DynamoResultList<Content> dynamoResultList = adminService.getContentListByDate(contentCategoryType, count, attributeValueMap);

		return ResponseEntity.ok(new DynamoResultList<>(
				Streams.of(dynamoResultList.getList()).map(ViewContent::new).toList(),
				dynamoResultList.getLastEvaluatedKey()));
	}


	@GetMapping("/rest/admin/{version}/content/{contentUuid}")
	public ResponseEntity<ViewContent> getContent(@PathVariable UUID contentUuid) {
		Content content = adminService.getContent(contentUuid);

		return ResponseEntity.ok(new ViewContent(content));
	}

	@PostMapping("/rest/admin/{version}/content/")
	public ResponseEntity<ViewContent> createContent(@RequestBody ViewContent viewContent) throws ValidationException {

		Content content = adminService.createContent(viewContent);

		return ResponseEntity.ok(new ViewContent(content));
	}

	@PutMapping("/rest/admin/{version}/content/{contentUuid}")
	public ResponseEntity<ViewContent> updateContent(@PathVariable UUID contentUuid, @RequestBody ViewContent viewContent) throws ValidationException {

		Content content = adminService.updateContent(contentUuid, viewContent);

		return ResponseEntity.ok(new ViewContent(content));
	}

	@GetMapping("/rest/admin/{version}/s3/upload/url")
	public S3UploadUrl getSignedUrl() {
		return fileService.getPreSignedUrl(Environment.get(EnvironmentConstants.AWS_S3_BUCKET_TEMP_FILE_DATA), UUID.randomUUID().toString());
	}

	@PostMapping("/rest/admin/{version}/s3/upload/complete/")
	public S3UploadComplete uploadUserProfileImage(@RequestBody S3UploadComplete s3UploadComplete) {

		UserToken userToken = AuthorizationUtils.getUserToken();

		s3UploadComplete = fileService.uploadImageComplete(userToken.getUserUuid(), s3UploadComplete);
		if(s3UploadComplete != null){
			if(!Utils.empty(s3UploadComplete.getS3Bucket()) && !Utils.empty(s3UploadComplete.getS3Key())) {
				s3UploadComplete.setDownloadUrl(fileService.getSignedUrl(s3UploadComplete.getS3Bucket(), s3UploadComplete.getS3Key()).getUrl());
			}
		}

		return s3UploadComplete;
	}

	@GetMapping("/rest/admin/{version}/s3/file/{fileUuid}/download")
	public S3UploadUrl getSignedUrl(@PathVariable UUID fileUuid) {
		return fileService.getSignedUrl(fileUuid);
	}
}
