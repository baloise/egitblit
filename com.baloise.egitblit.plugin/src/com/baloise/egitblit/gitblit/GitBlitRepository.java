package com.baloise.egitblit.gitblit;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.gitblit.models.RepositoryModel;
import com.gitblit.utils.StringUtils;

/**
 * Represents a GitBlit Repository 
 * This class contains the gitblit repository properties as copy.
 * @author MicBag
 * 
 */
public class GitBlitRepository{

	public static final String GROUP_MAIN = "main";
	public String serverUrl;
	public String gitUrl;
	public String groupName;
	public String projectName;
	public String projectPath;
	public boolean hasCommits;
	public String description;
	
	public List<String> owners = new ArrayList<String>();
	public Date lastChange;
	public boolean isFrozen;
	public boolean isFederated;
	public String frequency;
	public boolean isBare;
	public String originRepository;
	public String size;
	public long   byteSize;
	public String repoRGB;

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
		
		repo.owners.addAll(model.owners);
		repo.lastChange = model.lastChange;
		
		repo.isFrozen = model.isFrozen;
		repo.isFederated = model.isFederated;
		repo.frequency = model.frequency;
		repo.isBare = model.isBare;
		repo.originRepository = model.originRepository;
		repo.size = model.size;
		repo.byteSize = makeByteValue(repo.size);
		
		repo.repoRGB = StringUtils.getColor(repo.projectName);

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
	
	/**
	 * Gitblit repo size is a string. To make it comparable, the real numeric
	 * bytes have to be extracted
	 * 
	 * @param value
	 * @return
	 */
	public final static long makeByteValue(String value){
		if(value == null || value.isEmpty()){
			return -1L;
		}
		try{
			String size = value;
			long f = 1L;
			// Using 1000 instead of 1024 to avoid multiplications
			size = size.trim().toLowerCase();
			if(size.endsWith(" b")){
				size = size.substring(0, size.length() - 2).trim();
				f = 1L;
			}else if(size.endsWith(" k")){
				size = size.substring(0, size.length() - 2).trim();
				f = 1024L;
			}else if(size.endsWith(" kb")){
				size = size.substring(0, size.length() - 3).trim();
				f = 1024L;
			}else if(size.endsWith(" mb")){
				size = size.substring(0, size.length() - 3).trim();
				f = 1048576L;
			}else if(size.endsWith(" gb")){
				size = size.substring(0, size.length() - 3).trim();
				f = 1073741824L;
			}else if(size.endsWith(" tb")){
				size = size.substring(0, size.length() - 3).trim();
				f = 1099511627776L;
			}else{
				f = 1L;
			}

			// Detect decimal separator (no regex!)
			size = size.replace("'", "");
			size = size.replace("´", "");

			int cpos = size.lastIndexOf(",");
			int dpos = size.lastIndexOf(".");

			if(cpos != -1 || dpos != -1){
				if(cpos > dpos){
					// Comma is separator:
					size = size.replace(".", "").replaceAll(",", ".");
				}else if(cpos < dpos || (cpos != -1 && dpos > -1)){
					// Dot is separator
					size = size.replace(",", "");
				}
			}
			Double val = f * Double.parseDouble(size);
			return val.longValue();
		}catch(Exception e){
			//Activator.logError("Error while sorting view", e);
		}
		return -1L;
	}


	@Override
	public String toString(){
		return "\nGitblit Repository [gitUrl=" + gitUrl + ", groupName=" + groupName + ", projectName=" + projectName + ", projectPath=" + projectPath + ", description=" + description + "]";
	}
}

//*** fields from com.gitblit.models.RepositoryModel
//public boolean showRemoteBranches;
//public boolean useTickets;
//public boolean useDocs;
//public AccessRestrictionType accessRestriction;
//public AuthorizationControl authorizationControl;
//public boolean allowAuthenticated;
//public boolean showReadme;
//public FederationStrategy federationStrategy;
//public List<String> federationSets;
//public boolean skipSizeCalculation;
//public boolean skipSummaryMetrics;
//public String origin;
//public String HEAD;
//public List<String> availableRefs;
//public List<String> indexedBranches;
//public List<String> preReceiveScripts;
//public List<String> postReceiveScripts;
//public List<String> mailingLists;
//public Map<String, String> customFields;
//public String projectPath;
//private String displayName;
//public boolean allowForks;
//public Set<String> forks;
//public boolean verifyCommitter;
//public String gcThreshold;
//public int gcPeriod;
//public int maxActivityCommits;
//
//public transient boolean isCollectingGarbage;
//public Date lastGC;
//public String sparkleshareId;

