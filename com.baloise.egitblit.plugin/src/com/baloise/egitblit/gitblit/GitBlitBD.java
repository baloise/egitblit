package com.baloise.egitblit.gitblit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.baloise.egitblit.common.GitBlitExplorerException;
import com.baloise.egitblit.main.Activator;
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
	 * @param repoList
	 *            List of repositories to read from
	 */
	public GitBlitBD(List<GitBlitServer> repoList){
		this.repoList.addAll(repoList);
	}

	/**
	 * @param repoList
	 *            List of repositories to read from
	 */
	public GitBlitBD(GitBlitServer[] repoList){
		this(Arrays.asList(repoList));
	}

	public int getRepositorySize(){
		return this.repoList.size();
	}

	/**
	 * @param repo
	 *            server to add to list
	 * @throws GitBlitExplorerException
	 *             If the passed repo
	 */
	public void addRepository(GitBlitServer repo) throws GitBlitExplorerException{
		if(repo == null){
			Activator.logError("Trying to add null value to GitBlidBD (this is a bug).");
		}
		if(this.repoList.contains(repo) == true){
			throw new GitBlitExplorerException("Repository " + repo.url + " already registered.");
		}
		this.repoList.add(repo);
	}

	/**
	 * @param repo
	 *            Server to be removed
	 */
	public void removeRepository(GitBlitServer repo){
		this.repoList.remove(repo);
	}

	/**
	 * Reads all groups/repositories from the gitblit servers which have been
	 * added
	 * 
	 * @return List of repositories
	 * @throws GitBlitExplorerException
	 *             in case of an network or authorization error
	 */
	/**
	 * Reads all groups/repositories from the gitblit servers which have been
	 * added
	 * 
	 * @return List of repositories
	 * @throws GitBlitExplorerException
	 *             in case of an network or authorization error
	 */
	public List<GitBlitRepository> readRepositories(ProgressToken token, boolean activeOnly, boolean omitHasError) throws GitBlitExplorerException{
		List<GitBlitRepository> bres = new ArrayList<GitBlitRepository>();

		List<String> serverErrorList = new ArrayList<String>();
		Exception firstEx = null;

		for(GitBlitServer ritem : this.repoList){
			if(token != null){
				token.startWork(ritem.url);
				if(token.isCanceled()){
					return bres;
				}
			}
			if((activeOnly == true && ritem.active == false) ||	// Read only active servers
			   (omitHasError == true && ritem.serverError == true)){ // Read only servers which are working / avail
				// Read only from activated servers
				if(token != null){
					token.endWork();
				}
				// Read only from active servers
				continue;
			}
			try{
				ritem.serverError = false;
				// --- Git Utils: RPC Client
				char[] pwd = ritem.password == null ? null : ritem.password.toCharArray();
				Map<String, RepositoryModel> res = RpcUtils.getRepositories(ritem.url, ritem.user, pwd);
				for(String item : res.keySet()){
					bres.add(GitBlitRepository.create(ritem.url, item, res.get(item)));
				}
			}catch(Exception e){
				Activator.logError("Error accessing Gitblit server " + ritem.url, e);
				ritem.serverError = true;
				// Add error to list
				serverErrorList.add(ritem.url);
				if(firstEx == null){
					firstEx = e;
				}
			}
			if(token != null){
				token.endWork();
			}
		}
		// In case of an error, prepare message an throw it
		if(firstEx != null && omitHasError == false){
			String msg = "Error while reading Gitblit repositories. Connecting following servers has caused an error: " + serverErrorList;
			throw new GitBlitExplorerException(msg, firstEx);
		}
		return bres;
	}
}
