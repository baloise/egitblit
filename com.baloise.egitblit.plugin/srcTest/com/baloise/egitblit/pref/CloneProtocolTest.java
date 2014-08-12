package com.baloise.egitblit.pref;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.baloise.egitblit.gitblit.GitBlitServer;

public class CloneProtocolTest{

  class TestCase{
	  public final String exprectedURL;
	  public final String projectURL;
	  public final GitBlitServer server;
	  
	  public TestCase(String projectURL, String user, String pwd, Integer port, CloneProtocol protocol, String exprectedURL){
	    this.exprectedURL = exprectedURL;
	    this.projectURL = projectURL;
	    server = new GitBlitServer(projectURL,user,pwd,new CloneSettings(protocol,port));	    
	  }
	}
	
	private List<TestCase> testCases = new ArrayList<TestCase>();
	

	private void addTestCase(String exprectedCtxPath, String projectURL, String user, String pwd, Integer port, CloneProtocol protocol){
	  this.testCases.add(new TestCase(projectURL,user,pwd, port, protocol,protocol.schema + "://" + exprectedCtxPath));
	}
	
	
	@Test
	public void testMakeUrl(){

    try{
      addTestCaseSet(4711);
      addTestCaseSet(null);
      
      for(TestCase item : this.testCases){
        Assert.assertEquals("Clone Url protocol: " +item.server.cloneSettings.getCloneProtocol().schema + ":", 
            item.exprectedURL, item.server.getCloneURL(item.projectURL));
      }
    }
    catch(Exception e){
      Assert.fail(e.getCause().getMessage());
    }
	}
	
	
	protected void addTestCaseSet(Integer port){
    String schema = "https";
    String host   = "myHost";
    String project = "myProject.git";
    String user   = "user";
    String pwd    = "pwd";

    String path   = "/a/b/c/";
    String sshPath = "/" + GitBlitServer.GITBLIT_CTX_PATH;

    String httpURL = schema + "://" + host + getPort(port,null) + sshPath + project;

    // SSH
    addTestCase(user + "@" + host + getPort(port,CloneProtocol.SSH) + "/" + project,      httpURL, user,pwd,port, CloneProtocol.SSH);
    addTestCase(user + "@" + host + getPort(port,CloneProtocol.SSH) + "/" + project,      httpURL, user,null,port, CloneProtocol.SSH);
    addTestCase(host + getPort(port,CloneProtocol.SSH) + "/" + project,                   httpURL, null,null,port, CloneProtocol.SSH);
    // GIT
    addTestCase(host + getPort(port,CloneProtocol.GIT) + "/" + project,                   httpURL, user,pwd,port, CloneProtocol.GIT);
    addTestCase(host + getPort(port,CloneProtocol.GIT) + "/" + project,                   httpURL, user,null,port, CloneProtocol.GIT);
    addTestCase(host + getPort(port,CloneProtocol.GIT) + "/" + project,                   httpURL, null,null,port, CloneProtocol.GIT);
    
    httpURL = schema + "://" + host + getPort(port,null)+ "/" + GitBlitServer.GITBLIT_CTX_PATH + path + project;
    // SSH
    addTestCase(user + "@" + host + getPort(port,CloneProtocol.SSH) + path + project,     httpURL, user,pwd,port, CloneProtocol.SSH);
    addTestCase(user + "@" + host + getPort(port,CloneProtocol.SSH)+ path + project,      httpURL, user,null,port, CloneProtocol.SSH);
    addTestCase(host + getPort(port,CloneProtocol.SSH) + path + project,                  httpURL, null,null,port, CloneProtocol.SSH);
    // GIT
    addTestCase(host + getPort(port,CloneProtocol.GIT) + path + project,                  httpURL, user,pwd,port, CloneProtocol.GIT);
    addTestCase(host + getPort(port,CloneProtocol.GIT) + path + project,                  httpURL, user,null,port, CloneProtocol.GIT);
    addTestCase(host + getPort(port,CloneProtocol.GIT) + path + project,                  httpURL, null,null,port, CloneProtocol.GIT);

    httpURL = schema + "://" + host + getPort(port,null) + path + project;
    // SSH
    addTestCase(user + "@" + host + getPort(port,CloneProtocol.SSH) + "/" + project,     httpURL, user,pwd,port, CloneProtocol.SSH);
    addTestCase(user + "@" + host + getPort(port,CloneProtocol.SSH) + "/" + project,     httpURL, user,null,port, CloneProtocol.SSH);
    addTestCase(host + getPort(port,CloneProtocol.SSH) + "/" + project,                  httpURL, null,null,port, CloneProtocol.SSH);
    // GIT
    addTestCase(host + getPort(port,CloneProtocol.GIT) + "/" + project,                 httpURL, user,pwd,port, CloneProtocol.GIT);
    addTestCase(host + getPort(port,CloneProtocol.GIT) + "/" + project,                 httpURL, user,null,port, CloneProtocol.GIT);
    addTestCase(host + getPort(port,CloneProtocol.GIT) + "/" + project,                 httpURL, null,null,port, CloneProtocol.GIT);

    
    // Other protocols
    addTestCase(host + getPort(port,CloneProtocol.FTP) + path + project,      httpURL, user,pwd,port, CloneProtocol.FTP);
    addTestCase(host + getPort(port,CloneProtocol.HTTP) + path + project,     httpURL, user,pwd,port, CloneProtocol.HTTP);
    addTestCase(host + getPort(port,CloneProtocol.HTTPS) + path + project,    httpURL, user,pwd,port, CloneProtocol.HTTPS);
    addTestCase(host + getPort(port,CloneProtocol.SFTP) + path + project,     httpURL, user,pwd,port, CloneProtocol.SFTP);

    addTestCase(host + getPort(port,CloneProtocol.FTP) + path + project,      httpURL, user,null,port, CloneProtocol.FTP);
    addTestCase(host + getPort(port,CloneProtocol.HTTP) + path + project,     httpURL, user,null,port, CloneProtocol.HTTP);
    addTestCase(host + getPort(port,CloneProtocol.HTTPS) + path + project,    httpURL, user,null,port, CloneProtocol.HTTPS);
    addTestCase(host + getPort(port,CloneProtocol.SFTP) + path + project,     httpURL, user,null,port, CloneProtocol.SFTP);

    addTestCase(host + getPort(port,CloneProtocol.FTP) + path + project,      httpURL, null,null,port, CloneProtocol.FTP);
    addTestCase(host + getPort(port,CloneProtocol.HTTP) + path + project,     httpURL, null,null,port, CloneProtocol.HTTP);
    addTestCase(host + getPort(port,CloneProtocol.HTTPS) + path + project,    httpURL, null,null,port, CloneProtocol.HTTPS);
    addTestCase(host + getPort(port,CloneProtocol.SFTP) + path + project,     httpURL, null,null,port, CloneProtocol.SFTP);
	
	}

	private String getPort(Integer port, CloneProtocol prot){
	  if(port == null){
	    return "";
	  }
	  return ":" + port;
	}
}
