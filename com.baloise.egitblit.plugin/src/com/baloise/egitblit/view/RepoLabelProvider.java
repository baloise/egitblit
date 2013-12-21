package com.baloise.egitblit.view;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

import com.baloise.egitblit.view.model.GroupViewModel;
import com.baloise.egitblit.view.model.ProjectViewModel;

/**
 * @see CellLabelProvider
 * @author MicBag extends CellLabelProvider
 **/

public class RepoLabelProvider implements ITableLabelProvider{

	private SimpleDateFormat df = new SimpleDateFormat();
	
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
				}else if(element instanceof ProjectViewModel){
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
					List<String> list = ((ProjectViewModel) element).getOwners();
					StringBuilder buff = new StringBuilder();
					int size = list.size();
					for(int pos = 0; pos < size; pos++){
						buff.append(list.get(pos));
						if(pos < (size-1)){
							buff.append(", ");
						}
					}
					return buff.toString();
				}
				break;
			case 3:
				if(element instanceof ProjectViewModel){
				    return df.format(((ProjectViewModel) element).getLastChange());
				}
				break;
			case 4:
				if(element instanceof ProjectViewModel){
				    return ((ProjectViewModel) element).getSize();
				}
				break;
			case 5:
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
