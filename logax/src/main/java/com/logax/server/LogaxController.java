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
	private String fluentpath = "/Users/hyunhoha/LocalCEP/fluent/fluent.conf";
	private String rulepath = "/Users/hyunhoha/LocalCEP/Rule.json";
	private String pos_file = "/Users/hyunhoha/LocalCEP/fluent/pos/";

	public String resultMessage(int i, String message)
	{
		JSONObject job = new JSONObject();
		if (i == 0) {
			job.put("success", false);
			job.put("message", message);
			return job.toJSONString();
		}

		job.put("success", true);
		job.put("message", message);
		return job.toJSONString();
	}

	public String printRule()
	{
		try	{
			//CoreController.sparkStop();
			FileWriter writer = new FileWriter(rulepath);
			JSONObject job = new JSONObject();
			if (DBClient.getRuleList() != null) {
				job.put("Rule", DBClient.getRuleList());
				writer.write(job.toJSONString());
			}
			writer.flush();
			writer.close();
			//CoreController.sparkStart();
		}
		catch(IOException e)
		{
			return resultMessage(0, "CEP Engine doesn't work or wring in rulepath");
		}
	
		return resultMessage(1, "Success to handle request");
	}

	public String printFluentd()
	{
		FileWriter writer = null;
		ExecuteRequest request = new ExecuteRequest();
		
		try	{	
			/* Stop the core process */
			CoreController.stopCore();
			/* Edit fluentd File : type */
			writer = new FileWriter(fluentpath);
			JSONArray jarr = DBClient.getTypeList();
			for (int i = 0; i < jarr.size(); i++)
			{
				JSONObject job = (JSONObject)jarr.get(i);
				try {
					request.parse(job);
				}
				catch (JsonTypeException e)
				{
					return resultMessage(0, "Wrong Input type");
				}
				request.print(writer, pos_file);
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
		    writer.write("  default_topic       hightopic\n");
		    writer.write("  output_include_time true\n</match>\n\n");
			writer.write("<match low.**>\n");
	      	writer.write("  @type               kafka\n");
	        writer.write("  brokers             127.0.0.1:9092\n");
		    writer.write("  default_topic       lowtopic\n");
		    writer.write("  output_include_time true\n</match>\n\n");
			writer.flush();
			writer.close();
			CoreController.startCore();
		}
		catch(IOException e)
		{
			return resultMessage(0, "Fail to Start Core or Input file is wrong");
		}

		return resultMessage(1, "Success to handle request");
	}
	
	
	@RequestMapping(value = "/addtype", method = RequestMethod.POST, produces="application/json;charset=UTF-8")
	@ResponseBody
	public String addType(@RequestBody String requestString)
	{
		JSONParser parser = new JSONParser();
		JSONObject json = null;
		try
		{
			json = (JSONObject)parser.parse(requestString);
		}
		catch(ParseException e)
		{
			return resultMessage(0, "Wrong Input type");
		}

		ExecuteRequest request = new ExecuteRequest();
		
		try
		{
			request.parse(json);
		}
		catch(JsonTypeException e)
		{
			return resultMessage(0, "Wrong Input type");
		}
		/* add DB */
		if (request.addDBType() == -1)
		{
			return resultMessage(0, "Already exist name");
		}
	
		return printFluentd();
	}

	@RequestMapping(value = "/deletealltype", method = RequestMethod.GET, produces="application/json;charset=UTF-8")
	@ResponseBody
	public String deleteAllType()
	{
		if (DBClient.isAllTypeConnect() == -1) {
			return resultMessage(0, "Type is connected with rule");
		}
		
		DBClient.removeAllType();
		
		return printFluentd();
	}

	@RequestMapping(value = "/deletetype/{requestString}", method = RequestMethod.GET, produces="application/json;charset=UTF-8")
	@ResponseBody
	public String deleteType(@PathVariable(value="requestString") String requestString)
	{
		if (DBClient.isTypeConnect(requestString) == -1) {
			return resultMessage(0, "Type is connected with rule");
		}

		DBClient.removeType(requestString);

		return printFluentd();
	}

	@RequestMapping(value = "/edittype", method = RequestMethod.POST, produces="application/json;charset=UTF-8")
	@ResponseBody
	public String editType(@RequestBody String requestString)
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

		if (request.editDBType() != -1)
		{
			return resultMessage(1, "Cannot make type by edit. Use Add command");
		}

		return printFluentd();
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
	
	@RequestMapping(value = "/getjsontypelist", method = RequestMethod.GET, produces="application/json;charset=UTF-8")
	@ResponseBody
	public String returnJsonTypeList()
	{
		JSONObject obj = new JSONObject();
		obj.put("types", DBClient.getJsonTypeList());
		
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
			return resultMessage(0, "Parse Error");
		}
		
		/* add DB */
		if (DBClient.addRule(json, (String)json.get("name")) == -1)
		{
			return resultMessage(0, "Already Exist Name");
		}
		
		DBClient.ruleTypeConnect(json);

		return printRule();
	}
	
	@RequestMapping(value = "/deleterule/{requestString}", method = RequestMethod.GET, produces="application/json;charset=UTF-8")
	@ResponseBody
	public String deleteRule(@PathVariable(value="requestString") String requestString)
	{
		DBClient.removeRule(requestString);
		
		return printRule();
	}

	@RequestMapping(value = "/deleteallrule", method = RequestMethod.GET)
	@ResponseBody
	public String deleteAllRule()
	{
		DBClient.removeAllRule();
		
		return printRule();
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
