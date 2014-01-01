package com.baloise.egitblit.main;

public enum SharedImages{

	CloneGitOneClick	("CloneGitOneClick",	"Clone & Import", "cloneGitOneClick.gif"),
	ExpandAll			("ExpandAll",			"Expand all",	  "expandall.gif"),
	GitIconBlack		("GitIconBlack",		"Git",			  "git-black-16x16.png"),
	Refresh				("Refresh",				"Refresh",		  "refresh_tab.gif"),
	TreeMode			("TreeMode",			"Tree mode",	  "tree_mode.gif");

	
	public String ID;
	public String label;
	public String filename;
	
	SharedImages(String id, String label, String fileName){
		this.ID = "com.baloise.egitblit.images." + id;
		this.label = label;
		this.filename = fileName;
	}
	
	public String getImagePath(){
		return "icons/" + filename;
	}
}
