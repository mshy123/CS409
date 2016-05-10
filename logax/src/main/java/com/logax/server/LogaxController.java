package com.logax.server;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.ui.ModelMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;

@Controller
public class LogaxController
{
	@RequestMapping(value = "/execute", method = RequestMethod.POST)
	@ResponseBody
	public String execute(@RequestBody String requestString)
	{
		JSONParser parser = new JSONParser();
		JSONObject json = null;
		try
		{
			json = (JSONObject)parser.parse(requestString);
		}
		catch(ParseException e)
		{
			return "{\"error\":\"bad request\"}";
		}

		ExecuteRequest request = new ExecuteRequest();
		
		try
		{
			request.parse(json);
		}
		catch(JsonTypeException e)
		{
			return "{\"error\":\"bad request\"}";
		}

		FileWriter writer = null;
		try	{
			writer = new FileWriter("/Users/hyunhoha/LocalCEP/example.txt", true);
			request.print(writer);
		}
		catch(IOException e)
		{
			return "{\"error\":\"Bad file out\"}";
		}
		return "{\"error\":\"null\"}";
	}
}
