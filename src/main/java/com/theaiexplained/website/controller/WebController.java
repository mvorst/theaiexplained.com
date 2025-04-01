package com.theaiexplained.website.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
public class WebController {

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

}
