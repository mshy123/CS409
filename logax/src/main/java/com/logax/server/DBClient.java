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
		type.put("typeregex", document.getString("typeregex"));
		type.put("priority", document.getString("priority"));
		type.put("path", document.getString("path"));
		type.put("pos_file", document.getString("pos_file"));
		array.add(type);
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

	public static void addType(String typename, String typeregex, String priority, String path, String pos_file)
	{
		init();
		Document type = new Document()
			.append("typename", typename)
			.append("typeregex", typeregex)
			.append("priority", priority)
			.append("path", path)
			.append("pos_file", pos_file);
		db.getCollection("type").insertOne(type);
	}

	public static JSONArray getTypeList()
	{
		init();

		JSONArray typelist = new JSONArray();
		FindIterable<Document> iterable = db.getCollection("type").find();
		iterable.forEach(new ArrayBlock(typelist));
		return typelist;
	}
}
