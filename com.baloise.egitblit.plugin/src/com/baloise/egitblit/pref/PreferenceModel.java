package com.baloise.egitblit.pref;

import java.util.ArrayList;
import java.util.List;

import com.baloise.egitblit.gitblit.GitBlitServer;
import com.baloise.egitblit.main.EclipseHelper;

/**
 * Model representing the preference settings
 * @author MicBag
 *
 */
public class PreferenceModel{

	public static enum DoubleClickBehaviour{
			OpenGitBlit(0),
			CopyUrl(1),
			PasteEGit(2);
			
			
		public final int value;
		DoubleClickBehaviour(int value){
			this.value = value;
		}
		
		public static DoubleClickBehaviour getValue(int val){
			DoubleClickBehaviour[] values = DoubleClickBehaviour.values();
			for(DoubleClickBehaviour item : values){
				if(item.value == val){
					return item;
				}
			}
			return null;
		}
	};
	
	private DoubleClickBehaviour dbClick = DoubleClickBehaviour.PasteEGit;
	private List<GitBlitServer> repoList = new ArrayList<GitBlitServer>();
	private boolean omitServerErrors = false;
	
	public PreferenceModel(){
	}
	
	public DoubleClickBehaviour getDoubleClick(){
		return this.dbClick;
	}
	
	public void setDoubleClick(DoubleClickBehaviour dbcl){
		if(dbcl == null){
			return;
		}
		this.dbClick = dbcl;
	}
	
	public void setRepoList(List<GitBlitServer> repoList){
		this.repoList.addAll(repoList);
	}
	
	public List<GitBlitServer> getServerList(){
		return this.repoList;
	}
	public void addRepository(GitBlitServer repo){
		if(repo == null){
			return;
		}
		if(this.repoList.contains(repo)){
			EclipseHelper.logError("Can´t add repository location "  + repo.url + ". A repository with this url already exists.");
			return;
		}
		this.repoList.add(repo);
	}

	public void addRepository(String url, boolean active, String user, String pwd){
		addRepository(new GitBlitServer(url,active, user,pwd));
	}

	public boolean removeRepository(GitBlitServer repo){
		if(repo == null){
			return false;
		}
		return this.repoList.remove(repo);
	}

	public void setOmitServerErrors(boolean val){
		this.omitServerErrors = val;
	}
	public boolean isOmitServerErrors(){
		return this.omitServerErrors;
	}

}
