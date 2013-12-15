package com.baloise.egitblit.common;

import com.gitblit.models.RepositoryModel;

/**
 * DO Model
 * 
 * @author MicBag
 * 
 */
public class GitBlitRepository {

  public String url;
  public String repositoryName;
  public String projectName;
  public String description;

  public GitBlitRepository() {
  }

  public static GitBlitRepository create(String repoUrl, RepositoryModel model) {
    GitBlitRepository repo = new GitBlitRepository();

    repo.url = repoUrl;
    repo.repositoryName = getRepositoryName(model);
    repo.projectName = getProjectName(model);
    repo.description = model.description;

    return repo;
  }

  private final static String getRepositoryName(RepositoryModel model) {
    if (model == null) {
      return null;
    }
    String value = stripDotGit(model.name);
    if (value == null) {
      return null;
    }
    int pos = value.lastIndexOf("/");
    if (pos == -1) {
      return "<root>";
    }
    return value.substring(0, pos);
  }

  private final static String getProjectName(RepositoryModel model) {
    if (model == null) {
      return null;
    }
    String value = stripDotGit(model.name);
    if (value == null) {
      return null;
    }
    int pos = value.lastIndexOf("/");
    if (pos == -1) {
      return value;
    }
    return value.substring(pos + 1);
  }

  public static String stripDotGit(String value) {
    if (value.toLowerCase().endsWith(".git")) {
      return value.substring(0, value.length() - 4);
    }
    return value;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((url == null) ? 0 : url.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    GitBlitRepository other = (GitBlitRepository) obj;
    if (url == null) {
      if (other.url != null)
        return false;
    } else if (!url.equals(other.url))
      return false;
    return true;
  }

  @Override
  public String toString() {
    StringBuilder buff = new StringBuilder();
    buff.append("URL........: ");
    buff.append(this.url);
    buff.append("\nRepository.: ");
    buff.append(this.repositoryName);
    buff.append("\nProjectname: ");
    buff.append(this.projectName);
    return buff.toString();
  }
}
