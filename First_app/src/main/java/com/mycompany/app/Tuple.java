package com.mycompany.app;

import java.io.Serializable;

public class Tuple implements Serializable {
	public String typeName;
	public String content = null;
	public long time;
	
	public Tuple( String typeName_ , String content_ ) {
		typeName = typeName_;
		content = content_;
		time = System.currentTimeMillis();
	}
	public Tuple( String typeName_) {
		typeName = typeName_;
		time = System.currentTimeMillis();
	}
	public Tuple tupleClone() {
		String _typeName = typeName;
		long _time = time;
		
		Tuple t;
		if (content != null) {
			String _content = content;
			t = new Tuple (_typeName, _content);
			t.time = _time;
		}
		else {
			t = new Tuple (_typeName);
			t.time = _time;
		}
		return t;
	}
}