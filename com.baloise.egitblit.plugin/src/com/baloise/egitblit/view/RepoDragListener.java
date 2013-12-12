package com.baloise.egitblit.view;

import java.net.URL;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.URLTransfer;

/**
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
		RepoViewModel model= (RepoViewModel) selection.getFirstElement();
		if(model.endpoint == null){
			return;
		}
		if (TextTransfer.getInstance().isSupportedType(event.dataType)) {
			event.data = model.endpoint;
		}
		if (URLTransfer.getInstance().isSupportedType(event.dataType)) {
			try{
				event.data = new URL(model.endpoint);
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
	}

	@Override
	public void dragFinished(DragSourceEvent event) {
	}

}
