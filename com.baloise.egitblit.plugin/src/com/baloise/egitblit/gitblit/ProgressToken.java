package com.baloise.egitblit.gitblit;

/**
 * Like Eclipse ProgressMonitor: For indicating start/end of a process
 * This class has been created, because this package (layer) should not use Eclipse classes
 *  
 * @author MicBag
 *
 */
public abstract class ProgressToken{

	public abstract void startWork(String msg);
	public abstract void endWork();
	
	public ProgressToken(){};
}
