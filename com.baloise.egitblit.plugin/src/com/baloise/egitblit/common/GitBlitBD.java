package com.baloise.egitblit.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.baloise.egitblit.main.EclipseLog;
import com.baloise.egitblit.main.GitBlitExplorerException;
import com.gitblit.models.RepositoryModel;
import com.gitblit.utils.RpcUtils;

/**
 * BD for reading gitblit repositories
 * 
 * @author MicBag
 * 
 */
public class GitBlitBD{

	/**
	 * List of gitblit servers 
	 */
	private List<GitBlitServer> repoList = new ArrayList<GitBlitServer>();

	/**
	 * @param repoList List of repositories to read from
	 */
	public GitBlitBD(List<GitBlitServer> repoList){
		this.repoList.addAll(repoList);
	}

	/**
	 * @param repoList List of repositories to read from
	 */
	public GitBlitBD(GitBlitServer[] repoList){
		this(Arrays.asList(repoList));
	}

	/**
	 * @param repo server to add to list
	 * @throws GitBlitExplorerException If the passed repo
	 */
	public void addRepository(GitBlitServer repo) throws GitBlitExplorerException{
		if(repo == null){
			EclipseLog.error("Trying to add null value to GitBlidBD.");
			return;
		}
		if(this.repoList.contains(repo) == true){
			throw new GitBlitExplorerException("Repository " + repo.url + " already registered.");
		}
		this.repoList.add(repo);
	}

	/**
	 * @param repo Server to be removed
	 */
	public void removeRepository(GitBlitServer repo){
		this.repoList.remove(repo);
	}

	/**
	 * Reads all groups/repositories from the gitblit servers which have been added
	 * @return List of repositories
	 * @throws GitBlitExplorerException in case of an network or authorization error
	 */
	public List<GitBlitRepository> readRepositories() throws GitBlitExplorerException{
		List<GitBlitRepository> bres = new ArrayList<GitBlitRepository>();
		for(GitBlitServer ritem : this.repoList){
			if(ritem.active == false){
				// Read only from active servers
				continue;
			}
			try{
				// --- Git Utils: RPC Client
				char[] pwd = ritem.password == null ? null : ritem.password.toCharArray();
				Map<String, RepositoryModel> res = RpcUtils.getRepositories(ritem.url, ritem.user, pwd);
				for(String item : res.keySet()){
					bres.add(GitBlitRepository.create(ritem.url, item, res.get(item)));
				}
			}catch(Exception e){
				throw new GitBlitExplorerException(ritem.url,e);
			}
		}
		return bres;
	}
}
