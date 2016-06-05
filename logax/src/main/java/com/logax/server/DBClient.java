/*
 * DBClient
 * version 1.0
 * This class is handle the mongoDB
 * DB has 4 collection, type, rule, ruletype, rules(This collection is handle on the spark)
 */

package com.logax.server;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.bson.Document;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.FindIterable;

import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Filters;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;

/*
 * This class extends Block<Document> that check each document in iteration
 * It return the JSONArray that will print in the Rule.json
 */
class RuleArrayBlock implements Block<Document>
{
	private JSONArray array;
	
	public RuleArrayBlock(JSONArray array) {
		this.array = array;
	}
	
	@Override
	public void apply(final Document document)
	{
		JSONParser parser = new JSONParser();
		JSONObject rule = null;
		JSONArray newArray = new JSONArray();
		try
		{
			rule = (JSONObject)parser.parse(document.getString("body"));
			JSONArray types = (JSONArray)rule.get("types");
			for (Object obj : types) {
				JSONObject type = (JSONObject) obj;
				String name = (String)type.get("name");
				name = DBClient.getTypePriority(name) + name;
				type.remove("name");
				type.put("name", name);
				newArray.add(type);
			}
			rule.remove("types");
			rule.put("types", newArray);
			String order = (String)rule.get("ordered");
			rule.remove("ordered"); //type casting
			if(order.equals("true")) {
				rule.put("ordered", true);
			}
			else {
				rule.put("ordered", false);
			}
		}
		catch(ParseException e)
		{
		}

		array.add(rule);
	}
}

/*
 * This class extends Block<Document> that chech each document in iteration
 * It return the JSONArray that will print in the fluent.conf
 */

class ArrayBlock implements Block<Document>
{
	private JSONArray array;
	
	public ArrayBlock(JSONArray array) {
		this.array = array;
	}
	
	@Override
	public void apply(final Document document)
	{
		JSONObject type = new JSONObject();
		type.put("typename", document.getString("typename"));
		type.put("regexnum", document.getString("regexnum"));
		type.put("typeregex", document.getString("typeregex"));
		type.put("priority", document.getString("priority"));
		type.put("path", document.getString("path"));
		array.add(type);
	}
}

/*
 * This class extends Block<Document> that chech each document in iteration
 * It return the tree type JSONArray that will print on the client type treepanel
 */
class TreeBlock implements Block<Document>
{
	private JSONArray array;
	
	public TreeBlock(JSONArray array) {
		this.array = array;
	}
	
	@Override
	public void apply(final Document document)
	{
		JSONObject type = new JSONObject();
		type.put("text", document.getString("typename"));
		type.put("leaf", "true");
		array.add(type);
	}
}

/*
 * This class extends Block<Document> that chech each document in iteration
 * It return the tree type Json type JSONArray that will print on the client rule type combobox
 */
class JsonBlock implements Block<Document>
{
	private JSONArray array;
	private int num;

	public JsonBlock(JSONArray array) {
		this.array = array;
		num = 1;
	}
	
	@Override
	public void apply(final Document document)
	{
		JSONObject type = new JSONObject();
		type.put("typename", document.getString("typename"));
		type.put("code", num);
		num++;
		array.add(type);
	}
}

/*
 * This class extends Block<Document> that chech each document in iteration
 * It return the tree type JSONArray that will print on the client rule treepanel
 */
class TreeRuleBlock implements Block<Document>
{
	private JSONArray array;
	
	public TreeRuleBlock(JSONArray array) {
		this.array = array;
	}
	
	@Override
	public void apply(final Document document)
	{
		JSONObject rule = new JSONObject();
		rule.put("text", document.getString("rulename"));
		rule.put("leaf", "true");
		array.add(rule);
	}
}

public class DBClient
{
	private static MongoClient client = null;	// DBClient
	private static MongoDatabase db = null;		// Database

	/*
	 * This method initialize the DB
	 */
	private static void init()
	{
		if (client == null)
		{
			client = new MongoClient("localhost", 27017); //You can set mongoDB server
			db = client.getDatabase("test");
		}
	}

	/*
	 * This method add type to the type DB
	 */
	public static int addType(String typename, String regexnum, String typeregex, String priority, String path)
	{
		init();
		Document type = new Document()
			.append("typename", typename)
			.append("regexnum", regexnum)
			.append("typeregex", typeregex)
			.append("priority", priority)
			.append("path", path);
		/* Check type name is already exist */
		FindIterable<Document> iterable = db.getCollection("type").find(new Document("typename", typename));
		for (Document document : iterable)
		{
			/* If typename already exist, return fail */
			return -1;
		}
		/* Add to DB */
		db.getCollection("type").insertOne(type);
		return 0;
	}
	
