package com.mycompany.app;

import java.io.Serializable;

public class Tuple implements Serializable {
	public String typeName;
	public String content;
	public int number;
	public long time;
	public Tuple( String typeName_ , String content_ ) {
		typeName = typeName_;
		content = content_;
		time = System.currentTimeMillis();
	}
	public Tuple( String typeName_ , int number_ ) {
		typeName = typeName_;
		number = number_;
		time = System.currentTimeMillis();
	}
	
}
