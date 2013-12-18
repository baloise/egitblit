package com.baloise.egitblit.common;

import java.util.ArrayList;
import java.util.List;

import com.baloise.egitblit.main.GitBlitExplorerException;

public class Test{

	public static void main(String[] args){
		// TODO Auto-generated method stub
		new Test().run();
	}
	
	public void run(){
		
		List<GitBlitServer> repoList = new ArrayList<GitBlitServer>();
		
		repoList.add(new GitBlitServer("http://localhost:8080", "/", true, "admin", "admin"));
		repoList.add(new GitBlitServer("http://localhost:8082", "/", true, "admin", "admin"));
		GitBlitBD bd = new GitBlitBD(repoList);
		
		List<GitBlitRepository> pList;
		try{
			pList = bd.readRepositories();
			System.out.println(pList);
		}catch(GitBlitExplorerException e){
			e.printStackTrace();
		}
		
	}

}
