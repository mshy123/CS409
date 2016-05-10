package com.mycompany.app;

import java.io.Serializable;

public class Tuple implements Serializable {
	public String typeName;
	public String content;
	public Tuple( String typeName_ , String content_ ) {
		typeName = typeName_;
		content = content_;
	}
}
