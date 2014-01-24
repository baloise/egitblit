package com.baloise.egitblit.view;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.TreeColumn;

import com.baloise.egitblit.main.Activator;
import com.baloise.egitblit.pref.PreferenceMgr;
import com.baloise.egitblit.pref.PreferenceModel;
import com.baloise.egitblit.pref.PreferenceModel.ColumnData;

/**
 * Helper / Factory for creating and maintaining repo viewer tree / columns
 * 
 * @author MicBag
 */
public class ColumnFactory{
	public final static String	KEY_COLDESC		= "ColumnDesc";
	private TreeViewer			viewer;

	/**
	 * Move listener
	 */
	ControlListener	moveListener = new ControlListener() {
		@Override
		public void controlResized(ControlEvent e){
		}
		
		@Override
		public void controlMoved(ControlEvent e){
			int[] order = viewer.getTree().getColumnOrder();
			int[] newOrder = new int[order.length];
			int i = 0;
			// First column can't be moved and no other column can be placed at pos 0
				newOrder[i++] = 0;
				for(int col : order){
					if(col != 0){
						newOrder[i++] = col;
					}
				}
				viewer.getTree().setColumnOrder(newOrder);
			}
		};

	SelectionAdapter selListener= new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e){
			TreeColumn col = viewer.getTree().getSortColumn();
			viewer.getTree().setSortColumn((TreeColumn) e.widget);
			if(((TreeColumn) e.widget).equals(col)){
				int dir = viewer.getTree().getSortDirection() == SWT.UP ? SWT.DOWN : SWT.UP;
				viewer.getTree().setSortDirection(dir);
			}
			viewer.refresh();
		}
	};

	/**
	 * @param viewer
	 *            Viewer of the factory
	 * @param prefModel
	 *            preference model to store column settings
	 */
	public ColumnFactory(TreeViewer viewer){
		this.viewer = viewer;
	}

	public TreeColumn addColumn(ColumnDesc desc, int pos, int width){
		if(desc == null){
			return null;
		}
		int max = viewer.getTree().getColumnCount();
		pos = (pos > max || pos < 0) ? max : pos;
		TreeColumn col = new TreeColumn(viewer.getTree(), desc.style, pos);

		col.setAlignment(desc.style);
		col.setText(desc.label);
		col.setToolTipText(desc.label);
		col.setWidth(width);
		col.setData(KEY_COLDESC, desc);

		if(desc.isMovable()){
			enableMoveable(col);
		}
		return col;
	}

	public TreeColumn addColumn(ColumnDesc desc){
		return addColumn(desc, desc.getIndex(),desc.width);
	}

	public void removeColumn(ColumnDesc desc){
		TreeColumn col = getColumn(desc);
		if(col != null){
			disableMovable(col);
			col.dispose();
		}
	}

	public final static ColumnDesc getColumnDesc(TreeColumn item){
		if(item == null){
			return null;
		}
		return (ColumnDesc) item.getData(KEY_COLDESC);
	}

	public final ColumnDesc getColumnDesc(int index){
		return getColumnDesc(viewer.getTree().getColumn(index));
	}

	public final TreeColumn getColumn(ColumnDesc desc){
		if(desc == null){
			return null;
		}

		TreeColumn[] items = viewer.getTree().getColumns();
		for(TreeColumn item : items){
			if(desc == (ColumnDesc) item.getData(KEY_COLDESC)){
				return item;
			}
		}
		return null;
	}

	public final int getColumnIndex(ColumnDesc desc){
		if(desc == null){
			return -1;
		}
		
		int pos = 0;

		// Initial position
		TreeColumn[] items = viewer.getTree().getColumns();
		// actual order (index is order, value is init position)
		int[] actOrder = viewer.getTree().getColumnOrder();
		for(TreeColumn item : items){
			if(desc == (ColumnDesc) item.getData(KEY_COLDESC)){
				for(int i=0;i<actOrder.length;i++){
					if(actOrder[i] == pos){
						return i;
					}
				}
			}
			pos++;
		}
		return -1;
	}

	public final void enableMoveable(final TreeColumn... cols){
		if(cols == null || cols.length == 0){
			return;
		}

		for(TreeColumn col : cols){
			col.setMoveable(true);
			col.addControlListener(moveListener);
		}
	}

	public final void disableMovable(TreeColumn... cols){
		if(cols == null || cols.length == 0){
			return;
		}

		for(TreeColumn col : cols){
			col.setMoveable(false);
			col.removeControlListener(moveListener);
		}
	}

	public final void createColumns(PreferenceModel model){

		viewer.getTree().setRedraw(false);

		// --- Remove all columns
		TreeColumn[] treeCols = viewer.getTree().getColumns();
		disableMovable(treeCols);
		for(TreeColumn item : treeCols){
			// Next line: Useless, because item will be disposed and eventtable
			// will be set to null
			// item.removeSelectionListener(selListener);
			item.dispose();
		}

		List<ColumnData> colList = null;
		if(model != null && model.getColumnData().size() > 0){
			// --- Create columns from prefs
			colList = model.getColumnData();
		}
		if(colList == null || colList.isEmpty()){
			// --- no columns in preferences saved
			colList = PreferenceMgr.initColumnData();
		}

		PreferenceMgr.sortByIndex(colList);
		colList = PreferenceMgr.filterVisible(colList);
		ColumnDesc desc;
		for(ColumnData item : colList){
			desc = ColumnDesc.valueOf(item.id);
			if(item.visible){
				addColumn(desc,item.pos,item.width);
			}
		}

		treeCols = viewer.getTree().getColumns();
		enableMoveable(treeCols);
		initTreeSorter();
		viewer.refresh(true);
		viewer.getTree().setRedraw(true);
	}

	private void initTreeSorter(){
		if(this.viewer.getTree().getColumnCount() == 0){
			Activator.logError("Can't init TreeSorter. Tree has no columns.");
			return;
		}
		RepoViewSorter repoViewSorter = new RepoViewSorter();
		viewer.setSorter(repoViewSorter);
		viewer.getTree().setSortColumn(viewer.getTree().getColumn(0));
		viewer.getTree().setSortDirection(SWT.UP);
		viewer.refresh();

		TreeColumn[] cols = viewer.getTree().getColumns();
		for(final TreeColumn item : cols){
			item.addSelectionListener(this.selListener);
		}
	}

	/**
	 * @param toPrefFromPref
	 *            true = set current column data in Preference model, false =
	 *            init columns with pref settings
	 */
	public void update(PreferenceModel model, boolean toPrefFromPref){
		if(toPrefFromPref){
			model.setColumnData(getColumnData());
		}else{
			createColumns(model);
		}
	}
	
	public List<ColumnData> getColumnData(){
		TreeColumn col;
		int pos, width;
		boolean visible;
		
		List<ColumnData> list = new ArrayList<PreferenceModel.ColumnData>();
		ColumnDesc[] items = ColumnDesc.values();
		for(ColumnDesc item : items){
			col = getColumn(item);
			if(col == null){
				visible = false;
				width = item.width;
				pos = -1;
			}
			else{
				visible = true;
				width = col.getWidth();
				pos = getColumnIndex(item);
			}
			list.add(new ColumnData(item.name(), pos, visible, width));
		}
		
		// --- create default position for invisible columns
		
		// --- Get max position of visible columns
		pos = -1;
		for(ColumnData item : list){
			if(pos < item.pos && item.visible){
				pos = item.pos;
			}
		}
		// --- override position for invisible columns with next free index
		for(ColumnData item : list){
			if(item.pos == -1){
				item.pos = ++pos;
			}
		}
		return list;
	}
	
	
	//
	// /**
	// * @return Actual column data
	// */
	// private List<PreferenceModel.ColumnData> getColumnData(){
	// List<PreferenceModel.ColumnData> colDataList = new
	// ArrayList<PreferenceModel.ColumnData>();
	// viewer.refresh(true);
	//
	// int width;
	// int pos;
	// int max;
	// String id;
	// boolean visible;
	// TreeColumn col;
	//
	// ColumnDesc[] cdesc = ColumnDesc.values();
	// max = cdesc.length-1;
	// for(ColumnDesc item : cdesc){
	// col = getColumn(item);
	// if(col == null){
	// width = item.width;
	// visible = false;
	// }
	// else{
	// width = col.getWidth();
	// visible = width > 0 ? true : false;
	// }
	// id = item.name();
	// pos = visible == true ? getColumnIndex(item) : max--;
	//
	// colDataList.add(new PreferenceModel.ColumnData(id, pos, visible,width));
	// }
	// return colDataList;
	// }
	//
	// /**
	// * Initializes viewer columns from preferences
	// *
	// * @param prefModel
	// */
	// private void updateColumnsFromPref(List<PreferenceModel.ColumnData>
	// colDataList){
	// resetColumns();
	// for(ColumnData item : colDataList){
	// if(item.visible){
	// addColumn(ColumnDesc.valueOf(item.id),item.pos);
	// }
	// }
	// }
	//
	// public TreeColumn addColumn(ColumnDesc desc){
	// return addColumn(desc,desc.getIndex());
	// }
	//
	// public TreeColumn addColumn(ColumnDesc desc, int index){
	// if(desc == null){
	// return null;
	// }
	// TreeColumn col = addColumn(desc, desc.label, SWT.LEFT,desc.style,index,
	// desc.width);
	// if(desc.isMovable()){
	// enableMoveable(col);
	// }
	// return col;
	// }
	//
	// /**
	// * Create columns from preferences or form default settings (values of
	// enum ColumnDesc)
	// * @param model PreferenceModel containing the actual column settings
	// */
	// public void createColumns(PreferenceModel model){
	// if(model != null && model.getColumnData().size() > 0){
	// // --- Create from preferences
	// update(model,false);
	// return;
	// }
	//
	// // Create from default definitions
	// ColumnDesc[] items = ColumnDesc.values();
	// for(ColumnDesc item : items){
	// if(item.visible == true){
	// addColumn(item);
	// }
	// }
	// if(model != null){
	// // Set actual column data to preferences
	// model.setColumnData(getColumnData());
	// }
	// }
	//
	// public void resetColumns(){
	// resetColumns(null);
	// }
	//
	// public void resetColumns(PreferenceModel model){
	// TreeColumn[] treeCols = viewer.getTree().getColumns();
	// disableMovable(treeCols);
	// for(TreeColumn item : treeCols){
	// item.dispose();
	// }
	// createColumns(model);
	// }
}
