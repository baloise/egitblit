package com.baloise.egitblit.view.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.Viewer;

import com.baloise.egitblit.view.model.GitBlitViewModel;
import com.baloise.egitblit.view.model.ProjectViewModel;

/**
 * Clone repoisitory by calling EGit
 * @see Action  
 * @author MicBag
 *
 */
public class CloneRepositoryAction extends ViewActionBase{

	public CloneRepositoryAction(Viewer viewer){
		super(viewer, null, "Clone repository");
	}

	@Override
	public void doRun(){
		try{
			GitBlitViewModel model = getSelectedModel();
			if(model instanceof ProjectViewModel){

			}
		}catch(Exception e){
		}
	}

	protected boolean cloneRepository(){
		GitBlitViewModel model = getSelectedModel();
		if(model instanceof ProjectViewModel){
			ProjectViewModel pmodel = (ProjectViewModel)model;

		}
		return false;
	}
}
