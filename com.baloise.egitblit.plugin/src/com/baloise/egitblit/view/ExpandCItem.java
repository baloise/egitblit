package com.baloise.egitblit.view;

import static org.eclipse.ui.PlatformUI.getWorkbench;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.ISharedImages;

import com.baloise.egitblit.main.Activator;
import com.baloise.egitblit.main.SharedImages;

public class ExpandCItem extends ContributionItem{

	private TreeViewer viewer;
	public ToolItem item;
	final Image colAll;
	final Image expAll;
	private boolean exp = false;

	public ExpandCItem(TreeViewer viewer){
		super("com.baloise.egitblit.view.contribution.collapseExpand");
		this.viewer = viewer;
		colAll = getWorkbench().getSharedImages().getImage(ISharedImages.IMG_ELCL_COLLAPSEALL);
		expAll = Activator.getImage(SharedImages.ExpandAll);
	}

	@Override
	public void fill(ToolBar parent, int index){
		item = new ToolItem(parent, SWT.NONE);
		item.setToolTipText("Expand / collapse repository table");
		item.setImage(expAll);
		item.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e){
				exp = !exp;
				expand(exp);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e){
			}
		});
	}

	public void expand(boolean yesNo){
		if(viewer != null && item != null){
			if(yesNo == true){
				viewer.expandAll();
				item.setImage(colAll);
			}else{
				viewer.collapseAll();
				item.setImage(expAll);
			}
		}
	}

	public void enable(boolean yesNo){
		if(item != null){
			item.setEnabled(yesNo);
			expand(exp);
		}
	}

	@Override
	public boolean isVisible(){
		return true;
	}

}
