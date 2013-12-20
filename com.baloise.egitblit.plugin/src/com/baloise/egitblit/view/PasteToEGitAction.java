package com.baloise.egitblit.view;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.dialogs.PreferencesUtil;

import com.baloise.egitblit.main.EclipseLog;
import com.baloise.egitblit.pref.GitBlitExplorerPrefPage;
import com.baloise.egitblit.view.model.ErrorViewModel;
import com.baloise.egitblit.view.model.ProjectViewModel;

public class PasteToEGitAction extends Action{

	private final Viewer viewer;

	public final static String CMD_EGIT = "org.eclipse.egit.ui.RepositoriesViewPaste";

	public PasteToEGitAction(Viewer viewer){
		super("Paste repository Url in EGit");
		this.viewer = viewer;
	}

	@Override
	public void run(){
		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
		if(selection != null){
			Object sel = selection.getFirstElement();
			if(sel instanceof ProjectViewModel){
				ProjectViewModel model = (ProjectViewModel) selection.getFirstElement();
				if(model != null){
					// Copy url to clipboard
					CopyClipBoardAction cca = new CopyClipBoardAction(viewer);
					cca.run();

					Command cmd= getEGitCommand();
					if(cmd == null){
						EclipseLog.error("Can't call EGit. Eclipse command service not avail or EGit not installed.");
						return;
					}
					try{
						cmd.executeWithChecks(new ExecutionEvent());
					}catch(Exception e){
						EclipseLog.error("Error pasting repository url to EGit", e);
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
	public boolean isEnabled(){
		return getEGitCommand() != null;
	}

	public final static Command getEGitCommand(){
		ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getService(ICommandService.class);
		if(commandService == null){
			// Eclipse not ready
			return null;
		}
		return commandService.getCommand(CMD_EGIT);
	}
}
