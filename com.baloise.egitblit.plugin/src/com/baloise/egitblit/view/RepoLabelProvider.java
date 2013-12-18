package com.baloise.egitblit.view;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Point;

import com.baloise.egitblit.view.model.GitBlitViewModel;
import com.baloise.egitblit.view.model.GroupViewModel;
import com.baloise.egitblit.view.model.ProjectViewModel;

/**
 * @author MicBag
 * 
 */
public class RepoLabelProvider extends CellLabelProvider{

	@Override
	public void update(ViewerCell cell){
		Object element = cell.getElement();
		int columnIndex = cell.getColumnIndex();
		if(element == null){
			return;
		}

		switch(columnIndex){
			case 0:
				if(element instanceof GroupViewModel){
					cell.setText(((GroupViewModel) element).getName());
				}
				else if(element instanceof ProjectViewModel){
					cell.setText(((ProjectViewModel) element).getName());
				}
				break;
			case 1:
				if(element instanceof ProjectViewModel){
					cell.setText(((ProjectViewModel) element).getDescription());
				}
				break;
		}
	}

	@Override
	public String getToolTipText(Object element){
		String str = null;
		if(element instanceof GitBlitViewModel){
			str = ((GitBlitViewModel)element).getToolTip();
		}
		return str;
	}

	@Override
	public Point getToolTipShift(Object object){
		return new Point(5, 5);
	}

	@Override
	public int getToolTipDisplayDelayTime(Object object){
		return 500;
	}
	
	@Override
	public int getToolTipTimeDisplayed(Object object) {
		return 5000;
	}
}
