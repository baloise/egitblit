package com.baloise.egitblit.gitblit;

public abstract class ProgressToken{

	public abstract void startWork(String msg);
	public abstract void endWork();
	
	public ProgressToken(){};
}
