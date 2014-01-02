package com.baloise.egitblit.view;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

public class ColumnFactory{

	public final static String KEY_COLDESC = "ColumnDesc";
	
	public final static TreeColumn addColumn(ColumnDesc id, TreeViewer viewer, String label, int style, int alignment, int width){
		TreeColumn col  = new TreeColumn(viewer.getTree(), style);
		col.setAlignment(alignment);
		col.setText(label);
		col.setWidth(width);
		col.setToolTipText(label);
		col.setData(KEY_COLDESC,id);
		return col;
	}
	
	public final static int getColumnIndex(TreeViewer viewer, ColumnDesc desc){
		TreeColumn item = getColumn(viewer, desc);
		if(item == null){
			return -1;
		}
		return viewer.getTree().indexOf(item);
	}

	public final static ColumnDesc getColumnDesc(TreeColumn item){
		if(item == null){
			return null;
		}
		return (ColumnDesc) item.getData(KEY_COLDESC); 
	}

	public final static ColumnDesc getColumnDesc(TreeViewer viewer, int index){
		if(viewer == null){
			return null;
		}
		return getColumnDesc(viewer.getTree().getColumn(index));
		
	}

	public final static TreeColumn getColumn(TreeViewer viewer, ColumnDesc desc){
		if(viewer == null || desc == null){
			return null;
		}
		TreeColumn[] items = viewer.getTree().getColumns();
		for(TreeColumn item : items){
			if(desc == (ColumnDesc)item.getData(KEY_COLDESC)){
				return item;
			}
		}
		return null;
	}
			
}
