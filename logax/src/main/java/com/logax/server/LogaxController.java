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
		/* add DB */
		if (request.addDBType() == -1)
		{
			return "{\"error\":\"Type name is already exist\"}";
		}

		FileWriter writer = null;
		try	{	
			/* Stop the core process */
			CoreController.stopCore();
			/* Edit fluentd File : type */
			writer = new FileWriter("/Users/hyunhoha/LocalCEP/DB.txt");
			JSONArray jarr = DBClient.getTypeList();
			for (int i = 0; i < jarr.size(); i++)
			{
				JSONObject job = (JSONObject)jarr.get(i);
				try {
					request.parse(job);
				}
				catch (JsonTypeException e)
				{
					return "{\"error\":\"bad request\"}";
				}
				request.print(writer);
			}
			/* Edit fluentd File : Add type tag */
			writer.write("<filter **>\n");
			writer.write("  @type record_transformer\n");
			writer.write("  enable_ruby true\n");
			writer.write("  renew_record true\n");
			writer.write("  <record>\n");
			writer.write("    content ${record.to_json}\n");
			writer.write("    type ${tag}\n");
			writer.write("  </record>\n</filter>\n\n");

			/* Edit fluentd send to kafka */
			writer.write("<match high.**>\n");
	      	writer.write("  @type               kafka\n");
	        writer.write("  brokers             127.0.0.1:9092\n");
		    writer.write("  default_topic       apache_error_topic\n");
		    writer.write("  output_include_time true\n</match>\n\n");
			writer.write("<match low.**>\n");
	      	writer.write("  @type               kafka\n");
	        writer.write("  brokers             127.0.0.1:9092\n");
		    writer.write("  default_topic       apache_error_topic\n");
		    writer.write("  output_include_time true\n</match>\n\n");
			writer.flush();
			writer.close();
			CoreController.startCore();
		}
		catch(IOException e)
		{
			return "{\"error\":\"Bad file out\"}";
		}
		
		return "{\"error\":\"null\"}";
	}

	@RequestMapping(value = "/deleteall", method = RequestMethod.POST)
	@ResponseBody
	public String deleteAllType(@RequestBody String requestString)
	{
		DBClient.removeAll();
		try
		{
			FileWriter writer = new FileWriter("/Users/hyunhoha/LocalCEP/DB.txt");
			writer.close();
		}
		catch (IOException e)
		{
			return "{\"error\":\"Bad file out\"}";
		}
		return "{\"error\":\"null\"}";
	}

	@RequestMapping(value = "/delete", method = RequestMethod.POST)
	@ResponseBody
	public String deleteType(@RequestBody String requestString)
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

		request.removeDBType();

		FileWriter writer = null;
		try	{
			/* Stop the core process */
			CoreController.stopCore();
			/* Edit fluentd File : type */
			writer = new FileWriter("/Users/hyunhoha/LocalCEP/DB.txt");
			JSONArray jarr = DBClient.getTypeList();
			for (int i = 0; i < jarr.size(); i++)
			{
				JSONObject job = (JSONObject)jarr.get(i);
				try {
					request.parse(job);
				}
				catch (JsonTypeException e)
				{
					return "{\"error\":\"bad request\"}";
				}
				request.print(writer);
			}
			/* Edit fluentd File : Add type tag */
			writer.write("<filter **>\n");
			writer.write("  @type record_transformer\n");
			writer.write("  enable_ruby true\n");
			writer.write("  renew_record true\n");
			writer.write("  <record>\n");
			writer.write("    content ${record.to_json}\n");
			writer.write("    type ${tag}\n");
			writer.write("  </record>\n</filter>\n\n");

			/* Edit fluentd send to kafka */
			writer.write("<match high.**>\n");
	      	writer.write("  @type               kafka\n");
	        writer.write("  brokers             127.0.0.1:9092\n");
		    writer.write("  default_topic       apache_error_topic\n");
		    writer.write("  output_include_time true\n</match>\n\n");
			writer.write("<match low.**>\n");
	      	writer.write("  @type               kafka\n");
	        writer.write("  brokers             127.0.0.1:9092\n");
		    writer.write("  default_topic       apache_error_topic\n");
		    writer.write("  output_include_time true\n</match>\n\n");
			writer.flush();
			writer.close();
			CoreController.startCore();
		}
		catch(IOException e)
		{
			return "{\"error\":\"Bad file out\"}";
		}
		return "{\"error\":\"null\"}";
	}
}