	/*
	 * This method edit type to the type DB
	 */
	public static int editType(String typename, String regexnum, String typeregex, String priority, String path)
	{
		int exist = 0;
		init();
		Document type = new Document()
			.append("typename", typename)
			.append("regexnum", regexnum)
			.append("typeregex", typeregex)
			.append("priority", priority)
			.append("path", path);
		FindIterable<Document> iterable = db.getCollection("type").find(new Document("typename", typename));
		/* Check type name is already exist */
		for (Document document : iterable)
		{
			exist = 1;
		}

		if (exist == 1) {
			/* If typename exist, remove it and add new one */
			db.getCollection("type").deleteMany(new Document("typename", typename));
			db.getCollection("type").insertOne(type);

			return 0;
		}
		/* If typename not exist, return fail */
		return -1;
	}
	
	/*
	 * This method remove type from the type DB
	 */
	public static void removeType(String typename)
	{
		init();

		db.getCollection("type").deleteMany(new Document("typename", typename));
	}

	/*
	 * This method remove all type from the type DB
	 */
	public static void removeAllType()
	{
		init();

		db.getCollection("type").deleteMany(new Document());
	}

	/*
	 * This method return number of regular expression in specific type
	 */
	public static int getTypeFrame(String typename)
	{
		init();
		JSONObject type = new JSONObject();
		FindIterable<Document> iterable = db.getCollection("type").find(new Document("typename", typename));
		for (Document document : iterable)
		{
			return Integer.parseInt(document.getString("regexnum"));
		}

		return 0;
	}

	public static String getTypePriority(String typename)
	{
		init();
		FindIterable<Document> iterable = db.getCollection("type").find(new Document("typename", typename));
		for (Document document : iterable) {
			return document.getString("priority") + ".";
		}
		return "low.";
	}

	/*
	 * This method return information of specific type
	 */
	public static JSONObject getType(String typename)
	{
		JSONParser parser = new JSONParser();
		JSONArray typeregexarray = null;

		init();
		JSONObject type = new JSONObject();
		FindIterable<Document> iterable = db.getCollection("type").find(new Document("typename", typename));
		for (Document document : iterable)
		{
			type.put("typename", document.getString("typename"));
			type.put("regexnum", document.getString("regexnum"));
			try
			{
				/* Get each regular expression */
				typeregexarray = (JSONArray)parser.parse(document.getString("typeregex"));
				int i = 0;
				for (Object obj : typeregexarray) {
					JSONObject job = (JSONObject) obj;
					type.put("typeregex" + i, (String)job.get("typeregex"));
					i++;
				}
			}
			catch(ParseException e)
			{
			}
			type.put("priority", document.getString("priority"));
			type.put("path", document.getString("path"));
		}
		
		return type;
	}

	/*
	 * This method return all type DB
	 */
	public static JSONArray getTypeList()
	{
		init();

		JSONArray typelist = new JSONArray();
		FindIterable<Document> iterable = db.getCollection("type").find();
		iterable.forEach(new ArrayBlock(typelist));
		return typelist;
	}

	/*
	 * This method return all type DB in tree format. Used in type treepanel
	 */
	public static JSONArray getTreeTypeList()
	{
		init();

		JSONArray typelist = new JSONArray();
		FindIterable<Document> iterable = db.getCollection("type").find();
		iterable.forEach(new TreeBlock(typelist));

		return typelist;
	}

	/*
	 * This method return all type DB in json format. Used in rule type combobox
	 */
	public static JSONArray getJsonTypeList()
	{
		init();

		JSONArray typelist = new JSONArray();
		FindIterable<Document> iterable = db.getCollection("type").find();
		iterable.forEach(new JsonBlock(typelist));

		return typelist;
	}

	/*
	 * This method return new uniqueid
	 */
	public static String getuniqueid()
	{
		init();
		Document lastJob = db.getCollection("rule")
			.find()
			.sort(new Document("uniqueid", -1))
			.first();
		if (lastJob == null)
			return "1";
		else
			return Integer.toString(Integer.parseInt(lastJob.getString("uniqueid")) + 1);
	}

	/*
	 * This method add rule to the rule DB
	 */
	public static int addRule(JSONObject job, String rulename)
	{
		init();
	
		Document type = new Document()
			.append("uniqueid", getuniqueid())
			.append("rulename", rulename)
			.append("body", job.toString());
		/* Check rule name is already exist */
		FindIterable<Document> iterable = db.getCollection("rule").find(new Document("rulename", rulename));
		for (Document document : iterable)
		{
			/* Rule name is already exist, return fail */
			return -1;
		}
		/* Add rule */
		db.getCollection("rule").insertOne(type);
	
		return 0;
	}

	/* 
	 * This method remove from from the rule DB
	 */
	public static void removeRule(String id)
	{
		init();
		
		/* Find rule */
		FindIterable<Document> iterable = db.getCollection("rule").find(new Document("uniqueid", id));
		for (Document document : iterable)
		{
			String rulename = (String)document.getString("rulename");
			/* Disconnect the rule and type */
			ruleTypeDisconnect(rulename);
		}

		/* Remove rule */
		db.getCollection("rule").deleteMany(new Document("uniqueid", id));
	}
	
