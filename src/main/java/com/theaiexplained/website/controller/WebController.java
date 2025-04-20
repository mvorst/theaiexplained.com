package com.theaiexplained.website.controller;

import com.theaiexplained.website.service.ContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
public class WebController {

	@Autowired private ContentService contentService;

	@RequestMapping(value="/error.action", method= RequestMethod.GET)
	public ModelAndView error(){
		ModelAndView modelAndView = new ModelAndView("error");

		return modelAndView;
	}

	@RequestMapping(value="/index.action", method= RequestMethod.GET)
	public ModelAndView index(){
		ModelAndView modelAndView = new ModelAndView("index");

		return modelAndView;
	}

	@RequestMapping(value="/blog.action", method= RequestMethod.GET)
	public ModelAndView blog(@RequestAttribute(required = false, value = "10") int count, @RequestAttribute(required = false, value = "") String cursor){

//		contentService.getAllContent();


		return new ModelAndView("blog");
	}
}
