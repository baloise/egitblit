package com.baloise.egitblit.view;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.dialogs.PreferencesUtil;

import com.baloise.egitblit.main.Activator;
import com.baloise.egitblit.pref.GitBlitExplorerPrefPage;
import com.baloise.egitblit.view.model.ErrorViewModel;
import com.baloise.egitblit.view.model.ProjectViewModel;

/**
 * Guess what: Copy Git url to clipboard
 * @see Action 
 * @author MicBag
 *
 */
public class CopyClipBoardAction extends Action{

	private final Viewer viewer;

	public CopyClipBoardAction(Viewer viewer){
		super("Copy Repository Url");
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
					Clipboard clipboard = new Clipboard(viewer.getControl().getDisplay());
					clipboard.setContents(new Object[] { model.getGitURL() }, new Transfer[] { TextTransfer.getInstance() });
				}
			}
			if(sel instanceof ErrorViewModel){
				PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(null, GitBlitExplorerPrefPage.ID, null, null);
			    dialog.open();
			}
		}
	}

	@Override
	public ImageDescriptor getImageDescriptor(){
		return Activator.getDefault().getImageRegistry().getDescriptor(ISharedImages.IMG_TOOL_COPY);
	}
}
