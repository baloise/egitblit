package com.baloise.egitblit.gitblit;

import com.gitblit.models.RepositoryModel;

/**
 * Represents a GitBlit Repository 
 * 
 * @author MicBag
 * 
 */
public class GitBlitRepository {

	public static final String GROUP_MAIN = "main";
	public String serverUrl;
	public String gitUrl;
	public String groupName;
	public String projectName;
	public String projectPath;
	public boolean hasCommits;
	public String description;
	// ....more properties to come
	
	public GitBlitRepository(){
	}

	/**
	 * Factory method for createing an GitBlitProject Model
	 * @param gitUUrl Git Url of reposiotry
	 * @param model GitBlit RepositoryModel object
	 * @return created GitBlitProject
	 */
	public static GitBlitRepository create(String serverUrl, String gitUrl, RepositoryModel model){
		GitBlitRepository repo = new GitBlitRepository();

		repo.gitUrl = gitUrl;
		repo.groupName = getGroupName(model);

		repo.projectName = getProjectName(model);
		repo.projectPath = model.projectPath;
		repo.hasCommits = model.hasCommits;
		repo.description = model.description;
		repo.serverUrl = serverUrl;
		return repo;
	}

	/**
	 * Extracts the group name
	 * @param model repository which group name should be extracted
	 * @return the group name 
	 */
	private final static String getGroupName(RepositoryModel model){
		if(model == null){
			return null;
		}
		String value = stripDotGit(model.name);
		if(value == null){
			return null;
		}
		int pos = value.lastIndexOf("/");
		if(pos == -1){
			return GROUP_MAIN;
		}
		return value.substring(0, pos);
	}

	/**
	 * Extracts the project/repository name from the passed repository model
	 * @param model repository which name should be extracted
	 * @return name of the repository / project name
	 */
	private final static String getProjectName(RepositoryModel model){
		if(model == null){
			return null;
		}
		String value = stripDotGit(model.name);
		if(value == null){
			return null;
		}
		int pos = value.lastIndexOf("/");
		if(pos == -1){
			return value;
		}
		return value.substring(pos + 1);
	}

	/**
	 * ...copied from gitblit api source code
	 *  Extract trailing ".git" from passed string
	 * @param value url
	 * @return url without ".git"
	 */
	private static String stripDotGit(String value){
		if(value != null && value.toLowerCase().endsWith(".git")){
			return value.substring(0, value.length() - 4);
		}
		return value;
	}

	@Override
	public int hashCode(){
		final int prime = 31;
		int result = 1;
		result = prime * result + ((gitUrl == null) ? 0 : gitUrl.hashCode());
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
		GitBlitRepository other = (GitBlitRepository) obj;
		if(gitUrl == null){
			if(other.gitUrl != null)
				return false;
		}else if(!gitUrl.equals(other.gitUrl))
			return false;
		return true;
	}

	@Override
	public String toString(){
		return "\nGitblit Repository [gitUrl=" + gitUrl + ", groupName=" + groupName + ", projectName=" + projectName + ", projectPath=" + projectPath + ", description=" + description + "]";
	}
	
	

}

//*** fields from com.gitblit.models.RepositoryModel
//public String name;
//public String description;
//public String owner;
//public Date lastChange;
//public boolean hasCommits;
//public boolean showRemoteBranches;
//public boolean useTickets;
//public boolean useDocs;
//public AccessRestrictionType accessRestriction;
//public AuthorizationControl authorizationControl;
//public boolean allowAuthenticated;
//public boolean isFrozen;
//public boolean showReadme;
//public FederationStrategy federationStrategy;
//public List<String> federationSets;
//public boolean isFederated;
//public boolean skipSizeCalculation;
//public boolean skipSummaryMetrics;
//public String frequency;
//public boolean isBare;
//public String origin;
//public String HEAD;
//public List<String> availableRefs;
//public List<String> indexedBranches;
//public String size;
//public List<String> preReceiveScripts;
//public List<String> postReceiveScripts;
//public List<String> mailingLists;
//public Map<String, String> customFields;
//public String projectPath;
//private String displayName;
//public boolean allowForks;
//public Set<String> forks;
//public String originRepository;
//public boolean verifyCommitter;
//public String gcThreshold;
//public int gcPeriod;
//public int maxActivityCommits;
//
//public transient boolean isCollectingGarbage;
//public Date lastGC;
//public String sparkleshareId;

