package com.baloise.egitblit.pref;

public enum CloneProtocol{
	HTTPS("https", 8443, 0), 
	HTTP("http", 8080, 1), 
	SSH("ssh", 29418, 2), 
	GIT("git", 9418, 3), 
	File("file", null, 4), 
	FTP("ftp", 21, 5), 
	SFTP("sftp", 29418, 6);

	public final String schema;
	public final Integer defaultPort;
	public final int index;

	CloneProtocol(String schema, Integer defPort, int index){
		this.schema = schema;
		this.defaultPort = defPort;
		this.index = index;
	}

	public static CloneProtocol getByIndex(int index){
		CloneProtocol items[] = values();
		if(index < 0 || index > items.length){
			return null;
		}
		for(CloneProtocol item : items){
			if(item.index == index){
				return item;
			}
		}
		return null;
	}

	public static String[] getDisplayValues(){
		CloneProtocol items[] = values();
		String res[] = new String[items.length];
		for(CloneProtocol item : items){
			res[item.index] = item.schema;
		}
		return res;
	}

	public static CloneProtocol getValue(String schema){
		if(schema == null || schema.trim().isEmpty()){
			return null;
		}
		CloneProtocol items[] = values();
		for(CloneProtocol item : items){
			if(item.schema.equalsIgnoreCase(schema)){
				return item;
			}
		}
		return null;
	}
}