package com.baloise.egitblit.view.model;

public interface GitBlitViewModel{

	public String getName();
	
	public void setParent(GitBlitViewModel model);
	public GitBlitViewModel getParent();
}
