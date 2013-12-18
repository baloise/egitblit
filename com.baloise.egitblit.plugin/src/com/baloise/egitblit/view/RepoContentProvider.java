package com.baloise.egitblit.view;

import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.baloise.egitblit.view.model.GitBlitViewModel;
import com.baloise.egitblit.view.model.GroupViewModel;
import com.baloise.egitblit.view.model.ProjectViewModel;

/**
 * @see ITreeContentProvider
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
			return ((List<GroupViewModel>)inputElement).toArray();
		}
		return new ProjectViewModel[0];
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if(parentElement instanceof GroupViewModel){
			GroupViewModel model = (GroupViewModel)parentElement;
			return model.getChilds().toArray();
		}
		return new ProjectViewModel[0];
	}

	@Override
	public Object getParent(Object element) {
		if(element instanceof GitBlitViewModel){
			return ((GitBlitViewModel)element).getParent();
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if(element instanceof GroupViewModel){
			GroupViewModel model = (GroupViewModel)element;
			return model.getChilds().size() > 0;
		}
		return false;
	}


}
