package com.baloise.egitblit.gitblit;

import java.util.List;

import com.baloise.egitblit.pref.CloneSettings;
import com.baloise.egitblit.pref.CloneSettings.CloneProtocol;

/**
 * Represents one Config Entry (one GitBlit Repo entry)
 * @author MicBag
 *
 */
public class GitBlitServer{
	// Server settings
	public String url;
	public String user;
	public String password;
	public CloneSettings cloneSettings;
	

	// Status / Confiig fields
	public boolean active = true;
	public boolean serverError = false;
	
	private List<GitBlitRepository> projectList;
	
  public GitBlitServer(String url, boolean active, String user, String pwd, CloneSettings cls){
    this.url = url;
    this.user = user;
    this.password = pwd;
    this.active = active;
    this.cloneSettings = cls; 
  }

  public GitBlitServer(String url, boolean active, String user, String pwd){
    this(url,active,user,pwd,new CloneSettings());
	}
  
  public CloneSettings getCloneSettings(){
    return this.cloneSettings;
  }
  
  public CloneSettings setCloneSettings(CloneProtocol prot, Integer port){
    CloneSettings set = new CloneSettings(prot,port);
    setCloneSettings(set);
    return set;
  }
  public void setCloneSettings(CloneSettings set){
    this.cloneSettings = set;
  }
	
	public GitBlitServer(){
		this(null,true,null,null);
	}

	public void addProject(GitBlitRepository proj){
		this.projectList.add(proj);
	}
	
	public List<GitBlitRepository> getProjects(){
		return this.projectList;
	}
	
	public boolean removeProject(GitBlitRepository proj){
		return this.projectList.remove(proj);
	}

	public void clearProjects(){
		this.projectList.clear();
	}
	
	@Override
	public int hashCode(){
		final int prime = 31;
		int result = 1;
		result = prime * result + ((url == null) ? 0 : url.hashCode());
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
		GitBlitServer other = (GitBlitServer) obj;
		if(url == null){
			if(other.url != null)
				return false;
		}else if(!url.equals(other.url))
			return false;
		return true;
	}
}