package com.baloise.egitblit.view.action;

import static org.eclipse.core.runtime.Platform.getPreferencesService;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.PlatformUI;

import com.baloise.egitblit.gitblit.GitBlitRepository;
import com.baloise.egitblit.main.EclipseHelper;
import com.baloise.egitblit.view.model.GitBlitViewModel;
import com.baloise.egitblit.view.model.ProjectViewModel;

/**
 * Opens the Gitblit summary page of the selected repository path
 * 
 * @see Action
 * @author MicBag
 * 
 */
public class OpenGitBlitAction extends ViewActionBase{

	public final static String GITBLIT_SUMMARY_PATH = "summary/?r=";
	public final static String GIT_URL_POSTFIX = ".git";

	public OpenGitBlitAction(Viewer viewer){
		super(viewer, "Browse");
		boolean useInternalBrowser = 0 == getPreferencesService().getInt("org.eclipse.ui.browser", "browser-choice", 1, null); 
		if(useInternalBrowser){
			setImageDescriptorFromURL("platform:/plugin/org.eclipse.ui.browser/icons/obj16/internal_browser.gif");
		} else {
			setImageDescriptorFromURL("platform:/plugin/org.eclipse.ui.browser/icons/obj16/external_browser.gif");
		}
	}

	/**
	 * Creates a summary url for displaying project summary page in a browser
	 * 
	 * @param model
	 *            RepoViewModel
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
		url += GITBLIT_SUMMARY_PATH;

		// ignore main group
		if(!GitBlitRepository.GROUP_MAIN.equalsIgnoreCase(model.getGroupName())){
			url += model.getGroupName() + "/";
		}
		url += model.getProjectName() + GIT_URL_POSTFIX;
		return new URL(url);

	}

	@Override
	public void doRun(){
		GitBlitViewModel model = getSelectedModel();
		if(model instanceof ProjectViewModel){
			ProjectViewModel pm = (ProjectViewModel) model;
			if(pm.hasCommits() == false){
				EclipseHelper.showInfo("The selected repository has no commits. Can�t open an empty repository in GitBlit.");
				return;
			}
			try{
				PlatformUI.getWorkbench().getBrowserSupport().createBrowser("Gitblit summary page").openURL(makeGitBlitSummaryUrl(pm)); 
				//PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(makeGitBlitSummaryUrl(pm));
			}catch(Exception e){
				EclipseHelper.logError("Error while performing open Gitblit action", e);
			}
		}
	}
}
