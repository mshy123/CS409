package com.mycompany.app;

import java.io.Serializable;
import java.util.ArrayList;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import scala.util.parsing.json.JSON;

public class Rule implements Serializable{
	
	private String name;
	
	private long birthTime;
	
	private long duration;
	
	private ArrayList<Tuple> types;
	
	private ArrayList<Tuple> checkedTypes;
	
	private Boolean ordered;
	
	private ArrayList<String> attributeList;
	
	public enum resultCode { UPDATE, TIMEOVER, COMPLETE, FAIL } 
	
	public Rule( String name_ , long duration_ , Boolean ordered_, ArrayList<Tuple> types_) {
		name = name_;
		duration = duration_;
		ordered = ordered_;
		types = types_;
	}
	
	public resultCode check( Tuple type) {
		if( System.currentTimeMillis() < birthTime + duration ) {
			return resultCode.TIMEOVER;
		}
		if( checkedTypes.size() == types.size() ) {
		 return resultCode.COMPLETE;
		}
		if( ordered) {
			if( type.typeName == types.get( checkedTypes.size() ).typeName ) {
				if( attributeList.size() == 0 ) {
					return resultCode.UPDATE;
				} else {
					JSONParser parser = new JSONParser();
					try {
						JSONObject compareJson = (JSONObject)parser.parse(checkedTypes.get(0).content);
						JSONObject targetJson = (JSONObject)parser.parse(type.content);
						for( int i = 0 ; i < attributeList.size() ; i ++ ) {
							if( targetJson.get(attributeList.get(i)).toString() 
									!= compareJson.get(attributeList.get(i)).toString() ){
								return resultCode.FAIL;
							}
						}
						return resultCode.UPDATE;
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
			}
		} else {
			for( int j = 0; j < types.size(); j++ ) {
				if( types.get(j).typeName == type.typeName ) {
					for( int k = 0; k < checkedTypes.size(); k++ ) {
						if( checkedTypes.get(k).typeName == type.typeName ) {
							return resultCode.FAIL;
						}
					}
					if( attributeList.size() == 0 ) {
						return resultCode.UPDATE;
					} else {
						JSONParser parser = new JSONParser();
						try {
							JSONObject compareJson = (JSONObject)parser.parse(checkedTypes.get(0).content);
							JSONObject targetJson = (JSONObject)parser.parse(type.content);
							for( int i = 0 ; i < attributeList.size() ; i ++ ) {
								if( targetJson.get(attributeList.get(i)).toString() 
										!= compareJson.get(attributeList.get(i)).toString() ){
									return resultCode.FAIL;
								}
							}
							return resultCode.UPDATE;
						} catch (ParseException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		return resultCode.FAIL;
	 }
	
	public void update( Tuple type ) {
		if( checkedTypes.isEmpty() ) {
			birthTime = System.currentTimeMillis();
		}
		checkedTypes.add(type);
	}	

	
	public Boolean checkComplete() {
		return checkedTypes.size() == types.size();
	}
}
