package com.mycompany.app;

import java.io.Serializable;
import java.util.ArrayList;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


/**
 * This class is implemented to manage rules.
 * Rule called base rule is intanced when reading Rule.json file on begining of the program start,
 * And also when a base rule is including a input type in types list. 
 *  
 * 
 * @version 1.0 31 May 2016
 * @author CS409 CARPENTER (DAESEONG KIM, JAEYOUNG AHN, HYNHO HA)
 */
public class Rule implements Serializable{

	/** "name" is defined by user */
	private String name;

	/** 
	 * At first "birthTime" is initialized at 0.
	 * At moment when the rule has first "checkedTypes" element,
	 * "birthTime" is initialized at that time in mills.
	 */
	private long birthTime;

	/** 
	 * "duration" is defined by user.
	 * When some rules has "currentTime" > "birthTime" + "duration",
	 * The rules is time over rule.
	 */
	private long duration;

	/**
	 * "types" is defined by user.
	 * It means some types that rule has to filter.
	 */
	private ArrayList<Type> types;

	/**
	 * "checkedTypes" means what types are filtered in this rule.
	 * If "checkedTypes" has same elements with "types",
	 * then the rule is complete.
	 */
	private ArrayList<Type> checkedTypes;

	/**
	 * "ordered" is defined by user.
	 * It means whether rule's "types" should be checked by sequential or not. 
	 */
	private Boolean ordered;

	/**
	 * "attributeList" is defined by user.
	 * When checking a type in a rule,
	 * input type must have same attribute value with checkedTypes in "attributesList".
	 */
	private ArrayList<String> attributeList;

	/**
	 * RESULTCODE is returned when input type is checked.
	 * UPDATE means result that input type should be filtered.
	 * TIMEOVER means result that "currentTime" > "birthTime" + "duration".
	 * COMPLETE means result that input type makes the rule complete.
	 * FAIL means result that inputype has no effect on the rule.
	 */
	public enum RESULTCODE { UPDATE, TIMEOVER, COMPLETE, FAIL } 

	/**
	 * Constructor for Rule class.
	 * This constructor is used only when base rules are creating.
	 * @param name_ is rule's "name".
	 * @param duration_ is rule's "duration".
	 * @param ordered_ is boolean value that determine whether rule is ordered or not.
	 * @param types_ is rule's "types".
	 * @param attributeList_ is rule's "attributeList".
	 */
	public Rule( String name_ , long duration_ , Boolean ordered_, ArrayList<Type> types_,
			ArrayList<String> attributeList_) {
		name = name_;
		duration = duration_;
		ordered = ordered_;
		types = types_;
		birthTime = 0;
		checkedTypes = new ArrayList<Type>();
		attributeList = attributeList_;
	}

	/** Getter for "name". */
	public String getName() {
		return name;
	}

	/** Getter for "birthTime". */
	public long getBirthTime() {
		return birthTime;
	}

	/** Getter for "checkedTypes". */
	public ArrayList<Type> getCheckedTypes() {
		return checkedTypes;
	}

	/** Getter for "types". */
	public ArrayList<Type> getTypes() {
		return types;
	}

	/** Setter for "birthTime". */
	public void setBirthTime(long birthTime) {
		this.birthTime = birthTime;
	}

	/** Setter for "checkedTypes". */
	public void setCheckedTypes(ArrayList<Type> checkedTypes) {
		this.checkedTypes = checkedTypes;
	}

