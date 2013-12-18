package com.baloise.egitblit.view;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

import com.baloise.egitblit.view.model.GroupViewModel;
import com.baloise.egitblit.view.model.ProjectViewModel;

/**
 * @author MicBag
 * 
 */
public class RepoLabelProvider implements ITableLabelProvider {

	@Override
	public Image getColumnImage(Object element, int columnIndex){
//		if(element instanceof GroupViewModel && columnIndex == 0){
//			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER); 
//		}
		return null;
	}
	
	@Override
	public String getColumnText(Object element, int columnIndex){
		if(element == null){
			return "(Error)";
		}
		switch(columnIndex){
			case 0:
				if(element instanceof GroupViewModel){
					return ((GroupViewModel) element).getName();
				}
				if(element instanceof ProjectViewModel){
					return ((ProjectViewModel) element).getName();
				}
				return null;
			case 1:
				if(element instanceof ProjectViewModel){
					return ((ProjectViewModel) element).getDescription();
				}
				return null;
		}
		return "(Error)";
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
