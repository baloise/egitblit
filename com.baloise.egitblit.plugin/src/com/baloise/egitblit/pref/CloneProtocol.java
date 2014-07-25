package com.baloise.egitblit.pref;

public enum CloneProtocol {
  HTTPS ("https", 8443,   0), 
  HTTP  ("http",  8080,   1), 
  SSH   ("ssh",  29418,   2), 
  GIT   ("git",   9418,   3), 
  File  ("file",  null,   4), 
  FTP   ("ftp",     21,   5), 
  SFTP  ("sftp", 29418,   6);

  public final String schema;
  public final Integer defaultPort;
  public final int index;

  CloneProtocol(String schema, Integer defPort, int index) {
    this.schema = schema;
    this.defaultPort = defPort;
    this.index = index;
  }

  public static CloneProtocol getByIndex(int index) {
    CloneProtocol items[] = values();
    if(index < 0 || index > items.length){
      return null;
    }
    for(CloneProtocol item : items){
      if(item.index == index){
        return item;
      }
    }
    return null;
  }

  /**
   * Prepares an url for the clone action, depending on the current protocol
   * @param host hostname (optional on File protocol)
   * @param port port (optional)
   * @param path path to project
   * @param user userid (optional)
   * @param pwd  password(optional) PLEASE NOTICE: The paassword will be added as plain, readable text!
   * @param pwd  optional
   * @return the prepared url
   */
  public String makeUrl(String host, Integer port, String path, String user, String pwd) {

    if(this == CloneProtocol.File){
       host = host == null ? "" : host;
       port = null; // no port on file protocol
    }

    if(host == null){
      // otherwise (no file protocol): No host no fun
      return null;
    }

    String s = this.schema + "://";
    if(this == CloneProtocol.File && host.isEmpty() == false){
      // File protocol does not support host name. Therefore, add an additional slash (so, passed host name will be part of the path)
      // If no host was passed, parameter path should start with a slash. Therefore, we already have three slashes after schema 
      s += "/";
    }

    if(this != CloneProtocol.File){
      // Add user and if given the password to url. But only if this is not the file protocol 
      if(user != null && user.trim().length() > 0){
        s += user.trim();
        if(pwd != null && pwd.trim().length() > 0){
          s += ":" + pwd.trim();
        }
        s += "@";
      }
    }
    s += host;

    if(port != null && port > 0){
      s += ":" + port;
    }
    if(path != null){
      if(path.startsWith("/") == false){
        path += "/" + path;
      }
      s += path;
    }
    return s;
  }
  
  
  public static String[] getDisplayValues() {
    CloneProtocol items[] = values();
    String res[] = new String[items.length];
    for(CloneProtocol item : items){
      res[item.index] = item.schema;
    }
    return res;
  }

  public static CloneProtocol getValue(String schema) {
    if(schema == null || schema.trim().isEmpty()){
      return null;
    }
    CloneProtocol items[] = values();
    for(CloneProtocol item : items){
      if(item.schema.equalsIgnoreCase(schema)){
        return item;
      }
    }
    return null;
  }
}