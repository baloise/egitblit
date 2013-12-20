package com.baloise.egitblit.view.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.dialogs.PreferencesUtil;

import com.baloise.egitblit.main.Activator;
import com.baloise.egitblit.main.EclipseHelper;
import com.baloise.egitblit.pref.GitBlitExplorerPrefPage;
import com.baloise.egitblit.view.model.ErrorViewModel;
import com.baloise.egitblit.view.model.GitBlitViewModel;

public abstract class ViewActionBase extends Action{

	private Viewer viewer;
	private String imgDesc = null;

	public ViewActionBase(Viewer viewer, String imgDesc, String label){
		super(label);
		this.viewer = viewer;
		this.imgDesc = imgDesc;
	}
	
	
	public abstract void doRun();
	
	@Override
	public void run(){
		if(handleErrorModel() == true){
			return;
		}
		doRun();
	}
	
	protected Viewer getViewer(){
		return this.viewer;
	}

	protected GitBlitViewModel getSelectedModel(){
		if(this.viewer == null){
			EclipseHelper.logError("Internal error: Can't determinate selected ProjectViewModel. Viewer is null (This is a bug).");
			return null;
		}

		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
		if(selection == null || selection.isEmpty()){
			// No selectiuon, no fun....
			EclipseHelper.logError("Internal error: Action called with no selected ProjectViewModel (This is a bug).");			
			return null;
		}

		Object sel = selection.getFirstElement();
		if(sel instanceof GitBlitViewModel){
			return (GitBlitViewModel) selection.getFirstElement();
		}
		EclipseHelper.logError("Internal error: Can't determinate selected ProjectViewModel. Selection is not of expected type (This is a bug).");
		return null;
	}
	
	protected boolean handleErrorModel(){
		GitBlitViewModel model = getSelectedModel();
		if(model != null && model instanceof ErrorViewModel){
			PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(null, GitBlitExplorerPrefPage.ID, null, null);
		    dialog.open();
		    return true;
		}
		return false;
	}
	
	protected Display getDisplay(){
		if(this.viewer == null){
			EclipseHelper.logError("Internal error: Can't determinate selected ProjectViewModel. Viewer is null (This is a bug).");
			return null;
		}
		return this.viewer.getControl().getDisplay();
	}

	@Override
	public ImageDescriptor getImageDescriptor(){
		if(this.imgDesc != null){
			return Activator.getDefault().getImageRegistry().getDescriptor(this.imgDesc);
		}
		return super.getImageDescriptor();
	}
}
