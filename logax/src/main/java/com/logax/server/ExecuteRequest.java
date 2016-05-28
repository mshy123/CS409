package com.logax.server;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;

public class ExecuteRequest
{
	private String typename;
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
		writer.write("<source>\n");
		writer.write("  @type tail\n");
		writer.write("  format " + typeregex + "\n");
		writer.write("  path " + path + "\n");
		writer.write("  pos_file " + globalposfile + typename + "\n");
		writer.write("  tag " + priority + "." + typename + "\n");
		writer.write("  time_format %d/%b/%Y:%H:%M:%S %z\n");
		writer.write("</source>" + "\n\n");
	}

	public int addDBType()
	{
		return DBClient.addType(typename, typeregex, priority, path);
	}
	
	public int editDBType()
	{
		return DBClient.editType(typename, typeregex, priority, path);
	}

	public void removeDBType()
	{
		DBClient.removeType(typename);
	}

	public void parse(JSONObject jsonObject) throws JsonTypeException
	{
		SafeJson json = new SafeJson(jsonObject);

		this.typename = json.getString("typename");
		this.typeregex = json.getString("typeregex");
		this.priority = json.getString("priority");
		this.path = json.getString("path");
	}
}
