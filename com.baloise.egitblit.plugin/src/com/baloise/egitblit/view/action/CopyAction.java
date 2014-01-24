package com.baloise.egitblit.view.action;

import static org.eclipse.ui.PlatformUI.getWorkbench;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
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
public class CopyAction extends ViewActionBase{
	public final static String ID = "com.baloise.egitblit.cmd.copy";

	public CopyAction(Viewer viewer){
		super(ID,viewer, "Copy");
		ImageDescriptor img = getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_COPY);
		setImageDescriptor(img);
		ImageDescriptor imgDisabled = getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_COPY_DISABLED);
		setDisabledImageDescriptor(imgDisabled);
	}

	@Override
	public void doRun(){
		if(handleErrorModel() == true){
			return; // Error state: Can't perform action
		}
		GitBlitViewModel model = getSelectedModel();
		if(model != null){
			if(model instanceof ProjectViewModel){
				Clipboard clipboard = new Clipboard(getDisplay());
				clipboard.setContents(new Object[] { ((ProjectViewModel)model).getGitUrl() }, new Transfer[] { TextTransfer.getInstance() });
			}
		}
	}
}
