package com.baloise.egitblit.pref;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.baloise.egitblit.gitblit.GitBlitServer;

public class CloneProtocolTest{

	
	private CloneSettings createSettings(CloneProtocol cp, Integer port){
		return new CloneSettings(cp,port);
	}
	
	@Test
	public void testMakeUrl(){

		Map<GitBlitServer, String> testData = new HashMap<GitBlitServer,String>();
		
		String schema = "https";
		String host   = "myHost";
		Integer port   = 4711;
		String project = "/myProject.git";
		String path   = "/a/b/c/" + project;
		String user   = "user";
		String pwd    = "pwd";

		String gitProjectUrl = schema + "://" + host + path;
		
		int i=0;
		testData.put(new GitBlitServer("" + i++, user,pwd,createSettings(CloneProtocol.File, null)), 	CloneProtocol.File.schema 	+ ":///" + host + path);
		testData.put(new GitBlitServer("" + i++, user,pwd,createSettings(CloneProtocol.FTP, null)),  	CloneProtocol.FTP.schema 	+ "://"  + host + path);
		testData.put(new GitBlitServer("" + i++, user,pwd,createSettings(CloneProtocol.GIT, null)),		CloneProtocol.GIT.schema 	+ "://"  + host + project);
		testData.put(new GitBlitServer("" + i++, user,pwd,createSettings(CloneProtocol.HTTP, null)),	CloneProtocol.HTTP.schema 	+ "://"  + host + path);
		testData.put(new GitBlitServer("" + i++, user,pwd,createSettings(CloneProtocol.HTTPS, null)), 	CloneProtocol.HTTPS.schema 	+ "://"  + host + path);
		testData.put(new GitBlitServer("" + i++, user,pwd,createSettings(CloneProtocol.SFTP, null)),	CloneProtocol.SFTP.schema 	+ "://"  + host + path);
		testData.put(new GitBlitServer("" + i++, user,pwd,createSettings(CloneProtocol.SSH, null)),		CloneProtocol.SSH.schema 	+ "://"  + user + "@" + host + project);
		

		testData.put(new GitBlitServer("" + i++, user,pwd,createSettings(CloneProtocol.File, port)), 	CloneProtocol.File.schema 	+ ":///" + host + path);
		testData.put(new GitBlitServer("" + i++, user,pwd,createSettings(CloneProtocol.FTP, port)),  	CloneProtocol.FTP.schema 	+ "://"  + host + ":" + port + path);
		testData.put(new GitBlitServer("" + i++, user,pwd,createSettings(CloneProtocol.GIT, port)),		CloneProtocol.GIT.schema 	+ "://"  + host + ":" + port + project);
		testData.put(new GitBlitServer("" + i++, user,pwd,createSettings(CloneProtocol.HTTP, port)),	CloneProtocol.HTTP.schema 	+ "://"  + host + ":" + port + path);
		testData.put(new GitBlitServer("" + i++, user,pwd,createSettings(CloneProtocol.HTTPS, port)), 	CloneProtocol.HTTPS.schema 	+ "://"  + host + ":" + port + path);
		testData.put(new GitBlitServer("" + i++, user,pwd,createSettings(CloneProtocol.SFTP, port)),	CloneProtocol.SFTP.schema 	+ "://"  + host + ":" + port + path);
		testData.put(new GitBlitServer("" + i++, user,pwd,createSettings(CloneProtocol.SSH, port)),		CloneProtocol.SSH.schema 	+ "://"  + user + "@" + host + ":" + port + project);

		
		for(GitBlitServer item : testData.keySet()){
			Assert.assertEquals(gitProjectUrl +" does not match", testData.get(item), item.getCloneURL(gitProjectUrl));
			System.out.println(item.getCloneURL(gitProjectUrl));
		}
		
	}

}
