package com.baloise.egitblit.pref;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.ui.IMemento;

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
		public final static String KEY_GITBLIT_COLUMN_DESC_POS = "column.desc.pos";
		public final static String KEY_GITBLIT_COLUMN_DESC_WIDTH = "column.desc.width";
		public final static String KEY_GITBLIT_COLUMN_DESC_VISIBLE = "column.desc.visible";

		public String id;
		public int pos;
		public int width;
		public boolean visible;
		
		public ColumnData(){
		}
		
		public ColumnData(String id, int pos, boolean visible, int width){
			this.id = id;
			this.pos = pos;
			this.visible = visible;
			this.width = width;
		}
		
		public void saveState(IMemento memento){
			if(memento == null){
				Activator.logError("Error saving column state. Missing call parameter.");
				return;
			}
			
			memento.putInteger(makeKey(KEY_GITBLIT_COLUMN_DESC_POS), this.pos);
			memento.putInteger(makeKey(KEY_GITBLIT_COLUMN_DESC_WIDTH), this.width);
			memento.putBoolean(makeKey(KEY_GITBLIT_COLUMN_DESC_VISIBLE), this.visible);
		}
		
		public void loadState(IMemento memento){
			if(memento == null){
				Activator.logError("Error saving column state. Missing call parameter.");
				return;
			}
			Integer val;
			Boolean b;
			val = memento.getInteger(makeKey(KEY_GITBLIT_COLUMN_DESC_POS));
			this.pos = val != null ? val :  -1;
			val = memento.getInteger(makeKey(KEY_GITBLIT_COLUMN_DESC_WIDTH));
			this.width = val != null ? val : 0;
			b = memento.getBoolean(makeKey(KEY_GITBLIT_COLUMN_DESC_VISIBLE));
			this.visible = b != null && this.width > 0 ? b : false;
		}
		
		private String makeKey(String propId) {
			return this.id + "." + propId;
		}

		@Override
		public String toString(){
			return "ColumnData [id=" + id + ", pos=" + pos + ", width=" + width + ", visible=" + visible + "]";
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
		this.dbClick = DoubleClickBehaviour.PasteEGit;
		this.repoList = new ArrayList<GitBlitServer>();
		this.omitServerErrors = false;
		this.colorColums = false;
		this.showGroups = true;
		this.colData = new ArrayList<ColumnData>();
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

	public ColumnData putColumnData(String id, int pos, boolean visible, int width){
		ColumnData desc = getColumnData(id);
		if(desc == null){
			desc = new ColumnData();
			this.colData.add(desc);
		}
		desc.id = id;
		desc.pos = pos;
		desc.width = width;
		desc.visible = visible;
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

	@Override
	public String toString(){
		return "PreferenceModel [dbClick=" + dbClick + ", repoList=" + repoList + ", omitServerErrors=" + omitServerErrors + ", colorColums=" + colorColums + ", showGroups=" + showGroups + ", colDesc=" + colData + "]";
	}
}
