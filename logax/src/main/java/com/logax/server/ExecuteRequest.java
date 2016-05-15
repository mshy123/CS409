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
	private String pos_file;

	public ExecuteRequest()
	{
		typename = null;
		typeregex = null;
		priority = null;
		path = null;
		pos_file = null;
	}

	public void print(FileWriter writer) throws IOException
	{
		writer.write("<source>\n");
		writer.write("  @type tail\n");
		writer.write("  format " + typeregex + "\n");
		writer.write("  path " + path + "\n");
		writer.write("  pos_file " + pos_file + "\n");
		writer.write("  tag " + priority + "." + typename + "\n");
		writer.write("</source>" + "\n");
		writer.flush();
		writer.close();
	}

	public int addDBType()
	{
		return DBClient.addType(typename, typeregex, priority, path, pos_file);
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
		this.pos_file = json.getString("pos_file");
	}
}
