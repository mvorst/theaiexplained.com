package com.theaiexplained.website.controller;

import java.util.Map;
import java.util.UUID;

import com.mattvorst.shared.constant.Status;
import com.mattvorst.shared.model.DynamoResultList;
import com.mattvorst.shared.util.CursorUtils;
import com.mattvorst.shared.util.Streams;
import com.theaiexplained.website.constant.ContentCategoryType;
import com.theaiexplained.website.dao.model.Content;
import com.theaiexplained.website.dao.model.Newsletter;
import com.theaiexplained.website.model.ViewContent;
import com.theaiexplained.website.model.ViewHomeContent;
import com.theaiexplained.website.service.ContentService;
import com.theaiexplained.website.service.NewsletterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

@RestController
public class WebController {

	@Autowired private ContentService contentService;
	@Autowired private NewsletterService newsletterService;

	/* GET: /error/ -> /error.action */
	@RequestMapping(value="/error.action", method= RequestMethod.GET)
	public ModelAndView error(){
		ModelAndView modelAndView = new ModelAndView("error");

		return modelAndView;
	}

	/* GET: / -> /index.action */
	@RequestMapping(value="/index.action", method= RequestMethod.GET)
	public ModelAndView index(){
		ViewHomeContent viewHomeContent = contentService.getHomeContent();

		return new ModelAndView("index", Map.of("homeContent", viewHomeContent));
	}

	/* GET: / -> /index.action */
	@RequestMapping(value="/start-here.action", method= RequestMethod.GET)
	public ModelAndView startHere(){
//		ViewHomeContent viewHomeContent = contentService.getHomeContent();

//		return new ModelAndView("start-here", Map.of("homeContent", viewHomeContent));
		return new ModelAndView("start-here");
	}

	/* GET: /login/ -> /login.action */
	@RequestMapping(value="/login.action", method= RequestMethod.GET)
	public ModelAndView login(){
		ModelAndView modelAndView = new ModelAndView("login");

		return modelAndView;
	}

	/* GET: /blog/ -> /blog.action */
	@RequestMapping(value="/blog.action", method= RequestMethod.GET)
	public ModelAndView blog(@RequestParam(required = false, defaultValue = "10") int count, @RequestParam(required = false, defaultValue = "") String cursor){

		Map<String, AttributeValue> attributeValueMap = CursorUtils.decodeLastEvaluatedKeyFromCursor(cursor);

		DynamoResultList<Content> contentList = contentService.getContentListByDate(ContentCategoryType.BLOG_POST, count, attributeValueMap);

		DynamoResultList<ViewContent> dynamoResultList = new DynamoResultList<>(Streams.of(contentList.getList()).map(content -> new ViewContent(content)).toList(), contentList.getLastEvaluatedKey());

		return new ModelAndView("blog", Map.of("contentList", dynamoResultList));
	}

	/* GET: /blog/{uuid}/* -> /blog-detail.action */
	@RequestMapping(value="/blog-detail.action", method= RequestMethod.GET)
	public ModelAndView blogDetail(@RequestParam(required = true) UUID contentUuid){

		Content content = contentService.getContent(contentUuid);

		return new ModelAndView("blog-detail", Map.of("content", new ViewContent(content)));
	}

	/* GET: /newsletter/ -> /newsletter.action */
	@RequestMapping(value="/newsletter.action", method= RequestMethod.GET)
	public ModelAndView newsletter(){
		// Get the latest sent newsletter (ARCHIVED status)
		DynamoResultList<Newsletter> sentNewsletters = newsletterService.getNewsletterListByStatusAndCreatedDate(Status.ARCHIVED, 1, null);
		
		Newsletter latestNewsletter = null;
		if (sentNewsletters != null && sentNewsletters.getList() != null && !sentNewsletters.getList().isEmpty()) {
			latestNewsletter = sentNewsletters.getList().get(0);
		}

		if(latestNewsletter != null) {
			return new ModelAndView("newsletter", Map.of("newsletter", latestNewsletter));
		}else {
			return new ModelAndView("newsletter");
		}
	}

	/* POST: /rest/api/1/subscribe */
	@PostMapping("/rest/api/1/subscribe")
	public ResponseEntity<Map<String, String>> subscribe(@RequestBody SubscriptionRequest request) {
		// For now, just return success - in a real implementation you would:
		// 1. Validate the email
		// 2. Store in database
		// 3. Send confirmation email
		// 4. Add to mailing list service
		
		if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
			return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
		}
		
		// Simulate successful subscription
		return ResponseEntity.ok(Map.of(
			"message", "Thank you for subscribing!",
			"email", request.getEmail()
		));
	}

	// Simple subscription request model
	public static class SubscriptionRequest {
		private String email;
		private String firstName;

		public String getEmail() {
			return email;
		}

		public void setEmail(String email) {
			this.email = email;
		}

		public String getFirstName() {
			return firstName;
		}

		public void setFirstName(String firstName) {
			this.firstName = firstName;
		}
	}
}
