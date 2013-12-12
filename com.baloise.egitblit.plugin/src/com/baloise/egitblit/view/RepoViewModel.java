package com.baloise.egitblit.view;

import java.util.ArrayList;
import java.util.List;

/**
 * @author MicBag
 *
 */
public class RepoViewModel {

	public String endpoint;
	public String repo;
	public String project;
	public String desc;

	public RepoViewModel parent;
	protected List<RepoViewModel> childs = new ArrayList<RepoViewModel>();
	
	public RepoViewModel(){
	}
	
	public RepoViewModel(RepoViewModel model){
		this.endpoint = model.endpoint;
		this.repo = model.repo;
		this.project = model.project;
		this.desc = model.desc;
	}
	
	
	public RepoViewModel(String url, String repositoryName, String projectName, String desc) {
		this.endpoint = url;
		this.repo = repositoryName;
		this.project = projectName;
		this.desc = desc;
	}

	public void add(RepoViewModel model){
		if(model == null){
			return;
		}
		this.childs.add(model);
		model.parent = this;
	}
	
	public void add(List<RepoViewModel> list){
		if(list == null || list.isEmpty()){
			return;
		}
		for(RepoViewModel item: list){
			add(item);
		}
	}
	
	public List<RepoViewModel> getChilds(){
		return this.childs;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((endpoint == null) ? 0 : endpoint.hashCode());
		result = prime * result + ((project == null) ? 0 : project.hashCode());
		result = prime * result + ((repo == null) ? 0 : repo.hashCode());
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
		RepoViewModel other = (RepoViewModel) obj;
		if (endpoint == null) {
			if (other.endpoint != null)
				return false;
		} else if (!endpoint.equals(other.endpoint))
			return false;
		if (project == null) {
			if (other.project != null)
				return false;
		} else if (!project.equals(other.project))
			return false;
		if (repo == null) {
			if (other.repo != null)
				return false;
		} else if (!repo.equals(other.repo))
			return false;
		return true;
	}

	
}
