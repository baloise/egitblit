package com.baloise.egitblit.view;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

import com.baloise.egitblit.view.model.GroupViewModel;
import com.baloise.egitblit.view.model.ProjectViewModel;

/**
 * @see CellLabelProvider
 * @author MicBag
 * extends CellLabelProvider  
 **/

public class RepoLabelProvider implements ITableLabelProvider{

	@Override
	public Image getColumnImage(Object element, int columnIndex){
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex){
		switch(columnIndex){
			case 0:
				if(element instanceof GroupViewModel){
					return ((GroupViewModel) element).getName();
				}
				else if(element instanceof ProjectViewModel){
					return ((ProjectViewModel) element).getName();
				}
				break;
			case 1:
				if(element instanceof ProjectViewModel){
					return ((ProjectViewModel) element).getDescription();
				}
				break;
			case 2:
				if(element instanceof ProjectViewModel){
					return ((ProjectViewModel) element).getServerURL();
				}
				break;
		}
		return null;
	}

	@Override
	public void addListener(ILabelProviderListener listener){
	}

	@Override
	public void dispose(){
	}

	@Override
	public boolean isLabelProperty(Object element, String property){
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener){
	}
	
	
}
