package cs_project;

import java.util.ArrayList;
import java.util.Date;

public class Rule {
	
	private String name;
	
	private long birthTime;
	
	private int duration;
	
	private ArrayList<Tuple> types;
	
	private ArrayList<String> checkedTypes;
	
	private Boolean ordered;
	
	private ArrayList<String> attributeList;
	
	public Rule( String name_ , int duration_ , Boolean ordered_ ) {
		name = name_;
		duration = duration_;
		ordered = ordered_;
	}
	
	public Boolean check( Rule rule1) {
		return true;
	}
	
	public void update( String type ) {
		//updat 할 때 쳌트 타입스에 하나도 없으면 벌스타임을 추가하면 될듯?
		if( checkedTypes.isEmpty() ) {
			birthTime = System.currentTimeMillis();
		}
	}
}
