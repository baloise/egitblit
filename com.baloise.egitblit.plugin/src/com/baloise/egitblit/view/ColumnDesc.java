package com.baloise.egitblit.view;

public enum ColumnDesc{
	
	Repository(0),
	Description(1),
	Owner(2),
	LastChange(3),
	Size(4),
	Server(5),
	Group(6);
	
	public int pos;
	
	ColumnDesc(int pos){
		this.pos = pos;
	}
	
	public final static ColumnDesc getColumnDesc(int pos){
		ColumnDesc[] list = ColumnDesc.values();
		for(ColumnDesc item : list){
			if(item.pos == pos){
				return item;
			}
		}
		return null;
	}
	
	public final String getSortKey(){
		return "sortvalue_" + name();
	}
}
