package com.mycompany.app;

import java.io.Serializable;

/**
 * This class is implemented to manage "type".
 * "type" means packet from kafka.
 * 
 * @version 1.0 31 May 2016
 * @author CS409 CARPENTER (DAESEONG KIM, JAEYOUNG AHN, HYNHO HA)
 */
public class Type implements Serializable {
	
	/**
	 * "typeName" comes from kafka's packet.
	 * The packet from kakfa has a following example.
	 * { "type" : "apachepost", "content" : "blahblah" }
	 * In this example "apachepost" is a typeName.
	 * It is defined by user.
	 */
	public String typeName;
	
	/**
	 * "content" comes from kafka's packet.
	 * It is json String and has some attributes values.
	 */
	public String content = null;
	
	/** The time when the packet is received by spark. */
	public long time;
	
	/** 
	 * Constructor for input packet. 
	 * This is used for Rule's "checkedTypes". 
	 */
	public Type( String typeName_ , String content_ ) {
		typeName = typeName_;
		content = content_;
		time = System.currentTimeMillis();
	}
	
	/** 
	 * Constructor for type definition.
	 * This is used for Rule's "types"
	 */
	public Type( String typeName_) {
		typeName = typeName_;
		time = System.currentTimeMillis();
	}
	

	/**
	 * @return cloned Type.
	 */
	public Type typeClone() {
		String _typeName = typeName;
		long _time = time;
		
		Type t;
		if (content != null) {
			String _content = content;
			t = new Type (_typeName, _content);
			t.time = _time;
		}
		else {
			t = new Type (_typeName);
			t.time = _time;
		}
		return t;
	}
}