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
	@RequestMapping(value = "/execute", method = RequestMethod.POST, produces="application/json;charset=UTF-8")
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
			JSONObject returnjson = new JSONObject();
			returnjson.put("success", false);
			return returnjson.toJSONString();
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
		
		JSONObject returnjson = new JSONObject();
		returnjson.put("success", true);
		return returnjson.toJSONString();
	}

	@RequestMapping(value = "/deletealltype", method = RequestMethod.GET, produces="application/json;charset=UTF-8")
	@ResponseBody
	public String deleteAllType()
	{
		if (DBClient.isAllTypeConnect() == -1) {
			JSONObject returnjson = new JSONObject();
			returnjson.put("success", false);
			return returnjson.toJSONString();
		}
		
		DBClient.removeAllType();
		
		try
		{
			CoreController.stopCore();
			FileWriter writer = new FileWriter("/Users/hyunhoha/LocalCEP/DB.txt");
			writer.close();
			CoreController.startCore();
		}
		catch (IOException e)
		{
			return "{\"error\":\"Bad file out\"}";
		}
		JSONObject returnjson = new JSONObject();
		returnjson.put("success", true);
		return returnjson.toJSONString();
	}

	@RequestMapping(value = "/delete", method = RequestMethod.POST, produces="application/json;charset=UTF-8")
	@ResponseBody
	public String deleteType(@RequestBody String requestString)
	{
		//TODO : Check this type is used in rule
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

		if (DBClient.isTypeConnect((String)json.get("typename")) == -1) {
			JSONObject returnjson = new JSONObject();
			returnjson.put("success", false);
			return returnjson.toJSONString();
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
		JSONObject returnjson = new JSONObject();
		returnjson.put("success", true);
		return returnjson.toJSONString();
	}

	@RequestMapping(value = "/edittype", method = RequestMethod.POST, produces="application/json;charset=UTF-8")
	@ResponseBody
	public String editType(@RequestBody String requestString)
	{
		//TODO : Check this type is used in rule
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
		request.addDBType();

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
		JSONObject returnjson = new JSONObject();
		returnjson.put("success", true);
		return returnjson.toJSONString();
	}

	@RequestMapping(value = "/gettype/{requestString}", method = RequestMethod.GET, produces="application/json;charset=UTF-8")
	@ResponseBody
	public String returnType(@PathVariable(value="requestString") String requestString)
	{	
		JSONObject obj = new JSONObject();

		obj.put("success", true);
		obj.put("data", DBClient.getType(requestString));
		return obj.toJSONString();
	}

	@RequestMapping(value = "/typelist", method = RequestMethod.GET, produces="application/json;charset=UTF-8")
	@ResponseBody
	public String returnTypeList()
	{
		JSONArray jarr = DBClient.getTreeTypeList();
		JSONObject obj = new JSONObject();
		obj.put("success", "true");
		obj.put("children", jarr);
		
		return obj.toJSONString();
	}
	
	@RequestMapping(value = "/addrule", method = RequestMethod.POST, produces="application/json;charset=UTF-8")
	@ResponseBody
	public String addRule(@RequestBody String requestString)
	{
		//TODO : Add spark stop and spark start
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
		
		/* add DB */
		if (DBClient.addRule(json, (String)json.get("name")) == -1)
		{
			JSONObject success = new JSONObject();
			success.put("success", false);
			return success.toJSONString();
		}
		
		DBClient.ruleTypeConnect(json);

		try	{
			CoreController.sparkStop();
			FileWriter writer = new FileWriter("/Users/hyunhoha/LocalCEP/Rule.json");
			JSONObject job = new JSONObject();
			job.put("Rule", DBClient.getRuleList());
			writer.write(job.toJSONString());
			writer.flush();
			writer.close();
			CoreController.sparkStart();
		}
		catch(IOException e)
		{
			return "{\"error\":\"Bad file out\"}";
		}
	
		JSONObject returnjson = new JSONObject();
		returnjson.put("success", true);
		return returnjson.toJSONString();
	}
	
	@RequestMapping(value = "/deleterule/{requestString}", method = RequestMethod.GET, produces="application/json;charset=UTF-8")
	@ResponseBody
	public String deleteRule(@PathVariable(value="requestString") String requestString)
	{
		DBClient.removeRule(requestString);
		try	{
			CoreController.sparkStop();
			FileWriter writer = new FileWriter("/Users/hyunhoha/LocalCEP/Rule.json");
			JSONObject job = new JSONObject();
			job.put("Rule", DBClient.getRuleList());
			writer.write(job.toJSONString());
			writer.flush();
			writer.close();
			CoreController.sparkStart();
		}
		catch(IOException e)
		{
			return "{\"error\":\"Bad file out\"}";
		}
		return "{\"error\":\"null\"}";
	}

	@RequestMapping(value = "/deleteallrule", method = RequestMethod.GET)
	@ResponseBody
	public String deleteAllRule()
	{
		DBClient.removeAllRule();
		try	{
			CoreController.sparkStop();
			FileWriter writer = new FileWriter("/Users/hyunhoha/LocalCEP/Rule.json");
			writer.flush();
			writer.close();
			CoreController.sparkStart();
		}
		catch(IOException e)
		{
			return "{\"error\":\"Bad file out\"}";
		}
		return "{\"error\":\"null\"}";
	}

	@RequestMapping(value = "/ruletreelist", method = RequestMethod.GET, produces="application/json;charset=UTF-8")
	@ResponseBody
	public String returnRuleTreeList()
	{
		JSONArray jarr = DBClient.getRuleTreeList();
		JSONObject obj = new JSONObject();
		obj.put("success", "true");
		obj.put("children", jarr);
		
		return obj.toJSONString();
	}

	@RequestMapping(value = "/rulelist", method = RequestMethod.GET, produces="application/json;charset=UTF-8")
	@ResponseBody
	public String returnRuleList()
	{
		JSONArray jarr = DBClient.getRuleList();
		JSONObject obj = new JSONObject();
		obj.put("Rule", jarr);
		
		return obj.toJSONString();
	}
	
	@RequestMapping(value = "/getruleframe/{requestString}", method = RequestMethod.GET, produces="application/json;charset=UTF-8")
	@ResponseBody
	public String returnRuleFrame(@PathVariable(value="requestString") String requestString)
	{
		JSONObject obj = DBClient.getRuleFrame(requestString);
		return obj.toJSONString();
	}
	
	@RequestMapping(value = "/getrule/{requestString}", method = RequestMethod.GET, produces="application/json;charset=UTF-8")
	@ResponseBody
	public String returnRule(@PathVariable(value="requestString") String requestString)
	{	
		JSONObject obj = new JSONObject();
		obj.put("success", true);
		obj.put("data", DBClient.getRule(requestString));
		return obj.toJSONString();
	}
}
