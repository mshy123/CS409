package com.mycompany.app;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class RuleReader {
	/**
	 * Read Rule.json and save parse into Rule instances.
	 * All result are saved in baseRules.
	 * @param baseRules is ArrayList that save Rules read.
	 */
	public static void readRule (ArrayList<Rule> baseRules) {
		try {
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(new FileReader("Rule.json"));

			JSONObject jsonObject = (JSONObject) obj;
			JSONArray Rules = (JSONArray) jsonObject.get("Rule");

			for (Object rule : Rules) {
				JSONObject jsonRule = (JSONObject) rule;

				JSONArray jsonTypes = (JSONArray) jsonRule.get("types");
				ArrayList<Type> types = new ArrayList<Type>();

				for (Object type : jsonTypes) {
					for (int i=0;
							i<Math.toIntExact((Long) ((JSONObject) type).get("number"));
							i++) {
						types.add(new Type ((String) ((JSONObject) type).get("name")));
					}
				}
				JSONArray jsonAttributes = (JSONArray) jsonRule.get("attributes");
				ArrayList<String> attributes = new ArrayList<String>();

				for (Object attribute : jsonAttributes) {
					attributes.add(((JSONObject) attribute).get("name").toString());
				}

				Rule Rule = new Rule ((String) jsonRule.get("name"),
						(Long) jsonRule.get("duration"),
						(Boolean) jsonRule.get("ordered"), 
						types, attributes);
				baseRules.add(Rule);
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
}
