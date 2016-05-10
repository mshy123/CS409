package com.mycompany.app;

public class Tuple {
	public String typeName;
	public String content;
	public long time;
	public Tuple( String typeName_ , String content_ ) {
		typeName = typeName_;
		content = content_;
		time = System.currentTimeMillis();
	}
}
