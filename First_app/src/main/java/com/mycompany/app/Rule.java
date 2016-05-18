package com.mycompany.app;

import java.io.Serializable;
import java.util.ArrayList;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Rule implements Serializable{
	
	private String name;
	
	private long birthTime;
	
	private long duration;
	
	private ArrayList<Tuple> types;
	
	private ArrayList<Tuple> checkedTypes;
	
	private Boolean ordered;
	
	private ArrayList<String> attributeList;
	
	public enum resultCode { UPDATE, TIMEOVER, COMPLETE, FAIL } 
	
	public Rule( String name_ , long duration_ , Boolean ordered_, ArrayList<Tuple> types_,
			ArrayList<String> attributeList_) {
		name = name_;
		duration = duration_;
		ordered = ordered_;
		types = types_;
		birthTime = 0;
		checkedTypes = new ArrayList<Tuple>();
		attributeList = attributeList_;
	}
	
	public resultCode check( Tuple type) {
		if( checkedTypes.size() == types.size() ) {
			 return resultCode.COMPLETE;
		}
		if( birthTime != 0 && System.currentTimeMillis() > birthTime + duration ) {
			return resultCode.TIMEOVER;
		}
		if( ordered) {
			if( type.typeName.equals(types.get( checkedTypes.size() ).typeName )) {
				if( attributeList.size() == 0 ) {
					return resultCode.UPDATE;
				} else {
					JSONParser parser = new JSONParser();
					try {
						JSONObject compareJson = (JSONObject)parser.parse(checkedTypes.get(0).content);
						JSONObject targetJson = (JSONObject)parser.parse(type.content);
						for( int i = 0 ; i < attributeList.size() ; i ++ ) {
							if( !targetJson.get(attributeList.get(i)).toString().equals(
									compareJson.get(attributeList.get(i)).toString() )){
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
			ArrayList<Tuple> temp = new ArrayList<Tuple>(); 
			temp.addAll(types);
			for( int i = 0; i < types.size(); i++ ) {
				for( int j = 0; j < checkedTypes.size(); j++ ) {
					if( types.get(i).typeName.equals(checkedTypes.get(j).typeName )) {
						temp.remove(i);
					}
				}
			}
			for( int i = 0; i < temp.size(); i++ ) {
				if( temp.get(i).typeName.equals(type.typeName )) {
					if( attributeList.size() == 0 ) {
						return resultCode.UPDATE;
					} else {
						JSONParser parser = new JSONParser();
						try {
							JSONObject compareJson = (JSONObject)parser.parse(checkedTypes.get(0).content);
							JSONObject targetJson = (JSONObject)parser.parse(type.content);
							for( int j = 0 ; j < attributeList.size() ; j++ ) {
								if( !targetJson.get(attributeList.get(j)).toString().equals(
										compareJson.get(attributeList.get(j)).toString() )){
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
	
	public Boolean isBase() {
		return checkedTypes.size() == 0;
	}
	
	public ArrayList<Rule> removeFrom (ArrayList<Rule> rules) {
		for (Rule rule : rules) {
			if (rule.name.equals(this.name) && rule.birthTime == this.birthTime) {
				rules.remove(rule);
				break;
			}
		}
		return rules;
	}
}
