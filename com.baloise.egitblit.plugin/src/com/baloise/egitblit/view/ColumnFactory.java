package com.baloise.egitblit.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

import com.baloise.egitblit.main.Activator;
import com.baloise.egitblit.pref.PreferenceModel;
import com.baloise.egitblit.pref.PreferenceModel.ColumnData;

/**
 * Helper / Factory for creating and maintaining repo viewer tree / columns
 * 
 * @author MicBag
 */
public class ColumnFactory{

	public final static String KEY_COLDESC = "ColumnDesc";

	private TreeViewer viewer;

	private List<TreeColumn> ctrlMoveList = new ArrayList<TreeColumn>();
	private List<TreeColumn> ctrlMoveStack = new ArrayList<TreeColumn>();
	private List<PreferenceModel.ColumnData> colInit = new ArrayList<PreferenceModel.ColumnData>();

	// ------------------------------------------------------------------------
	// Standard listeners
	// ------------------------------------------------------------------------
	DisposeListener diposeMoveListener = new DisposeListener() {
		@Override
		public void widgetDisposed(DisposeEvent e){
			if(e.widget instanceof TreeColumn){
				unregisterColumn((TreeColumn) e.widget);
			}
		}
	};

	ControlListener moveListener = new ControlListener() {
		@Override
		public void controlResized(ControlEvent e){
		}

		@Override
		public void controlMoved(ControlEvent e){
			int[] order = viewer.getTree().getColumnOrder();
			int[] newOrder = new int[order.length];
			int i = 0;
			// First column can't be moved and no other column can be placed at
			// pos 0
			newOrder[i++] = 0;
			for(int col : order){
				if(col != 0){
					newOrder[i++] = col;
				}
			}
			viewer.getTree().setColumnOrder(newOrder);
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

	/**
	 * Creates an table column for repo viewer
	 * 
	 * @param id
	 *            enum / id of the column
	 * @param label
	 *            column label
	 * @param style
	 *            style of the control (mostly SWT.LEFT or SWT.RIGHT)
	 * @param alignment
	 *            alignment of the column data (mostly SWT.LEFT or SWT.RIGHT)
	 * @param width
	 *            initial width of the column
	 * @return created column
	 */
	public TreeColumn addColumn(ColumnDesc id, String label, int style, int alignment, int width){
		TreeColumn col = new TreeColumn(viewer.getTree(), style);
		col.setAlignment(alignment);
		col.setText(label);
		col.setToolTipText(label);
		col.setWidth(width);
		col.setToolTipText(label);
		col.setData(KEY_COLDESC, id);
		return col;
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

	public final void enableMoveable(final TreeColumn... cols){
		if(cols == null || cols.length == 0){
			return;
		}
		ctrlMoveList.addAll(Arrays.asList(cols));
		ctrlMoveStack.removeAll(Arrays.asList(cols));

		for(TreeColumn col : cols){
			col.setMoveable(true);
			col.addControlListener(moveListener);
			col.addDisposeListener(diposeMoveListener);
		}
	}

	public final void disableMovable(TreeColumn... cols){
		if(cols == null || cols.length == 0){
			return;
		}

		ctrlMoveStack.addAll(Arrays.asList(cols));
		ctrlMoveList.removeAll(Arrays.asList(cols));
		for(TreeColumn col : cols){
			col.setMoveable(false);
			col.removeControlListener(moveListener);
			col.removeDisposeListener(diposeMoveListener);
		}
	}

	/**
	 * Enables or disables control listners
	 * 
	 * @param enable
	 *            true = enabled / false = disabled
	 * @return previous state
	 */
	public boolean enableControlListeners(boolean enable){
		if((ctrlMoveList.isEmpty() && enable == false) || (ctrlMoveStack.isEmpty() && enable == true)){
			// trying to enable or disable with no columns or invalid state
			return !ctrlMoveList.isEmpty();
		}

		if(enable == true){
			enableMoveable(ctrlMoveStack.toArray(new TreeColumn[ctrlMoveStack.size()]));
		}else{
			disableMovable(ctrlMoveList.toArray(new TreeColumn[ctrlMoveList.size()]));
		}
		return !ctrlMoveList.isEmpty();
	}

	/**
	 * @param toPrefFromPref
	 *            true = set current column data in Preference model, false =
	 *            init columns with pref settings
	 */
	public void updatePreferences(PreferenceModel model, boolean toPrefFromPref){
		captureColumnData(false);
		if(toPrefFromPref){
			model.setColumnData(getColumnData());
		}else{
			updateColumnsFromPref(model.getColumnData());
		}
	}

	public void captureColumnData(boolean force){
		if(force == false){
			if(colInit.isEmpty() == false){
				return;
			}
		}
		colInit.clear();
		colInit.addAll(getColumnData());
	}

	public void reset(){
		if(colInit.isEmpty()){
			return;
		}
		updateColumnsFromPref(colInit);
	}

	/**
	 * @return Actual column data
	 */
	private List<PreferenceModel.ColumnData> getColumnData(){
		enableControlListeners(false);

		List<PreferenceModel.ColumnData> colData = new ArrayList<PreferenceModel.ColumnData>();

		Tree tree = viewer.getTree();
		viewer.refresh(true);

		// Save all columns
		TreeColumn[] cols = tree.getColumns();
		String id;
		int pos, width;
		ColumnDesc colDesc;
		for(TreeColumn item : cols){
			colDesc = getColumnDesc(item);
			if(colDesc == null){
				Activator.logError("Error while saving prefernces. Viewer / column definition are out of sync. Missing column description.");
				return colInit;
			}
			id = colDesc.name();
			pos = viewer.getTree().indexOf(getColumn(colDesc));
			width = item.getWidth();
			colData.add(new PreferenceModel.ColumnData(id, pos, width));
		}
		
		enableControlListeners(true);
		return colData;
	}

	/**
	 * Initializes viewer columns from preferences
	 * 
	 * @param prefModel
	 */
	private void updateColumnsFromPref(List<PreferenceModel.ColumnData> colDataList){
		enableControlListeners(false);

		TreeColumn[] treeCols = viewer.getTree().getColumns(); // original order when building table

		int colSize = treeCols.length;
		int[] order = new int[colSize];
		for(int i = 0; i < colSize; i++){
			order[i] = -1;
		}
		
		TreeColumn item;
		ColumnDesc colDesc;
		ColumnData colData;
		for(int i = 0; i< colSize; i++){
			item = viewer.getTree().getColumn(i);
			colDesc = getColumnDesc(item); // Description of the item
			colData = PreferenceModel.getColumnData(colDataList, colDesc.name()); // Data with the new position
			if(colData == null || colData.pos > colSize){
				Activator.logWarn("Column index from preferences does not match with no. of columns. Continue anyway.");
				continue;
			}
			order[colData.pos] = i; // At new position pos, column i will be placed
			item.setWidth(colData.width);
		}

		for(int i = 0; i < colSize; i++){
			if(order[i] == -1){
				enableControlListeners(true);
				Activator.logError("Column index from preferences does not match with table. Missing column in preferences");
				return;
			}
		}
		viewer.getTree().setColumnOrder(order);
		enableControlListeners(true);
	}

	public void unregisterColumn(TreeColumn col){
		ctrlMoveList.remove(col);
		ctrlMoveStack.remove(col);
	}

	public void clear(){
		ctrlMoveList.clear();
		ctrlMoveStack.clear();
	}

	public void createColumns(){
		TreeColumn col;
		int width;
		ColumnDesc[] items = ColumnDesc.values();
		for(ColumnDesc item : items){
			width = item.visible == true ? item.width : 0;
			col = addColumn(item, item.label, SWT.LEFT,item.style,width);
			if(item.isMovable()){
				enableMoveable(col);
			}
		}
		captureColumnData(true);
	}
}
