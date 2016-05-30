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
		try
		{
			rule = (JSONObject)parser.parse(document.getString("body"));
			String order = (String)rule.get("ordered");
			rule.remove("ordered");
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
	private static MongoClient client = null;
	private static MongoDatabase db = null;

	private static void init()
	{
		if (client == null)
		{
			client = new MongoClient("localhost", 27017);
			db = client.getDatabase("test");
		}
	}

	public static int addType(String typename, String regexnum, String typeregex, String priority, String path)
	{
		init();
		Document type = new Document()
			.append("typename", typename)
			.append("regexnum", regexnum)
			.append("typeregex", typeregex)
			.append("priority", priority)
			.append("path", path);
		FindIterable<Document> iterable = db.getCollection("type").find(new Document("typename", typename));
		for (Document document : iterable)
		{
			return -1;
		}
		db.getCollection("type").insertOne(type);
		return 0;
	}
	
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
		for (Document document : iterable)
		{
			exist = 1;
		}

		if (exist == 1) {
			db.getCollection("type").deleteMany(new Document("typename", typename));
			db.getCollection("type").insertOne(type);
			return 0;
		}
		return -1;
	}
	
	public static void removeType(String typename)
	{
		init();

		db.getCollection("type").deleteMany(new Document("typename", typename));
	}

	public static void removeAllType()
	{
		init();

		db.getCollection("type").deleteMany(new Document());
	}

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

	public static JSONArray getTypeList()
	{
		init();

		JSONArray typelist = new JSONArray();
		FindIterable<Document> iterable = db.getCollection("type").find();
		iterable.forEach(new ArrayBlock(typelist));
		return typelist;
	}

	public static JSONArray getTreeTypeList()
	{
		init();

		JSONArray typelist = new JSONArray();
		FindIterable<Document> iterable = db.getCollection("type").find();
		iterable.forEach(new TreeBlock(typelist));

		return typelist;
	}

	public static JSONArray getJsonTypeList()
	{
		init();

		JSONArray typelist = new JSONArray();
		FindIterable<Document> iterable = db.getCollection("type").find();
		iterable.forEach(new JsonBlock(typelist));

		return typelist;
	}

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

	public static int addRule(JSONObject job, String rulename)
	{
		init();
		Document type = new Document()
			.append("uniqueid", getuniqueid())
			.append("rulename", rulename)
			.append("body", job.toString());
		FindIterable<Document> iterable = db.getCollection("rule").find(new Document("rulename", rulename));
		for (Document document : iterable)
		{
			return -1;
		}
		db.getCollection("rule").insertOne(type);
	
		return 0;
	}

	public static void removeRule(String id)
	{
		init();
		
		FindIterable<Document> iterable = db.getCollection("rule").find(new Document("uniqueid", id));
		for (Document document : iterable)
		{
			String rulename = (String)document.getString("rulename");
			ruleTypeDisconnect(rulename);
		}
		db.getCollection("rule").deleteMany(new Document("uniqueid", id));
	}
	
	public static void removeAllRule()
	{
		init();
		db.getCollection("ruletype").deleteMany(new Document());
		db.getCollection("rule").deleteMany(new Document());
	}

	public static JSONArray getRuleList()
	{
		init();

		JSONArray rulelist = new JSONArray();
		FindIterable<Document> iterable = db.getCollection("rule").find();
		iterable.forEach(new RuleArrayBlock(rulelist));
		return rulelist;
	}

	public static JSONObject getRuleFrame(String rulename)
	{
		init();
		FindIterable<Document> iterable = db.getCollection("rule").find(new Document("rulename", rulename));
		JSONObject result = new JSONObject();
		JSONParser parser = new JSONParser();
		try
		{
			for (Document document : iterable) {

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
			result.put("success", false);
		}
		return result;
	}

	public static JSONObject getRule(String rulename)
	{
		init();
		FindIterable<Document> iterable = db.getCollection("rule").find(new Document("rulename", rulename));
		JSONObject result = new JSONObject();
		JSONParser parser = new JSONParser();
		try
		{
			for (Document document : iterable) {
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
			result.put("success", false);
		}
		return result;
	}
	
	public static JSONArray getRuleTreeList()
	{
		init();

		JSONArray rulelist = new JSONArray();
		FindIterable<Document> iterable = db.getCollection("rule").find();
		iterable.forEach(new TreeRuleBlock(rulelist));

		return rulelist;
	}

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

	public static void ruleTypeDisconnect(String rulename)
	{
		init();
		db.getCollection("ruletype").deleteMany(new Document("rule", rulename));
	}

	public static int isTypeConnect(String typename)
	{
		init();
		FindIterable<Document> iterable = db.getCollection("ruletype").find(new Document("type", typename));
		for (Document document : iterable)
		{
			return -1;
		}

		return 0;
	}	
	
	public static int isAllTypeConnect()
	{
		init();
		FindIterable<Document> iterable = db.getCollection("ruletype").find();
		for (Document document : iterable)
		{
			return -1;
		}

		return 0;
	}	
}
