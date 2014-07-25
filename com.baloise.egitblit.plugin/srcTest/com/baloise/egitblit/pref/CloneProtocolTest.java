package com.baloise.egitblit.pref;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class CloneProtocolTest {

  class TestData{
    CloneProtocol cp;
    public final String   host;
    public final Integer  port;
    public final String   path;
    public final String   user;
    public final String   pwd;
    public final String   url;  // expected url;
    
    public TestData(CloneProtocol cp, String host, Integer port, String path, String user, String pwd, String url){
      this.cp = cp;
      this.host = host;
      this.port = port;
      this.path = path;
      this.user = user;
      this.pwd = pwd;
      this.url = url;
    }
  }
  
  @Test
  public void testMakeUrl() {
   
    String  HOST = "myHost";
    Integer PORT = 4711;
    String  PATH = "/a/b/c.git";
    String  USER = "bob";
    String  PWD  = "secret";
    
    List<TestData> tcList = new ArrayList<TestData>();
    String expUrl_FULL              = "://" + USER + ":" + PWD + "@" + HOST + ":" + PORT + PATH;
    String expUrl_NO_PWD            = "://" + USER + "@" + HOST + ":" + PORT + PATH;
    String expUrl_NO_USERPWD        = "://" + HOST + ":" + PORT + PATH;
    String expUrl_NO_PORT           = "://" + USER + ":" + PWD + "@" + HOST + PATH;

    // protocol specific cases
    String expUrl_NO_PORTUSERPWD_FILE   = ":///" + HOST + PATH;
    String expUrl_NO_HOSTPORTUSERPWD_FILE   = "://" + PATH;
    
    // --- prepare test cases
    CloneProtocol[] cpl = CloneProtocol.values();
    for(CloneProtocol item : cpl){
      if(item == CloneProtocol.File){
        tcList.add(new TestData(item, HOST,PORT,PATH,USER,PWD,  item.schema + expUrl_NO_PORTUSERPWD_FILE)); 
        tcList.add(new TestData(item, HOST,PORT,PATH,null,PWD,  item.schema + expUrl_NO_PORTUSERPWD_FILE));
        tcList.add(new TestData(item, HOST,PORT,PATH,USER,null, item.schema + expUrl_NO_PORTUSERPWD_FILE));
        tcList.add(new TestData(item, HOST,PORT,PATH,null,null, item.schema + expUrl_NO_PORTUSERPWD_FILE));
        tcList.add(new TestData(item, null,PORT,PATH,USER,PWD,  item.schema + expUrl_NO_HOSTPORTUSERPWD_FILE));
        tcList.add(new TestData(item, HOST,null,PATH,USER,PWD,  item.schema + expUrl_NO_PORTUSERPWD_FILE));
        tcList.add(new TestData(item, null,null,PATH,USER,PWD,  item.schema + expUrl_NO_HOSTPORTUSERPWD_FILE));
        continue;
      }
      tcList.add(new TestData(item, HOST,PORT,PATH,USER,PWD,  item.schema + expUrl_FULL)); 
      tcList.add(new TestData(item, HOST,PORT,PATH,null,PWD,  item.schema + expUrl_NO_USERPWD));
      tcList.add(new TestData(item, HOST,PORT,PATH,USER,null, item.schema + expUrl_NO_PWD));
      tcList.add(new TestData(item, HOST,PORT,PATH,null,null, item.schema + expUrl_NO_USERPWD));
      tcList.add(new TestData(item, null,PORT,PATH,USER,PWD,  null));
      tcList.add(new TestData(item, HOST,null,PATH,USER,PWD,  item.schema + expUrl_NO_PORT));
      tcList.add(new TestData(item, null,null,PATH,USER,PWD,  null));
    }
    
    // --- perform tests
    for(TestData item : tcList){
      String act= item.cp.makeUrl(item.host, item.port, item.path, item.user, item.pwd);
      Assert.assertEquals("Testcase for " + item.cp.name(), item.url, act);
    }
    
  }

}
