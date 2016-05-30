package com.logax.server;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;

public class ExecuteRequest
{
	private String typename;
	private String regexnum;
	private String typeregex;
	private String priority;
	private String path;

	public ExecuteRequest()
	{
		typename = null;
		typeregex = null;
		priority = null;
		path = null;
	}

	public void print(FileWriter writer, String globalposfile) throws IOException
	{
		JSONParser parser = new JSONParser();
		try
		{
			JSONArray typeregexarray = (JSONArray)parser.parse(typeregex);
			for (Object ob : typeregexarray) {
				JSONObject job = (JSONObject) ob;
				String reg = (String)job.get("typeregex");
				writer.write("<source>\n");
				writer.write("  @type tail\n");
				writer.write("  format " + reg + "\n");
				writer.write("  path " + path + "\n");
				writer.write("  pos_file " + globalposfile + typename + "_kafka\n");
				writer.write("  tag " + priority + "." + typename + "\n");
				writer.write("  time_format %d/%b/%Y:%H:%M:%S %z\n");
				writer.write("  keep_time_key true\n");
				writer.write("</source>" + "\n\n");
				writer.write("<source>\n");
				writer.write("  @type tail\n");
				writer.write("  format " + reg + "\n");
				writer.write("  path " + path + "\n");
				writer.write("  pos_file " + globalposfile + typename + "_elastic\n");
				writer.write("  tag " + "elastic." + priority + "." + typename + "\n");
				writer.write("  time_format %d/%b/%Y:%H:%M:%S %z\n");
				writer.write("  keep_time_key true\n");
				writer.write("</source>" + "\n\n");
			}
		}
		catch(ParseException e)
		{
		}
	}

	public void printElastic(FileWriter writer, String globalposfile) throws IOException
	{
		writer.write("<match elastic.**>\n");
		writer.write("  @type elasticsearch\n");
		writer.write("  host localhost\n");
		writer.write("  port 9200\n");
		writer.write("  index_name " + typename + "\n");
		writer.write("  type_name " + typename + "\n");
		writer.write("  flush_interval 60s\n");
		writer.write("  logstash_format true\n");
		writer.write("</match>" + "\n\n");
	}
	public int addDBType()
	{
		return DBClient.addType(typename, regexnum, typeregex, priority, path);
	}
	
	public int editDBType()
	{
		return DBClient.editType(typename, regexnum, typeregex, priority, path);
	}

	public void removeDBType()
	{
		DBClient.removeType(typename);
	}

	public void parse2(JSONObject jsonObject) throws JsonTypeException
	{
		SafeJson json = new SafeJson(jsonObject);

		this.typename = json.getString("typename");
		this.regexnum = json.getString("regexnum");
		this.typeregex = json.getString("typeregex");
		this.priority = json.getString("priority");
		this.path = json.getString("path");
	}
	public void parse(JSONObject jsonObject) throws JsonTypeException
	{
		SafeJson json = new SafeJson(jsonObject);

		this.typename = json.getString("typename");
		this.regexnum = json.getString("regexnum");
		this.typeregex = ((JSONArray)jsonObject.get("typeregex")).toString();
		this.priority = json.getString("priority");
		this.path = json.getString("path");
	}
}
