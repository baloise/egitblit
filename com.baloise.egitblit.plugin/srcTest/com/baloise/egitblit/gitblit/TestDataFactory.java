package com.baloise.egitblit.gitblit;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.gitblit.utils.StringUtils;

public class TestDataFactory{

	
	public final static List<GitBlitRepository> createRepoList(int goups, int reposPerGroup){
		List<GitBlitRepository> resList = new ArrayList<GitBlitRepository>();
		
		int count = 0;
		for(int g=0;g<goups;g++){
			for(int r=0;r<reposPerGroup;r++){
				resList.add(createRepository("Group " + g, "Name (" + g + "/" + r + ")",count++,r % 2 == 0));
			}
		}
		return resList;
	}
	
	public final static GitBlitRepository createRepository(String group, String name, int id, boolean boolValues){
		GitBlitRepository repo = new GitBlitRepository();
		
	    repo.name = name;
	    repo.description= "Description for " + name;
	    
    	repo.owners.addAll(makeStringList("Owner",4));

	    repo.lastChange= new Date();
	    repo.lastChangeAuthor= "LC Auther " + id;
	    repo.hasCommits= boolValues;
	    repo.showRemoteBranches= boolValues;
	    repo.useIncrementalPushTags= boolValues;
	    repo.incrementalPushTagPrefix= "incrementalPushTagPrefix";
	    
	    repo.accessRestriction="accessRestriction";
	    repo.authorizationControl= "authorizationControl";
	    
	    repo.allowAuthenticated= boolValues;
	    repo.isFrozen= boolValues;
	    repo.federationStrategy= "federationStrategy";
    	repo.federationSets.addAll(makeStringList("federationSets", 3));
	    repo.isFederated= boolValues;
	    repo.skipSizeCalculation= boolValues;
	    repo.skipSummaryMetrics= boolValues;
	    repo.frequency= "frequency";
	    repo.isBare= boolValues;

	    repo.origin= "origin";
	    repo.HEAD= "HEAD";
	    repo.availableRefs.addAll(makeStringList("availableRefs ",3));
	    
    	repo.indexedBranches.addAll(makeStringList("indexedBranches",5));
	    
    	repo.size= "" + (id * 1024);
    	repo.preReceiveScripts.addAll(makeStringList("preReceiveScripts",4));
    	repo.postReceiveScripts.addAll(makeStringList("postReceiveScripts",4));

    	repo.mailingLists.addAll(makeStringList("mailingLists",3));
    	//repo.customFields.putAll(makeStringList("customFields",5));

    	repo.projectPath = "projectPath";

	    repo.allowForks= boolValues;
    	repo.forks.addAll(makeStringList("forks",5));

    	repo.originRepository= "originRepository ";
	    repo.verifyCommitter= boolValues;
	    repo.gcThreshold= "gcThreshold";
	    repo.gcPeriod= 321;
	    repo.maxActivityCommits= 21;

    	repo.metricAuthorExclusions.addAll(makeStringList("metricAuthorExclusions",3));

	    repo.isCollectingGarbage= boolValues;
	    repo.lastGC= new Date();
	    repo.sparkleshareId= "sparkleshareId";


	    // --- Passed or prepared attributes
		repo.gitUrl = "gitUrl/" + group + "/projectName/" + id + ".git";
		repo.groupName = group;

		repo.projectName = "projectName " + id;
		repo.serverUrl = "serverUrl";
		repo.byteSize = new Long(id * 1024);
		repo.repoRGB = StringUtils.getColor(repo.projectName);

		return repo;
	}
	
	private final static List<String> makeStringList(String label, int size){
		List<String> res = new ArrayList<String>(size);
		for(int i=0; i<size;i++){
			res.add(label + i);
		}
		return res;
	}
}
