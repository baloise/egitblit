package com.baloise.egitblit.view.model;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.baloise.egitblit.gitblit.GitBlitRepository;

/**
 * ViewModel wrapping GitBlitProject (Git Repository entry)
 * 
 * @author MicBag
 * 
 */
public class ProjectViewModel implements GitBlitViewModel{

	private GitBlitRepository	repo;
	private GitBlitViewModel	parent;
	private String				toolTip;

	public ProjectViewModel(GitBlitRepository entry){
		if(entry == null){
			throw new IllegalArgumentException("Missing argument entry");
		}
		this.repo = entry;
	}

	public ProjectViewModel(GitBlitRepository entry, GitBlitViewModel parent){
		this(entry);
		setParent(parent);
	}

	public void setParent(GitBlitViewModel parent){
		this.parent = parent;
	}

	public GitBlitViewModel getParent(){
		return this.parent;
	}

	public String getName(){
		return repo.name;
	}

	public String getDescription(){
		return repo.description;
	}

	public List<String> getOwners(){
		return repo.owners;
	}

	public Date getLastChange(){
		return repo.lastChange;
	}

	public String getLastChangeAuthor(){
		return repo.lastChangeAuthor;
	}

	public boolean hasCommits(){
		return repo.hasCommits;
	}

	public boolean isShowRemoteBranches(){
		return repo.showRemoteBranches;
	}

	public boolean isUseIncrementalPushTags(){
		return repo.useIncrementalPushTags;
	}

	public String getIncrementalPushTagPrefix(){
		return repo.incrementalPushTagPrefix;
	}

	public String getAccessRestriction(){
		return repo.accessRestriction;
	}

	public String getAuthorizationControl(){
		return repo.authorizationControl;
	}

	public boolean isAllowAuthenticated(){
		return repo.allowAuthenticated;
	}

	public boolean isFrozen(){
		return repo.isFrozen;
	}

	public String getFederationStrategy(){
		return repo.federationStrategy;
	}

	public List<String> getFederationSets(){
		return repo.federationSets;
	}

	public boolean isFederated(){
		return repo.isFederated;
	}

	public boolean isSkipSizeCalculation(){
		return repo.skipSizeCalculation;
	}

	public boolean isSkipSummaryMetrics(){
		return repo.skipSummaryMetrics;
	}

	public String getFrequency(){
		return repo.frequency;
	}

	public boolean getBare(){
		return repo.isBare;
	}

	public String getOrigin(){
		return repo.origin;
	}

	public String getHead(){
		return repo.HEAD;
	}

	public List<String> getArailableRefs(){
		return repo.availableRefs;
	}

	public List<String> getInderxedBranches(){
		return repo.indexedBranches;
	}

	public String getSize(){
		return repo.size;
	}

	public List<String> getPreReceiveScrkipts(){
		return repo.preReceiveScripts;
	}

	public List<String> getPostReceiveStripts(){
		return repo.postReceiveScripts;
	}

	public List<String> getMailingLists(){
		return repo.mailingLists;
	}

	public Map<String, String> getCustomFields(){
		return repo.customFields;
	}

	public String getProjectPath(){
		return repo.projectPath;
	}

	public boolean isAllowForks(){
		return repo.allowForks;
	}

	public List<String> getForks(){
		return repo.forks;
	}

	public String getOriginRepository(){
		return repo.originRepository;
	}

	public boolean isVerifyComitter(){
		return repo.verifyCommitter;
	}

	public String getGCTreshold(){
		return repo.gcThreshold;
	}

	public int getGCPeriod(){
		return repo.gcPeriod;
	}

	public int getMaxActivityComits(){
		return repo.maxActivityCommits;
	}

	public List<String> getMetricAuthorExclusions(){
		return repo.metricAuthorExclusions;
	}

	public boolean isCollectionGarbage(){
		return repo.isCollectingGarbage;
	}

	public Date getLastGC(){
		return repo.lastGC;
	}

	public String getSDparklesShareId(){
		return repo.sparkleshareId;
	}

	// --- Passed or prepared attributes
	public String getGitUrl(){
		return repo.gitUrl;
	}

	public String getGroupName(){
		return repo.groupName;
	}

	public String getProjectName(){
		return repo.projectName;
	}

	public String getServerUrl(){
		return repo.serverUrl;
	}

	public long getByteSize(){
		return repo.byteSize;
	}

	public String getRepoColor(){
		return repo.repoRGB;
	}

	// public String getGitURL(){
	// return this.entry.gitUrl;
	// }
	//
	// public String getServerURL(){
	// return this.entry.serverUrl;
	//
	// }
	//
	// public String getProjectName(){
	// return this.entry.projectName;
	// }
	//
	// public String getGroupName(){
	// return this.entry.groupName;
	// }
	//
	// public boolean hasCommits(){
	// return this.entry.hasCommits;
	// }
	//
	// @Override
	// public String getName(){
	// return getProjectName();
	// }
	//
	// public String getDescription(){
	// return this.entry.description;
	// }
	//
	// public List<String> getOwners(){
	// return this.entry.owners;
	// }
	//
	// public Date getLastChange(){
	// return this.entry.lastChange;
	// }
	//
	// public Boolean isFrozen(){
	// return this.entry.isFrozen;
	// }
	//
	// public Boolean isFederated(){
	// return this.entry.isFederated;
	// }
	//
	// public Boolean isBare(){
	// return this.entry.isBare;
	// }
	//
	// public String getFrequency(){
	// return this.entry.frequency;
	// }
	//
	// public String getOriginRepository(){
	// return this.entry.originRepository;
	// }
	//
	// public String getSize(){
	// return this.entry.size;
	// }
	//
	// public List<String> getAvailableRefs(){
	// return this.entry.availableRefs;
	// }
	//
	// public String getLastChangeAuthor(){
	// return this.entry.lastChangeAuthor;
	// }
	//
	// public long getByteSize(){
	// return this.entry.byteSize;
	// }
	//
	// public String getHead(){
	// return this.entry.HEAD;
	// }
	//
	// public String getOrigin(){
	// return this.entry.origin;
	// }
	//
	// public boolean getAllowAuthenticated(){
	// return this.entry.allowAuthenticated;
	// }
	//
	// public boolean getAllowForks(){
	// return this.entry.allowForks;
	// }
	//
	// public String getProjectPath(){
	// return this.entry.projectPath;
	// }

	public void setToolTip(String msg){
		this.toolTip = msg;
	}

	public String getToolTip(){
		return this.toolTip;
	}


}
