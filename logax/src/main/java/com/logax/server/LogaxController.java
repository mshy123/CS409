/*
 * LogaxController
 * version 1.0
 * This class is handle all the request (GET, POST) from the client (sencha)
 * All the request is map by /api/{command}, and called when client request.
 */

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
	private String fluentpath = "/Users/hyunhoha/LocalCEP/fluent/fluent.conf";	// This is path for fluent.conf. You have to edit it.
	private String rulepath = "/Users/hyunhoha/LocalCEP/Rule.json"; 			// This is path for Rule.json. You have to edit it.
	private String pos_file = "/Users/hyunhoha/LocalCEP/fluent/pos/"; 			// This is path for fluent_pos. You have to edit it.

	/*
	 * This method return the Message that handled by client.
	 * When i = 0, return the fail message, otherwise return success message
	 */
	public String resultMessage(int i, String message)
	{
		JSONObject job = new JSONObject();
		if (i == 0) {
			// Return message when fail to process
			job.put("success", false);
			job.put("message", message);
			return job.toJSONString();
		}
		// Otherwise, return message success.
		job.put("success", true);
		job.put("message", message);
		return job.toJSONString();
	}

	/*
	 * This method receive rule from the DB, and print it on the Rule.json
	 */
	public String printRule()
	{
		try	{
			//CoreController.sparkStop(); This is not working currently
			FileWriter writer = new FileWriter(rulepath);
			JSONObject job = new JSONObject();
			if (DBClient.getRuleList() != null) {
				// When the rule DB is not empty
				job.put("Rule", DBClient.getRuleList());
				writer.write(job.toJSONString());
			}
			writer.flush();
			writer.close();
			//CoreController.sparkStart(); This is not working currently
		}
		catch(IOException e)
		{
			return resultMessage(0, "CEP Engine doesn't work or wring in rulepath");
		}
	
		return resultMessage(1, "Success to handle request");
	}

	/*
	 * This method receive type from the DB, and print it on the fluent.conf
	 */
	public String printFluentd()
	{
		FileWriter writer = null;
		ExecuteRequest request = new ExecuteRequest();
		
		try	{	
			/* Stop the core process */
			//CoreController.stopCore();
			/* Add fluent.conf File : source, generate kafa, elasticsearch */
			writer = new FileWriter(fluentpath);
			JSONArray jarr = DBClient.getTypeList();
			for (int i = 0; i < jarr.size(); i++)
			{
				JSONObject job = (JSONObject)jarr.get(i);
				try {
					request.parse2(job);
				}
				catch (JsonTypeException e)
				{
					return resultMessage(0, "Wrong Input type3");
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

			/* Add fluent.conf File : match Elasticsearch */
			for (int i = 0; i < jarr.size(); i++)
			{
				JSONObject job = (JSONObject)jarr.get(i);
				try {
					request.parse2(job);
				}
				catch (JsonTypeException e)
				{
					return resultMessage(0, "Wrong Input type4");
				}
				request.printElastic(writer, pos_file);
			}

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
			//CoreController.startCore();
		}
		catch(IOException e)
		{
			/* when the core doesn't work or cannot edit fluent.conf */
			return resultMessage(0, "Fail to Start Core or Input file is wrong");
		}

		return resultMessage(1, "Success to handle request");
	}
	
	
	/*
	 * This method receive type from the client, and add DB
	 * All process is success, call printFluentd()
	 */
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
			return resultMessage(0, "Wrong Input type1");
		}

		ExecuteRequest request = new ExecuteRequest();
		
		try
		{
			/* Parse the request */
			request.parse(json);
		}
		catch(JsonTypeException e)
		{
			/* Parse failed */
			return resultMessage(0, "Wrong Input type2");
		}
		/* add DB */
		if (request.addDBType() == -1)
		{
			/* Name is already exist */
			return resultMessage(0, "Already exist name");
		}
	
		return printFluentd();
	}

	/*
	 * This method delete all type DB
	 * After request success, print DB into fluent.conf using printFluentd()
	 */
	@RequestMapping(value = "/deletealltype", method = RequestMethod.GET, produces="application/json;charset=UTF-8")
	@ResponseBody
	public String deleteAllType()
	{
		/* Check rule contains some type */
		if (DBClient.isAllTypeConnect() == -1) {
			/* Rule contains types, so can't delete */
			return resultMessage(0, "Type is connected with rule");
		}
		
		/* Otherwise, delete all type */
		DBClient.removeAllType();
		
		return printFluentd();
	}

	/*
	 * This method delete specific typename in DB
	 * Client request by GET, with the typename
	 * After request success, print DB into fluent.conf using printFluentd()
	 */
	@RequestMapping(value = "/deletetype/{requestString}", method = RequestMethod.GET, produces="application/json;charset=UTF-8")
	@ResponseBody
	public String deleteType(@PathVariable(value="requestString") String requestString)
	{
		/* Check rule contains some type */
		if (DBClient.isTypeConnect(requestString) == -1) {
			/* Rule contains types, so can't delete */
			return resultMessage(0, "Type is connected with rule");
		}

		/* Otherwise, delete the type */
		DBClient.removeType(requestString);

		return printFluentd();
	}

	/*
	 * This method edit type in DB
	 * Client request by POST, with the all information need to make type
	 * After request success, print DB into fluent.conf using printFluentd()
	 */
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
			/* Parse the request */
			request.parse(json);
		}
		catch(JsonTypeException e)
		{
			/* Parse failed */
			return "{\"error\":\"bad request\"}";
		}

		/* Check that this type is already exist */
		if (request.editDBType() != 0)
		{
			/* When the type is not exist, return fail message */
			return resultMessage(0, "Cannot make type by edit. Use Add command");
		}

		return printFluentd();
	}
	
	/*
	 * This method send number of regex in type
	 */
	@RequestMapping(value = "/gettypeframe/{requestString}", method = RequestMethod.GET, produces="application/json;charset=UTF-8")
	@ResponseBody
	public String returnTypeFrame(@PathVariable(value="requestString") String requestString)
	{	
		JSONObject obj = new JSONObject();

		obj.put("success", true);
		obj.put("regexnum", DBClient.getTypeFrame(requestString));
		return obj.toJSONString();
	}

	/*
	 * This method return type information
	 */
	@RequestMapping(value = "/gettype/{requestString}", method = RequestMethod.GET, produces="application/json;charset=UTF-8")
	@ResponseBody
	public String returnType(@PathVariable(value="requestString") String requestString)
	{	
		JSONObject obj = new JSONObject();

		obj.put("success", true);
		obj.put("data", DBClient.getType(requestString));
		return obj.toJSONString();
	}

	/*
	 * This method return type list, that shows the treelist
	 */
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
	
	/*
	 * This method return json type list that used in AddRule page.
	 */
	@RequestMapping(value = "/getjsontypelist", method = RequestMethod.GET, produces="application/json;charset=UTF-8")
	@ResponseBody
	public String returnJsonTypeList()
	{
		JSONObject obj = new JSONObject();
		obj.put("types", DBClient.getJsonTypeList());
		
		return obj.toJSONString();
	}

	/*
	 * This method receive Rule from the client, and add DB
	 * All process is success, call printRule()
	 */
	@RequestMapping(value = "/addrule", method = RequestMethod.POST, produces="application/json;charset=UTF-8")
	@ResponseBody
	public String addRule(@RequestBody String requestString)
	{
		JSONParser parser = new JSONParser();
		JSONObject json = null;
		try
		{
			/* Parse the Rule */
			json = (JSONObject)parser.parse(requestString);
		}
		catch(ParseException e)
		{
			/* Parse failed */
			return resultMessage(0, "Parse Error");
		}
		
		/* Check that rule name is already in DB, if not add to DB */
		if (DBClient.addRule(json, (String)json.get("name")) == -1)
		{
			/* Already exist */
			return resultMessage(0, "Already Exist Name");
		}
	
		/* Connect type and rule */
		DBClient.ruleTypeConnect(json);

		return printRule();
	}
	
	/*
	 * This method receive Rule unique id from the client, and delete it DB
	 * All process is success, call printRule()
	 */
	@RequestMapping(value = "/deleterule/{requestString}", method = RequestMethod.GET, produces="application/json;charset=UTF-8")
	@ResponseBody
	public String deleteRule(@PathVariable(value="requestString") String requestString)
	{
		DBClient.removeRule(requestString);
		
		return printRule();
	}

	/*
	 * This method delete all rule DB
	 * All process is success, call printRule()
	 */
	@RequestMapping(value = "/deleteallrule", method = RequestMethod.GET)
	@ResponseBody
	public String deleteAllRule()
	{
		DBClient.removeAllRule();
		
		return printRule();
	}

	/*
	 * This method return rule list with tree type 
	 */
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

	/*
	 * This method return all the rule's information
	 */
	@RequestMapping(value = "/rulelist", method = RequestMethod.GET, produces="application/json;charset=UTF-8")
	@ResponseBody
	public String returnRuleList()
	{
		JSONArray jarr = DBClient.getRuleList();
		JSONObject obj = new JSONObject();
		obj.put("Rule", jarr);
		
		return obj.toJSONString();
	}
	
	/*
	 * This method return number of attribute and types
	 */
	@RequestMapping(value = "/getruleframe/{requestString}", method = RequestMethod.GET, produces="application/json;charset=UTF-8")
	@ResponseBody
	public String returnRuleFrame(@PathVariable(value="requestString") String requestString)
	{
		JSONObject obj = DBClient.getRuleFrame(requestString);
		return obj.toJSONString();
	}
	
	/*
	 * This method return specific rule's information
	 * client request with the rulename
	 */
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
