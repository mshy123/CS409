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
	
	public enum RESULTCODE { UPDATE, TIMEOVER, COMPLETE, FAIL } 
	
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
	
	public String getName() {
		return name;
	}

	public long getBirthTime() {
		return birthTime;
	}

	public ArrayList<Tuple> getCheckedTypes() {
		return checkedTypes;
	}
	
	public ArrayList<Tuple> getTypes() {
		return types;
	}

	
	
	public void setBirthTime(long birthTime) {
		this.birthTime = birthTime;
	}

	public void setCheckedTypes(ArrayList<Tuple> checkedTypes) {
		this.checkedTypes = checkedTypes;
	}

	public RESULTCODE check( Tuple type) {
		if( birthTime != 0 && System.currentTimeMillis() > birthTime + duration ) {
			return RESULTCODE.TIMEOVER;
		}
		if( checkedTypes.size() >= types.size()) {
			 return RESULTCODE.COMPLETE;
		}
		if( ordered) {
			if( type.typeName.equals(types.get( checkedTypes.size() ).typeName )) {
				if( checkedTypes.size() >= types.size() - 1 ) {
					 return RESULTCODE.COMPLETE;
				}
				if( attributeList.size() == 0 ) {
					return RESULTCODE.UPDATE;
				} else {
					JSONParser parser = new JSONParser();
					if(checkedTypes.size() == 0) {
						return RESULTCODE.UPDATE;
					}
					try {
						JSONObject compareJson = (JSONObject)parser.parse(checkedTypes.get(0).content);
						JSONObject targetJson = (JSONObject)parser.parse(type.content);
						for( int i = 0 ; i < attributeList.size() ; i ++ ) {
							if( !targetJson.get(attributeList.get(i)).toString().equals(
									compareJson.get(attributeList.get(i)).toString() )){
								return RESULTCODE.FAIL;
							}
						}
						return RESULTCODE.UPDATE;
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
			}
		} else {
			if( checkedTypes.size() >= types.size() ) {
				 return RESULTCODE.COMPLETE;
			}
			ArrayList<Tuple> temp1 = new ArrayList<Tuple>();
			ArrayList<Tuple> temp2 = new ArrayList<Tuple>();
			temp1.addAll(types);
			temp2.addAll(checkedTypes);
			int idx1 = 0;
			int idx2 = 0;
			boolean temp = false;
			
			while (idx1 < temp1.size()) {
				idx2 = 0;
				temp = false;
				while (idx2 < temp2.size()) {
					if (temp1.get(idx1).typeName.equals(temp2.get(idx2).typeName)) {
						temp1.remove(idx1);
						temp2.remove(idx2);
						temp = true;
						break;
					}
					idx2++;
				}
				if (temp) continue;
				else idx1++;
			}
			
			if (temp1.size() != types.size() - checkedTypes.size()) {
				App.logger.error("!!DANGER!! You cannot arrive here!!!");
			}
			
			for( int i = 0; i < temp1.size(); i++ ) {
				if( temp1.get(i).typeName.equals(type.typeName )) {
					if( checkedTypes.size() >= types.size() - 1 ) {
						 return RESULTCODE.COMPLETE;
					}
					if( attributeList.size() == 0 ) {
						return RESULTCODE.UPDATE;
					} else {
						JSONParser parser = new JSONParser();
						if(checkedTypes.size() == 0) {
							return RESULTCODE.UPDATE;
						}
						try {
							JSONObject compareJson = (JSONObject)parser.parse(checkedTypes.get(0).content);
							JSONObject targetJson = (JSONObject)parser.parse(type.content);
							for( int j = 0 ; j < attributeList.size() ; j++ ) {
								if( !targetJson.get(attributeList.get(j)).toString().equals(
										compareJson.get(attributeList.get(j)).toString() )){
									return RESULTCODE.FAIL;
								}
							}
							if( checkedTypes.size() >= types.size() - 1 ) {
								 return RESULTCODE.COMPLETE;
							}
							return RESULTCODE.UPDATE;
						} catch (ParseException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		return RESULTCODE.FAIL;
	 }
	
	public void update( Tuple type ) {
		if( checkedTypes.isEmpty() ) {
			birthTime = System.currentTimeMillis();
		}
		checkedTypes.add(type);
	}	

	
	public Boolean checkComplete() {
		return checkedTypes.size() >= types.size();
	}
	
	public Boolean isBase() {
		return checkedTypes.size() == 0;
	}
	
	public Rule ruleClone() {
		String _name = name;
		long _birthTime = birthTime;
		long _duration = duration;
		
		ArrayList<Tuple> _types = new ArrayList<Tuple>();
		for (Tuple t : types) {
			_types.add(t.tupleClone());
		}
		
		ArrayList<Tuple> _checkedTypes = new ArrayList<Tuple>();
		for (Tuple t : checkedTypes) {
			_checkedTypes.add(t.tupleClone());
		}
		
		boolean _ordered = ordered;

		ArrayList<String> _attributedList = new ArrayList<String>();
		_attributedList.addAll(attributeList);
		
		
		Rule r = new Rule(_name, _duration, _ordered, _types, _attributedList); 
		r.setBirthTime(_birthTime);
		r.setCheckedTypes(_checkedTypes);
		
		return r;
	}
}