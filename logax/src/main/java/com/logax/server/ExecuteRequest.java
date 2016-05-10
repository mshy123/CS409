package com.logax.server;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;

public class ExecuteRequest
{
	private String typeName;
	private String typeRegex;
	private String priority;
	private String path;
	private String pos_file;

	public ExecuteRequest()
	{
		typeName = null;
		typeRegex = null;
		priority = null;
		path = null;
		pos_file = null;
	}

	public void print(FileWriter writer) throws IOException
	{
		writer.write("<source>\n");
		writer.write("  @type tail\n");
		writer.write("  format " + typeRegex + "\n");
		writer.write("  path " + path + "\n");
		writer.write("  pos_file " + pos_file + "\n");
		writer.write("  tag " + priority + "\n");
		writer.write("</source>" + "\n");
		writer.flush();
		writer.close();
	}
	public void parse(JSONObject jsonObject) throws JsonTypeException
	{
		SafeJson json = new SafeJson(jsonObject);

		this.typeName = json.getString("typeName");
		this.typeRegex = json.getString("typeRegex");
		this.priority = json.getString("priority");
		this.path = json.getString("path");
		this.pos_file = json.getString("pos_file");
	}
}
