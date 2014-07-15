package com.baloise.egitblit.pref;

import java.util.ArrayList;
import java.util.List;

import com.baloise.egitblit.gitblit.GitBlitServer;
import com.baloise.egitblit.main.Activator;
import com.baloise.egitblit.view.ColumnData;

/**
 * Model representing the preference settings
 * @author MicBag
 *
 */
public class PreferenceModel{

	/**
	 * DoubleClick behaviour
	 * @author MicBag
	 *
	 */
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
     *
     */
    public static enum CloneProtocol{
        HTTP(0),
        SSH(1);

        public final int value;
        CloneProtocol(int value){
            this.value = value;
        }

        public static CloneProtocol getValue(int val){
            CloneProtocol[] values = CloneProtocol.values();
            for(CloneProtocol item : values){
                if(item.value == val){
                    return item;
                }
            }
            return null;
        }
    }
	
	// ------------------------------------------------------------------------
	// Properties
	// ------------------------------------------------------------------------
	private DoubleClickBehaviour dbClick;
    private CloneProtocol cloneProtocol;
	private List<GitBlitServer> serverList;
	private boolean omitServerErrors;
	private boolean decorateView;
	private boolean showGroups;
	private List<ColumnData> colData;

	private boolean wsGroupName = true;
	
	public PreferenceModel(){
		reset();
	}
	
	public void reset(){
		this.dbClick = DoubleClickBehaviour.PasteEGit;
        this.cloneProtocol = CloneProtocol.HTTP;
		this.serverList = new ArrayList<GitBlitServer>();
		this.omitServerErrors = false;
		this.decorateView = false;
		this.showGroups = true;
		this.colData = new ArrayList<ColumnData>();

		this.wsGroupName = true;
	}
	
	public void init(PreferenceModel model){
		if(model == null){
			return;
		}
		reset();
		
		this.dbClick = model.dbClick;
        this.cloneProtocol = model.cloneProtocol;
		this.serverList.addAll(model.serverList);
		this.omitServerErrors = model.omitServerErrors;
		this.decorateView = model.decorateView;
		this.showGroups = model.showGroups;
		this.colData.addAll(model.colData);
		
		this.wsGroupName = model.wsGroupName;
	}

	
	public boolean isWSGroupNameEnabled(){
	  return this.wsGroupName;
	}
	
	public void setWSGroupNameEnabled(boolean enabled){
	  this.wsGroupName = enabled;
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

    public CloneProtocol getCloneProtocol() {
        return this.cloneProtocol;
    }

    public void setCloneProtocol(CloneProtocol cloneProtocol) {
        if (cloneProtocol == null) {
            return;
        }
        this.cloneProtocol = cloneProtocol;
    }

	public void setServerList(List<GitBlitServer> repoList){
		this.serverList.clear();
		this.serverList.addAll(repoList);
	}
	
	public List<GitBlitServer> getServerList(){
		return this.serverList;
	}
	public void addRepository(GitBlitServer repo){
		if(repo == null){
			return;
		}
		if(this.serverList.contains(repo)){
			Activator.logError("Can�t add repository location "  + repo.url + ". A repository with this url already exists.");
			return;
		}
		this.serverList.add(repo);
	}

	public void addRepository(String url, boolean active, String user, String pwd, Integer sshPort){
		addRepository(new GitBlitServer(url,active, user,pwd, sshPort));
	}

	public boolean removeRepository(GitBlitServer repo){
		if(repo == null){
			return false;
		}
		return this.serverList.remove(repo);
	}
	
	public void clearServerList(){
		this.serverList.clear();
	}

	
	public void setDecorateView(boolean yesNo){
		this.decorateView = yesNo;
	}
	
	public boolean isDecorateView(){
		return this.decorateView;
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
		return "PreferenceModel [dbClick=" + dbClick + ", serverList=" + serverList + ", omitServerErrors=" + omitServerErrors + ", decorateView=" + decorateView + ", showGroups=" + showGroups + ", colData=" + colData + ", wsGroupName=" + wsGroupName + "]";
	}

}
