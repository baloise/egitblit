package com.baloise.egitblit.pref;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.baloise.egitblit.gitblit.GitBlitServer;
import com.baloise.egitblit.main.Activator;

/**
 * Model representing the preference settings
 * @author MicBag
 *
 */
public class PreferenceModel{

	public static enum DoubleClickBehaviour{
			OpenGitBlit(0),
			CopyUrl(1),
			PasteEGit(2),
			CloneImport(3);
			
			
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
	
	/**
	 * Class holding column configuration (id, position and width)
	 * @author MicBag
	 *
	 */
	public static class ColumnData{
		public String id;
		public int pos;
		public int width;
		
		public ColumnData(){
		}
		
		public ColumnData(String id, int pos, int width){
			this.id = id;
			this.pos = pos;
			this.width = width;
		}
		
		@Override
		public String toString(){
			return "ColDesc [id=" + id + ", pos=" + pos + ", width=" + width + "]";
		}
	}

	// ------------------------------------------------------------------------
	// Properties
	// ------------------------------------------------------------------------
	private DoubleClickBehaviour dbClick;
	private List<GitBlitServer> repoList;
	private boolean omitServerErrors;
	private boolean colorColums;
	private boolean showGroups;
	private List<ColumnData> colData;
	
	public PreferenceModel(){
		reset();
	}
	
	public void reset(){
		dbClick = DoubleClickBehaviour.PasteEGit;
		repoList = new ArrayList<GitBlitServer>();
		omitServerErrors = false;
		colorColums = false;
		showGroups = true;
		colData = new ArrayList<ColumnData>();
	}
	
	public void init(PreferenceModel model){
		if(model == null){
			return;
		}
		reset();
		this.dbClick = model.dbClick;
		this.repoList.addAll(model.repoList);
		this.omitServerErrors = model.omitServerErrors;
		this.colorColums = model.colorColums;
		this.showGroups = model.showGroups;
		this.colData.addAll(model.colData);
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
			Activator.logError("Can�t add repository location "  + repo.url + ". A repository with this url already exists.");
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
	
	public void setColorColumns(boolean yesNo){
		this.colorColums = yesNo;
	}
	
	public boolean isColorColumns(){
		return this.colorColums;
	}

	public void setOmitServerErrors(boolean val){
		this.omitServerErrors = val;
	}
	public boolean isOmitServerErrors(){
		return this.omitServerErrors;
	}
	
	public void setShowGroups(boolean val){
		this.showGroups = val;
	}
	
	public boolean isShowGroups(){
		return this.showGroups;
	}

	public ColumnData putColumnData(String id, int pos, int width){
		ColumnData desc = getColumnData(id);
		if(desc == null){
			desc = new ColumnData();
			this.colData.add(desc);
		}
		desc.id = id;
		desc.pos = pos;
		desc.width = width;
		return desc;
	}
	
	public void clearColumnData(){
		this.colData.clear();
	}

	public void removeColumnData(String id){
		ColumnData desc = getColumnData(id);
		if(desc != null){
			this.colData.remove(desc);
		}
	}
	
	public final static ColumnData getColumnData(List<ColumnData> list, String id){
		for(ColumnData item : list){
			if(item.id.equalsIgnoreCase(id)){
				return item;
			}
		}
		return null;
	}

	public ColumnData getColumnData(String id){
		return getColumnData(this.colData, id);
	}

	public ColumnData getColumnData(int pos){
		for(ColumnData item : this.colData){
			if(item.pos == pos){
				return item;
			}
		}
		return null;
	}

	public List<ColumnData> getColumnData(){
		return new ArrayList<ColumnData>(this.colData);
	}
	
	public void setColumnData(List<ColumnData> list){
		this.colData.clear();
		this.colData.addAll(list);
	}

	/**
	 * ...Just for debugging purpose
	 * @param ColumnDat list list to be sorted by position
	 */
	public final static void sortByOrder(List<ColumnData> list){
		if(list == null){
			return;
		}
		Collections.sort(list,new Comparator<ColumnData>() {
			@Override
			public int compare(ColumnData o1, ColumnData o2){
				return new Integer(o1.pos).compareTo(o2.pos);
			}
		});
	}
	
	@Override
	public String toString(){
		return "PreferenceModel [dbClick=" + dbClick + ", repoList=" + repoList + ", omitServerErrors=" + omitServerErrors + ", colorColums=" + colorColums + ", showGroups=" + showGroups + ", colDesc=" + colData + "]";
	}
}
