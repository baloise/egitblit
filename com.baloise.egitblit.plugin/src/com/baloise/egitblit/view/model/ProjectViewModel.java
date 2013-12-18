package com.baloise.egitblit.view.model;

import com.baloise.egitblit.common.GitBlitRepository;

/**
 * ViewModel wrapping GitBlitProject
 * @author MicBag
 *
 */
public class ProjectViewModel implements GitBlitViewModel{

	private GitBlitRepository entry;
	private GitBlitViewModel parent;
	
	public ProjectViewModel(GitBlitRepository entry, GitBlitViewModel parent){
		this.entry = entry;
		setParent(parent);
	}
	
	public void setParent(GitBlitViewModel parent){
		this.parent = parent;
	}
	
	public GitBlitViewModel getParent(){
		return this.parent;
	}
	

	public ProjectViewModel(GitBlitRepository entry){
		this.entry = entry;
	}
	
	public String getGitURL(){
		if(this.entry == null){
			return null;
		}
		return this.entry.gitUrl;
	}
	
	public String getServerURL(){
		if(this.entry == null){
			return null;
		}
		return this.entry.serverUrl;
		
	}

	public String getProjectName(){
		if(this.entry == null){
			return null;
		}
		return this.entry.projectName;
	}
	
	public boolean hasCommits(){
		if(this.entry == null){
			return false;
		}
		return this.entry.hasCommits;
	}
	
	@Override
	public String getName(){
		return getProjectName();
	}

	public String getDescription(){
		if(this.entry == null){
			return null;
		}
		return this.entry.description;
	}
}
