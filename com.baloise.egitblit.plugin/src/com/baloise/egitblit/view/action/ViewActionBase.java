package com.baloise.egitblit.view.action;

import static org.eclipse.jface.resource.ImageDescriptor.createFromURL;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.dialogs.PreferencesUtil;

import com.baloise.egitblit.main.Activator;
import com.baloise.egitblit.pref.GitBlitExplorerPrefPage;
import com.baloise.egitblit.pref.PreferenceModel;
import com.baloise.egitblit.view.model.ErrorViewModel;
import com.baloise.egitblit.view.model.GitBlitViewModel;

/**
 * Base class for handling viewer actions (get selection etc.)
 * The actionDefinitionId must match to a command id in plugin.xml (if you want to define a keybinding)
 * @see Action 
 * @author MicBag
 *
 */
public abstract class ViewActionBase extends Action{

	public final static String KEY_PREF_MODEL = "com.baloise.egitblit.prefmodel";
	private Viewer viewer;
	
	/**
	 * @param id Id corresponding to a command id in plugin.xml if you want to define a key binding
	 * @param viewer viewer
	 * @param label label to be displayed
	 */
	public ViewActionBase(String id, Viewer viewer, String label){
		super(label);
		this.viewer = viewer;
		setActionDefinitionId(id);
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

	public PreferenceModel getPrefModel(){
		return (PreferenceModel)this.viewer.getData(KEY_PREF_MODEL);
	}
	
	public void setPrefModel(PreferenceModel model){
		this.viewer.setData(KEY_PREF_MODEL, model);
	}
	
	/**
	 * Determinate the selected view model from the viewer
	 * @return Selected model
	 */
	protected GitBlitViewModel getSelectedModel(){
		if(this.viewer == null){
			Activator.logError("Internal error: Can't determinate selected ProjectViewModel. Viewer is null (This is a bug).");
			return null;
		}

		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
		if(selection == null || selection.isEmpty()){
			// No selectiuon, no fun....
			Activator.logError("Internal error: Action called with no selected ProjectViewModel (This is a bug).");			
			return null;
		}

		Object sel = selection.getFirstElement();
		if(sel instanceof GitBlitViewModel){
			return (GitBlitViewModel) selection.getFirstElement();
		}
		Activator.logError("Internal error: Can't determinate selected ProjectViewModel. Selection is not of expected type (This is a bug).");
		return null;
	}
	
	/**
	 * If the viewer is displaying a error model, an error dialog will be displayed
	 * This is the general implementation, just showing an error dialog 
	 * 
	 * @return error has occured
	 */
	protected boolean handleErrorModel(){
		GitBlitViewModel model = getSelectedModel();
		if(model instanceof ErrorViewModel){
			return openPreferences();
		}
		return false;
	}
	
	public final static boolean openPreferences(){
		PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(null, GitBlitExplorerPrefPage.ID, null, null);
	    return Window.OK == dialog.open();
	}
	
	protected Display getDisplay(){
		if(this.viewer == null){
			Activator.logError("Internal error: Can't determinate selected ProjectViewModel. Viewer is null (This is a bug).");
			return null;
		}
		return this.viewer.getControl().getDisplay();
	}
	
	protected void setImageDescriptorFromURL(String spec) {
		try {
			setImageDescriptor(createFromURL(new URL(spec)));
		} catch (MalformedURLException e) {
			Activator.logError("Could not load image from url "+spec, e);
		}
	}

}
