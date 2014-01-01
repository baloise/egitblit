package com.baloise.egitblit.view;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreePathViewerSorter;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TreeColumn;

import com.baloise.egitblit.main.Activator;
import com.baloise.egitblit.view.model.GitBlitViewModel;
import com.baloise.egitblit.view.model.GroupViewModel;
import com.baloise.egitblit.view.model.ProjectViewModel;
import com.gitblit.utils.ArrayUtils;

/**
 * Sorting view asc.
 * 
 * @author MicBag
 * 
 */
public class RepoViewSorter extends TreePathViewerSorter{

	// private final static DecimalFormat dform = new
	private final static DecimalFormat dform = new DecimalFormat("0000000000000000000.00");

	public RepoViewSorter(){
	}

	@Override
	public int category(TreePath parentPath, Object element){
		if(element == null){
			return 0;
		}
		if(element instanceof GroupViewModel){
			return 1;
		}
		if(element instanceof ProjectViewModel){
			return 2;
		}
		return super.category(parentPath, element);
	}

	@Override
	public boolean isSorterProperty(Object element, String property){
		// TODO Auto-generated method stub
		return super.isSorterProperty(element, property);
	}

	@Override
	public int compare(Viewer viewer, TreePath parentPath, Object e1, Object e2){
		if(viewer == null){
			Activator.logError("Can´t sort view. Viewer is null.");
			return 0;
		}
		TreeViewer tviewer = (TreeViewer) viewer;

		int dir = SWT.UP;
		ColumnDesc column = null;
		String v1 = "";
		String v2 = "";

		int cat1 = category(e1);
		int cat2 = category(e2);
		if(cat1 != cat2){
			if(dir == SWT.DOWN){
				return cat2 - cat1;
			}
			return cat1 - cat2;
		}
		if(viewer instanceof TreeViewer == false){
			Activator.logError("Can´t sort tree. Wrong viewer classs. Expecting a TreeViewer, but viewer is of class " + viewer.getClass());
			return 0;
		}

		// --- Standard column sorting by label
		IBaseLabelProvider prov = tviewer.getLabelProvider();
		if(prov instanceof RepoLabelProvider == false){
			Activator.logError("Can´t sort tree. Wrong LabelProvider classs. Expecting a RepoLabelProvider, but viewer is of class " + prov.getClass());
			return 0;
		}

		// Init
		dir = tviewer.getTree().getSortDirection();
		TreeColumn col = tviewer.getTree().getSortColumn();
		column = ColumnDesc.getColumnDesc(tviewer.getTree().indexOf(col));
		RepoLabelProvider lprov = (RepoLabelProvider) prov;

		v1 = lprov.getColumnText((GitBlitViewModel) e1, column);
		v2 = lprov.getColumnText((GitBlitViewModel) e2, column);

		v1 = v1 == null ? "" : v1;
		v2 = v2 == null ? "" : v2;
		v1 = v1.trim();
		v2 = v2.trim();

		if(column != ColumnDesc.Repository){
			// For repository column, use label
			if(e1 instanceof GroupViewModel){
				// Get the values for the columns, if the model is a group
				// Because a group entry does not contain values for all columns
				v1 = getGroupSortValue(tviewer, (GroupViewModel) e1, column, dir);
				v2 = getGroupSortValue(tviewer, (GroupViewModel) e2, column, dir);
//				if(v1.compareTo(v2) == 0){
//					// Based on sort direction, entries are equal, so maybe the invers 	comparison
//					// brings a unique result (compare highest vs. compare lowest entries of a group)
//					v1 = getGroupSortValue(tviewer, (GroupViewModel) e1, column, (dir == SWT.DOWN ? SWT.UP : SWT.DOWN));
//					v2 = getGroupSortValue(tviewer, (GroupViewModel) e2, column, (dir == SWT.DOWN ? SWT.UP : SWT.DOWN));
//				}
			}else if(e1 instanceof ProjectViewModel){
				// Only for numeric values required (because of alphanumeric(!)
				// string compare)
				switch(column){
					case LastChange:
						v1 = makeSortValue(((ProjectViewModel) e1).getLastChange());
						v2 = makeSortValue(((ProjectViewModel) e2).getLastChange());
						break;
					case Size:
						v1 = makeSortValue(((ProjectViewModel) e1).getByteSize());
						v2 = makeSortValue(((ProjectViewModel) e2).getByteSize());
						break;
					case Server:
						break;
					default:
						break;
				}
			}
		}
		if(dir == SWT.DOWN){
			return -v1.compareTo(v2);
		}
		return v1.compareTo(v2);
	}

