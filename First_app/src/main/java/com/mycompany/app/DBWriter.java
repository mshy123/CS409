package com.mycompany.app;

import java.util.ArrayList;
import java.util.Date;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;

public class DBWriter {
	/** Write complete rule to mongoDB */
	public static void writeDB (Rule r) {
		Mongo mongo = new Mongo("143.248.222.116", 27017);
		DB db = mongo.getDB("test");
		DBCollection collection = db.getCollection("Rules");
		BasicDBObject document = new BasicDBObject();
		document.put("name", r.getName());

		ArrayList<BasicDBObject> types = new ArrayList<BasicDBObject>();
		for (Type t : r.getCheckedTypes()) {
			BasicDBObject tmp = new BasicDBObject();
			tmp.put("name", t.typeName);
			tmp.put("content", t.content);
			types.add(tmp);
		}
		document.put("types", types);

		Date now = new Date();
		BasicDBObject timeNow = new BasicDBObject("date", now);
		document.put("savedTime", timeNow);
		
		collection.insert(document);
		mongo.close();
	}
}