	/**
	 * Check rule for input type, and return RESULTCODE.
	 * @param input type
	 * @return RESULTCODE that indicating result of checking.
	 */
	public RESULTCODE check (Type type) {
		if((birthTime != 0) && (System.currentTimeMillis() > birthTime + duration) ) {
			return RESULTCODE.TIMEOVER; /* Check if the rule exceed its duration. */
		}
		if(checkedTypes.size() >= types.size()) {
			return RESULTCODE.COMPLETE; /* Check if the rule is already complete */
		}
		if(ordered) {
			/* When the rule is ordered rule */
			if(type.typeName.equals(types.get(checkedTypes.size()).typeName)) {
				/* When input type should be filtered. */
				
				if(isBase()) {
					/* 
					 * When the rule is base rule.
					 * There is no comparable attribute set. 
					 */
					return RESULTCODE.UPDATE; 
				}
				
				if(attributeList.size() == 0) {
					if(checkedTypes.size() >= types.size() - 1) {
						/* 
						 * Return COMPLETE when the rule is complete if input rule is updated to the rule.
						 * When "the rule + input type = complete rule"
						 */
						return RESULTCODE.COMPLETE; 
					}
					
					return RESULTCODE.UPDATE; /* The rule has no "attributeList" */
				} else {
					/* The rule has "attributeList" */	
					try {
						JSONParser parser = new JSONParser();
						JSONObject compareJson = (JSONObject)parser.parse(checkedTypes.get(0).content);
						JSONObject targetJson = (JSONObject)parser.parse(type.content);
						for( int i = 0 ; i < attributeList.size() ; i ++ ) {
							if( !targetJson.get(attributeList.get(i)).toString().equals(
									compareJson.get(attributeList.get(i)).toString() )){
								return RESULTCODE.FAIL; /* Input type has different attributes values. */
							}
						}
						
						if(checkedTypes.size() >= types.size() - 1) {
							/* 
							 * Return COMPLETE when the rule is complete if input rule is updated to the rule.
							 * When "the rule + input type = complete rule"
							 */
							return RESULTCODE.COMPLETE; 
						}
						
						return RESULTCODE.UPDATE; /* Input Type has same attributes values. */
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
			}
		} else {
			/* When the rule is not ordered rule */
			
			/* To make list of "types" - "checkedTypes". */
			ArrayList<Type> temp1 = new ArrayList<Type>(); /* "types" - "checkedTypes" */
			ArrayList<Type> temp2 = new ArrayList<Type>();
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
				App.logger.error("temp1.size() != types.size() - checkedTypes.size()");
			}

			/* Check if input type is included in temp1. */
			for(int i=0; i<temp1.size(); i++) {
				if(temp1.get(i).typeName.equals(type.typeName)) {
					/* When input type should be filtered. */
					if(isBase()) {
						/* 
						 * When the rule is base rule.
						 * There is no comparable attribute set. 
						 */
						if(checkedTypes.size() >= types.size() - 1) {
							/* 
							 * Return COMPLETE when the rule is complete if input rule is updated to the rule.
							 * When "the rule + input type = complete rule"
							 */
							return RESULTCODE.COMPLETE; 
						}
						return RESULTCODE.UPDATE;
					}
					
					if(attributeList.size() == 0) {
						return RESULTCODE.UPDATE; /* The rule has no "attributeList" */
					} else {
						try {
							JSONParser parser = new JSONParser();
							JSONObject compareJson = (JSONObject)parser.parse(checkedTypes.get(0).content);
							JSONObject targetJson = (JSONObject)parser.parse(type.content);
							for( int j = 0 ; j < attributeList.size() ; j++ ) {
								if( !targetJson.get(attributeList.get(j)).toString().equals(
										compareJson.get(attributeList.get(j)).toString() )){
									return RESULTCODE.FAIL; /* Input type has different attributes values. */
								}
							}
							if(checkedTypes.size() >= types.size() - 1) {
								/* 
								 * Return COMPLETE when the rule is complete if input rule is updated to the rule.
								 * When "the rule + input type = complete rule"
								 */
								return RESULTCODE.COMPLETE;
							}
							return RESULTCODE.UPDATE; /* Input Type has same attributes values. */
						} catch (ParseException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		return RESULTCODE.FAIL;
	}

	/**
	 * The "checkedTypes" of the rule add input type.
	 * If the rule is base rule, it sets birthTime. 
	 * @param type
	 */
	public void update( Type type ) {
		if( checkedTypes.isEmpty() ) {
			birthTime = System.currentTimeMillis();
		}
		checkedTypes.add(type);
	}	

	/** @return boolean value that determine whether the rule is already complete.*/
	public Boolean checkComplete() {
		return checkedTypes.size() >= types.size();
	}

	/** @return boolean value that determine if the rule is base rule or not. */
	public Boolean isBase() {
		return checkedTypes.isEmpty();
	}

	/**
	 * @return cloned rule.
	 */
	public Rule ruleClone() {
		String _name = name;
		long _birthTime = birthTime;
		long _duration = duration;

		ArrayList<Type> _types = new ArrayList<Type>();
		for (Type t : types) {
			_types.add(t.typeClone());
		}

		ArrayList<Type> _checkedTypes = new ArrayList<Type>();
		for (Type t : checkedTypes) {
			_checkedTypes.add(t.typeClone());
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