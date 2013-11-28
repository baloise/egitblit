package ch.basler.plugin.gitblitexplorer.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.gitblit.models.RepositoryModel;
import com.gitblit.utils.RpcUtils;


/**
 * @author MicBag
 *
 */
public class GitBlitBD {

	private String endpoint;
	private String user;
	private String pwd;
	
	private boolean doCaching = false;
	private List<GitBlitRepository> projectCache;
	
	public GitBlitBD(String endpoint,String user, String pwd){
		this(endpoint,user,pwd,false);
	}

	public GitBlitBD(String endpoint,String user, String pwd, boolean doCaching){
		this.endpoint = endpoint;
		this.user = user;
		this.pwd = pwd;
		this.doCaching = doCaching;
	}
		
	public void clearCache(){
		this.projectCache = null;
	}
	
	/**
	 * Liest alle GIT Repositories ("Projekte")
	 * @return List der als BaslerRepository aufbereiteten GIT-Repositories  
	 * @throws GitBlitBDException
	 */
	public List<GitBlitRepository> readRepositories() throws GitBlitBDException{
		if(this.projectCache != null){
			return this.projectCache;
		}
		try{
			// --- Git Utils: RPC Client für den Zugriff
			Map<String, RepositoryModel> res = RpcUtils.getRepositories(this.endpoint,this.user,this.pwd.toCharArray());
			List<GitBlitRepository> bres = new ArrayList<GitBlitRepository>();
			for(String item : res.keySet()){
				bres.add(GitBlitRepository.create(item, res.get(item)));
			}
			// --- Caching BD: Wenn cache if desired
			if(this.doCaching){
				this.projectCache = bres;
			}
			return bres;
		}
		catch(Exception e){
			clearCache();
			throw new GitBlitBDException(e);
		}
	}

	public Map<String,List<GitBlitRepository>> readRepositoryMap() throws GitBlitBDException{
		try{
			List<GitBlitRepository> res = readRepositories();
			Map<String,List<GitBlitRepository>> mres = new TreeMap<String,List<GitBlitRepository>>();
			for(GitBlitRepository item : res){
				List<GitBlitRepository> rlist = mres.get(item.repositoryName);
				if(rlist == null){
					rlist = new ArrayList<GitBlitRepository>();
					mres.put(item.repositoryName, rlist);
				}
				if(rlist.contains(item) == false){
					rlist.add(item);
				}
			}
			return mres;
		}
		catch(GitBlitBDException e){
			throw e;
		}
		catch(Exception e){
			clearCache();
			throw e;
		}
	}
	
}

