package com.baloise.egitblit.view;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;

import com.baloise.egitblit.common.GitBlitRepository;
import com.baloise.egitblit.common.GitBlitServer;
import com.baloise.egitblit.main.Activator;
import com.baloise.egitblit.main.EclipseLog;
import com.baloise.egitblit.pref.GitBlitExplorerPrefPage;
import com.baloise.egitblit.view.model.ErrorViewModel;
import com.baloise.egitblit.view.model.ProjectViewModel;

public class OpenGitBlitAction extends Action{

	public final static String GITBLIT_SUMMARY_PATH = "summary/?r=";
	public final static String GIT_URL_POSTFIX = ".git";
	
	private final Viewer viewer; 
	
	
	public OpenGitBlitAction(Viewer viewer){
		super("Open GitBlit");
		this.viewer = viewer;
	}
	
	
	/**
	 * Creates a summary url for displaying project summary page in a browser
	 * @param model RepoViewModel
	 * @return URL
	 * @throws MalformedURLException 
	 */
	private final static URL makeGitBlitSummaryUrl(ProjectViewModel model) throws MalformedURLException{
		if(model == null || model.getServerURL() == null){
			return null;
		}
		
		// --- Get Server url an prepare it
		String url = model.getServerURL();
		if(url != null && url.endsWith("/") == false){
			url += "/";
		}
		url +=  GITBLIT_SUMMARY_PATH;
		
		// ignore main group
		if(!GitBlitRepository.GROUP_MAIN.equalsIgnoreCase(model.getGroupName())){
			url += model.getGroupName() + "/";
		}
		url += model.getProjectName() + GIT_URL_POSTFIX;

//		// Ignore main group
//		if(!GitBlitRepository.GROUP_MAIN.equalsIgnoreCase(model.getGroupName())){
//			url += model.getGroupName() + GitBlitServer.DEF_URL_SEPARATOR;
//		}
//
//		url += model.getProjectName() + GIT_URL_POSTFIX;
		return new URL(url);
		
	}
	
	@Override
	public void run() {
		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
		if(selection != null) {
			Object sel = selection.getFirstElement();
			if(sel instanceof ProjectViewModel){
				ProjectViewModel model = (ProjectViewModel) selection.getFirstElement();
				if(model != null) {
					if(model.hasCommits() == false){
						MessageDialog.open(IStatus.INFO,viewer.getControl().getShell(),"GitBlit Repository Explorer","The selected repository has no commits. Can´t open an empty repository in GitBlit.",SWT.NONE);
						return;
					}
					try{
						URL url = makeGitBlitSummaryUrl(model);
						PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(url);
					}catch (Exception e) {
						EclipseLog.error("Error while performing open GitBlit action",e);
					}
				}
			}
			if(sel instanceof ErrorViewModel){
				PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(null, GitBlitExplorerPrefPage.ID, null, null);
			    dialog.open();
			}
		}
	}
	

	@Override
	public ImageDescriptor getImageDescriptor() {
		return Activator.getDefault().getImageRegistry().getDescriptor(ISharedImages.IMG_TOOL_COPY);
	}
}