	protected final static String makeSortValue(long value){
		return dform.format(value);
	}

	protected final static String makeSortValue(double value){
		return dform.format(value);
	}

	protected final static String makeSortValue(Date date){
		if(date == null){
			Activator.logError("Error creating sort value for date. Argument is null.");
			return makeSortValue(System.currentTimeMillis());
		}
		return makeSortValue(System.currentTimeMillis() - date.getTime());
	}

	/**
	 * If the group cell has no value, use one of its child values, depending of
	 * the sort order
	 * 
	 * @param gmodel
	 * @param col
	 * @param dir
	 * @return
	 */
	private String getGroupSortValue(TreeViewer viewer, GroupViewModel gmodel, ColumnDesc col, int dir){
		if(gmodel instanceof GroupViewModel == false){
			return "";
		}

		GroupViewModel model = (GroupViewModel) gmodel;
		ArrayList<String> sl = new ArrayList<String>();
		List<GitBlitViewModel> list = model.getChilds();
		if(list.isEmpty()){
			return "";
		}

		// 1) Build an array containing the child values
		// 2) Sort them (asc)
		// 3) Finally, returning the first or last element (highest or lowest
		// value) of the array, depending of the sort order
		for(GitBlitViewModel item : list){
			if(item instanceof ProjectViewModel){
				switch(col){
					case Repository:
						break;
					case Description:
						sl.add(((ProjectViewModel) item).getDescription());
						break;
					case Owner:
						sl.add(ArrayUtils.toString(((ProjectViewModel) item).getOwners()));
						break;
					case LastChange:
						sl.add(makeSortValue(((ProjectViewModel) item).getLastChange()));
						break;
					case Size:
						sl.add(makeSortValue(((ProjectViewModel) item).getByteSize()));
						break;
					case Server:
						sl.add(((ProjectViewModel) item).getServerURL());
						break;
					default:
						sl.add(((ProjectViewModel) item).getName());
				}
			}
		}

		if(sl.size() == 1){
			return sl.get(0);
		}
		
		// let´s sort
		Collections.sort(sl);

		// fill with defaults, so that we have no leaks
		// a empty slot will be filled with the next higher value (n(i) == null
		// ? n(i+1) : n)
		int len = sl.size() - 1;
		for(int i = len; i >= 0; i--){
			if(sl.get(i).isEmpty()){
				if((i + 1) <= len){
					sl.remove(i);
					sl.add(i, sl.get(i));
				}
			}
		}

		// Return value depending on sort order (highest or lowest)
		if(dir == SWT.UP){
			return sl.get(0);
		}
//		return sl.get(0);
		int pos = sl.size() - 1;
		return sl.get((pos > 0 ? pos : 0));
	}

}


/*
GroupC ->	 GroupB	 (0000000000000000004,00 / 0000000000000000003,00 = 1
main ->	 GroupC	     (0000000000000000004,00 / 0000000000000000004,00 = 0
A ->	 main	     (0000000000000000003,00 / 0000000000000000004,00 = -1
A ->	 GroupC	     (0000000000000000003,00 / 0000000000000000004,00 = -1
A ->	 GroupB	     (0000000000000000003,00 / 0000000000000000003,00 = 0
GroupA ->	 GroupC	 (0000000000000000003,00 / 0000000000000000004,00 = -1
GroupA ->	 A	     (0000000000000000003,00 / 0000000000000000003,00 = 0

*/