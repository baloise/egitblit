package com.baloise.egitblit.view;

import java.net.URL;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.URLTransfer;

import com.baloise.egitblit.main.EclipseHelper;
import com.baloise.egitblit.view.model.GitBlitViewModel;
import com.baloise.egitblit.view.model.ProjectViewModel;

/**
 * Offers drag & drop capability to a selected repository in viewer
 * @see DragSourceListener
 * @author MicBag
 *
 */
public class RepoDragListener implements DragSourceListener {

	private TreeViewer viewer;
	
	public RepoDragListener(TreeViewer viewer){
		this.viewer = viewer;
	}
	
	@Override
	public void dragStart(DragSourceEvent event) {
	}

	@Override
	public void dragSetData(DragSourceEvent event) {
		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
		GitBlitViewModel model= (ProjectViewModel) selection.getFirstElement();

		String gitURL=null;
		if(model instanceof ProjectViewModel){
			ProjectViewModel pm = (ProjectViewModel)model;
			gitURL = pm.getGitURL();
		}
		if(gitURL == null){
			return;
		}
		if (TextTransfer.getInstance().isSupportedType(event.dataType)) {
			event.data = gitURL;
		}
		if (URLTransfer.getInstance().isSupportedType(event.dataType)) {
			try{
				event.data = new URL(gitURL);
			}
			catch(Exception e){
				EclipseHelper.logError("Error while performin drag & drop",e);
			}
		}
	}

	@Override
	public void dragFinished(DragSourceEvent event) {
	}

}
