/*
 * SafeJSON
 * version 1.0
 * This class receive JSONObject, and return it specific class type with check
 */

package com.logax.server;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/* Type-checking JSONObject wrapper*/
public class SafeJson
{
	private JSONObject json;

	public SafeJson(JSONObject json)
	{
		this.json = json;
	}

	public String getString(String key) throws JsonTypeException
	{
		Object item = json.get(key);
		/* Check item is not null and has String type */
		if (item != null && item instanceof String)
			return (String)item;
		else
			throw new JsonTypeException("Key error: " + key);
	}

	public JSONObject getObject(String key) throws JsonTypeException
	{
		Object item = json.get(key);
		/* Check item is not null and has JSONObject type */
		if (item != null && item instanceof JSONObject)
			return (JSONObject)item;
		else
			throw new JsonTypeException("Key error: " + key);
	}
}
