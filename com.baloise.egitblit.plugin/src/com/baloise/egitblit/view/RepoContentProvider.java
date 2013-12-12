package com.baloise.egitblit.view;

import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * @author MicBag
 *
 */
public class RepoContentProvider implements ITreeContentProvider  {

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if(inputElement instanceof List){
			return ((List<RepoViewModel>)inputElement).toArray();
		}
		return new RepoViewModel[0];
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if(parentElement instanceof RepoViewModel){
			RepoViewModel model = (RepoViewModel)parentElement;
			return model.getChilds().toArray();
		}
		return new RepoViewModel[0];
	}

	@Override
	public Object getParent(Object element) {
		if(element instanceof RepoViewModel){
			RepoViewModel model = (RepoViewModel)element;
			return model.parent;
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if(element instanceof RepoViewModel){
			RepoViewModel model = (RepoViewModel)element;
			return model.getChilds().size() > 0;
		}
		return false;
	}


}
