package com.baloise.egitblit.view.model;

public interface GitBlitViewModel{

	public String getName();
	public String getToolTip();
	public void setParent(GitBlitViewModel model);
	public GitBlitViewModel getParent();
}
