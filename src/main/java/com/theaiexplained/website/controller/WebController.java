package com.theaiexplained.website.controller;

import java.util.Map;

import com.mattvorst.shared.model.DynamoResultList;
import com.mattvorst.shared.util.CursorUtils;
import com.theaiexplained.website.dao.model.Content;
import com.theaiexplained.website.service.ContentService;
import org.springframework.beans.factory.annotation.Autowired;
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

	/* GET: /error/ -> /error.action */
	@RequestMapping(value="/error.action", method= RequestMethod.GET)
	public ModelAndView error(){
		ModelAndView modelAndView = new ModelAndView("error");

		return modelAndView;
	}

	/* GET: / -> /index.action */
	@RequestMapping(value="/index.action", method= RequestMethod.GET)
	public ModelAndView index(){
		ModelAndView modelAndView = new ModelAndView("index");

		return modelAndView;
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

		DynamoResultList<Content> contentList = contentService.getContentListByDate(count, attributeValueMap);

		return new ModelAndView("blog", Map.of("contentList", contentList));
	}
}
