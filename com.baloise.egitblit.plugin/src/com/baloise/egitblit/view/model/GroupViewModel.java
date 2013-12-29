package com.baloise.egitblit.view.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Model for displaying a gitblit group in the tree viewer
 * @author Mike
 *
 */
public class GroupViewModel implements GitBlitViewModel{

	private String groupName;
	private GitBlitViewModel parent;
	
	private List<GitBlitViewModel> childList = new ArrayList<GitBlitViewModel>();
	
	public GroupViewModel(String groupName){
		this(groupName,(GitBlitViewModel)null);
	}
	
	public GroupViewModel(String groupName,GitBlitViewModel model){
		this.groupName = groupName;
		if(model != null){
			this.childList.add(model);
		}
	}

	public GroupViewModel(String groupName,List<GitBlitViewModel> list){
		this.groupName = groupName;
		if(list != null){
			this.childList.addAll(list);
		}
	}

	public void addChild(GitBlitViewModel model){
		this.childList.add(model);
	}
	
	public List<GitBlitViewModel> getChilds(){
		return this.childList;
	}
	
	@Override
	public String getName(){
		return this.groupName;
	}
	
	public void setParent(GitBlitViewModel parent){
		this.parent = parent;
	}
	
	public GitBlitViewModel getParent(){
		return this.parent;
	}

	@Override
	public String getToolTip(){
		return null;
	}

	@Override
	public int hashCode(){
		final int prime = 31;
		int result = 1;
		result = prime * result + ((groupName == null) ? 0 : groupName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj){
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())
			return false;
		GroupViewModel other = (GroupViewModel) obj;
		if(groupName == null){
			if(other.groupName != null)
				return false;
		}else if(!groupName.equals(other.groupName))
			return false;
		return true;
	}

}
