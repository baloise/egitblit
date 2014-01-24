package com.baloise.egitblit.view.action;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

import com.baloise.egitblit.main.Activator;
import com.baloise.egitblit.view.model.GitBlitViewModel;
import com.baloise.egitblit.view.model.ProjectViewModel;

/**
 * Action to paste git repository url to clipboard and calls corrsponding EGit command to open clone wizard
 * @see Action  
 * @author MicBag
 *
 */
public class CloneAction extends ViewActionBase{

	public final static String CMD_EGIT = "org.eclipse.egit.ui.RepositoriesViewPaste";
	public final static String ID = "com.baloise.egitblit.plugin.cmd.clone";

	public CloneAction(Viewer viewer){
		super(ID,viewer, "Clone");
		setImageDescriptorFromURL("platform:/plugin/org.eclipse.egit.ui/icons/obj16/cloneGit.gif");
	}

	@Override
	public void doRun(){
		GitBlitViewModel model = getSelectedModel();
		if(model instanceof ProjectViewModel){
			// Copy url to clipboard
			CopyAction cca = new CopyAction(getViewer());
			cca.run();

			Command cmd = getEGitCommand();
			if(cmd == null){
				Activator.logError("Can't call EGit. Eclipse command service not avail or EGit not installed.");
				return;
			}
			try{
				cmd.executeWithChecks(new ExecutionEvent());
			}catch(Exception e){
				Activator.logError("Error pasting repository url to EGit", e);
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
