package com.baloise.egitblit.view;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import com.baloise.egitblit.view.model.GitBlitViewModel;
import com.baloise.egitblit.view.model.GroupViewModel;
import com.baloise.egitblit.view.model.ProjectViewModel;

public class RepoViewSorter extends ViewerSorter{

	@Override
	public int category(Object element){
		if(element == null){
			return 0;
		}
		if(element instanceof GroupViewModel){
			return 1;
		}
		if(element instanceof ProjectViewModel){
			return 2;
		}
		return super.category(element);
	}

	@Override
	public int compare(Viewer viewer, Object o1, Object o2){
		if(o1 == null){
			if(o2 == null){
				return 0;
			}
			return 1;
		}
		if(o2 == null){
			return -1;
		}
		
		String v1 = ((GitBlitViewModel)o1).getName();
		String v2 = ((GitBlitViewModel)o2).getName();
		
		if(v1 != null){
			return v1.compareTo(v2);
		}
		if(v2 != null){
			return (v2 == null ? 0 : 1); 
		}
		return super.compare(viewer, o1, o2);
	}
//
//	@Override
//	public boolean isSorterProperty(Object element, String property){
//		// TODO Auto-generated method stub
//		return super.isSorterProperty(element, property);
//	}

	@Override
	public void sort(Viewer viewer, Object[] elements){
		// TODO Auto-generated method stub
		super.sort(viewer, elements);
	}

}