	/* 
	 * This method remove all rule DB
	 */
	public static void removeAllRule()
	{
		init();
		db.getCollection("ruletype").deleteMany(new Document());
		db.getCollection("rule").deleteMany(new Document());
	}

	/* 
	 * This method get all rule information from the rule DB. It will print in Rule.json
	 */
	public static JSONArray getRuleList()
	{
		init();

		JSONArray rulelist = new JSONArray();
		FindIterable<Document> iterable = db.getCollection("rule").find();
		iterable.forEach(new RuleArrayBlock(rulelist));
		return rulelist;
	}

	/* 
	 * This method get all number of attribute and types in rule from the rule DB
	 */
	public static JSONObject getRuleFrame(String rulename)
	{
		init();

		/* Find rule */
		FindIterable<Document> iterable = db.getCollection("rule").find(new Document("rulename", rulename));
		JSONObject result = new JSONObject();
		JSONParser parser = new JSONParser();
		try
		{
			for (Document document : iterable) {
				/* Put the number information in result */
				JSONObject rule = (JSONObject)parser.parse(document.getString("body"));
				JSONArray typearray = (JSONArray)rule.get("types");
				JSONArray attarray = (JSONArray)rule.get("attributes");
				result.put("success", true);
				result.put("typenum", Integer.toString(typearray.size()));
				result.put("attnum", Integer.toString(attarray.size()));
			}
		}
		catch(ParseException e)
		{
			/* Fail to parse it */
			result.put("success", false);
		}
		return result;
	}

	/* 
	 * This method get specific rule information. It will print on the client rulecontainer.
	 */
	public static JSONObject getRule(String rulename)
	{
		init();

		/* Find rule */
		FindIterable<Document> iterable = db.getCollection("rule").find(new Document("rulename", rulename));
		JSONObject result = new JSONObject();
		JSONParser parser = new JSONParser();
		try
		{
			for (Document document : iterable) {
				/* Type Casting */
				JSONObject rule = (JSONObject)parser.parse(document.getString("body"));
				JSONArray typearray = (JSONArray)rule.get("types");
				JSONArray attarray = (JSONArray)rule.get("attributes");
				result.put("controlleruniqueid", (String)document.getString("uniqueid"));
				result.put("controllername", (String)rule.get("name"));
				result.put("controllerduration", (int)(long) (Long)rule.get("duration"));
				result.put("controllerordered", (String)rule.get("ordered"));

				int i = 0;
				for (Object ob : typearray) {
					JSONObject job = (JSONObject)ob;
					result.put("controllertypename" + Integer.toString(i), (String)job.get("name"));
					result.put("controllertypenum" + Integer.toString(i), (int)(long)(Long)job.get("number"));
					i++;
				}
				i = 0;
				for (Object ob : attarray) {
					JSONObject job = (JSONObject)ob;
					result.put("controllerattribute" + Integer.toString(i), (String)job.get("name"));
					i++;
				}
			}
		}
		catch(ParseException e)
		{
			/* Fail to parse it */
			result.put("success", false);
		}
		return result;
	}
	
	/*
	 * This method return all rulename of rule DB in tree format. Used in rule treepanel
	 */
	public static JSONArray getRuleTreeList()
	{
		init();

		JSONArray rulelist = new JSONArray();
		FindIterable<Document> iterable = db.getCollection("rule").find();
		iterable.forEach(new TreeRuleBlock(rulelist));

		return rulelist;
	}

	/*
	 * This method connect rule and type. Connected type cannot be delete without delete connected rule
	 */
	public static void ruleTypeConnect(JSONObject rule)
	{
		init();
		JSONArray jarr = (JSONArray)rule.get("types");
		for (Object obj : jarr) {
			JSONObject job = (JSONObject)obj;
			Document type = new Document()
				.append("type", (String)job.get("name"))
				.append("rule", (String)rule.get("name"));
			db.getCollection("ruletype").insertOne(type);
		}
	}

	/*
	 * Disconnect type and rule when rule is delete
	 */
	public static void ruleTypeDisconnect(String rulename)
	{
		init();
		db.getCollection("ruletype").deleteMany(new Document("rule", rulename));
	}

	/*
	 * This method check specific type is connect with other rules
	 */
	public static int isTypeConnect(String typename)
	{
		init();
		FindIterable<Document> iterable = db.getCollection("ruletype").find(new Document("type", typename));
		for (Document document : iterable)
		{
			/* This type is connect with other rules */
			return -1;
		}

		/* This type is not connected */
		return 0;
	}	
	
	/* 
	 * This method check that there is some type that connect with some rule
	 */
	public static int isAllTypeConnect()
	{
		init();
		FindIterable<Document> iterable = db.getCollection("ruletype").find();
		for (Document document : iterable)
		{
			/* Some type is connect with rule */
			return -1;
		}
		
		/* None of the type is connect with rule */
		return 0;
	}	
}
