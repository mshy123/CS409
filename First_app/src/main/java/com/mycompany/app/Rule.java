package com.mycompany.app;

import java.util.ArrayList;
import java.util.Date;

public class Rule {
	
	private String name;
	
	private long birthTime;
	
	private long duration;
	
	private ArrayList<Tuple> types;
	
	private ArrayList<Tuple> checkedTypes;
	
	private Boolean ordered;
	
	private ArrayList<String> attributeList;
	
	public Rule( String name_ , long duration_ , Boolean ordered_, ArrayList<Tuple> types_) {
		name = name_;
		duration = duration_;
		ordered = ordered_;
		types = types_;
	}
	
	public Boolean check( Tuple type) {
		if( ordered) {
			if( type.typeName == types.get( checkedTypes.size() ).typeName ) {
				return true;
			}
		} else {
			if( types.contains( type.typeName ) && !checkedTypes.contains( type.typeName ) ) {
				return true;
			}
		}
		return false;
	}
	
	public void update( Tuple type ) {
		//updat 할 때 쳌트 타입스에 하나도 없으면 벌스타임을 추가하면 될듯?
		if( checkedTypes.isEmpty() ) {
			birthTime = System.currentTimeMillis();
		}
		checkedTypes.add(type);
	}	
	
	public Boolean checkComplete() {
		return checkedTypes.size == types.size();
	}
}
