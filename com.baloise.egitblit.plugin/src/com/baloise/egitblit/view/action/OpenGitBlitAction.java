package com.baloise.egitblit.view.action;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import com.baloise.egitblit.common.GitBlitRepository;
import com.baloise.egitblit.main.EclipseHelper;
import com.baloise.egitblit.view.model.GitBlitViewModel;
import com.baloise.egitblit.view.model.ProjectViewModel;

/**
 * Opens the gitblit summary page for a repository
 * 
 * @see Action
 * @author Mike
 * 
 */
public class OpenGitBlitAction extends ViewActionBase{

	public final static String GITBLIT_SUMMARY_PATH = "summary/?r=";
	public final static String GIT_URL_POSTFIX = ".git";

	public OpenGitBlitAction(Viewer viewer){
		super(viewer, ISharedImages.IMG_TOOL_COPY, "Open GitBlit");
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
				EclipseHelper.showInfo("The selected repository has no commits. Can´t open an empty repository in GitBlit.");
				return;
			}
			try{
				URL url = makeGitBlitSummaryUrl(pm);
				PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(url);
			}catch(Exception e){
				EclipseHelper.logError("Error while performing open Gitblit action", e);
			}
		}
	}
}
