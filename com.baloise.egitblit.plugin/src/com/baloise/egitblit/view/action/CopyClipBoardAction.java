package com.baloise.egitblit.view.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.ISharedImages;

import com.baloise.egitblit.view.model.GitBlitViewModel;
import com.baloise.egitblit.view.model.ProjectViewModel;

/**
 * Guess what: Copy Git url to clipboard
 * @see Action 
 * @author MicBag
 *
 */
public class CopyClipBoardAction extends ViewActionBase{

	public CopyClipBoardAction(Viewer viewer){
		super(viewer,ISharedImages.IMG_TOOL_COPY, "Copy repository Url");
	}

	@Override
	public void doRun(){
		GitBlitViewModel model = getSelectedModel();
		if(model != null){
			if(model instanceof ProjectViewModel){
				Clipboard clipboard = new Clipboard(getDisplay());
				clipboard.setContents(new Object[] { ((ProjectViewModel)model).getGitURL() }, new Transfer[] { TextTransfer.getInstance() });
			}
		}
	}
}
