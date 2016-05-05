package com.logax.server;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.ui.ModelMap;

@Controller
public class HelloUser
{
	@RequestMapping(value = "/greeting/{userid}", method = RequestMethod.GET)
	public ModelAndView user(@PathVariable(value="userid") String id)
	{
		ModelAndView model = new ModelAndView();
		model.setViewName("user");
		model.addObject("greeting", "Hello User " + id);
		return model;
	}
}
