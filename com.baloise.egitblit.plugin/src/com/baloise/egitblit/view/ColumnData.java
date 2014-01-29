package com.baloise.egitblit.view;

import org.eclipse.ui.IMemento;

import com.baloise.egitblit.main.Activator;

/**
 * Class holding column configuration (id, position and width)
 * @author MicBag
 *
 */
public class ColumnData{
	public final static String KEY_PREFIX = "com.baloise.egitblit";
	public final static String KEY_GITBLIT_COLUMN_DESC_POS = "column.desc.pos";
	public final static String KEY_GITBLIT_COLUMN_DESC_WIDTH = "column.desc.width";
	public final static String KEY_GITBLIT_COLUMN_DESC_VISIBLE = "column.desc.visible";

	public String id;
	public int pos;
	public int width;
	public boolean visible;
	
	public ColumnData(){
	}
	
	public ColumnData(String id, int pos, boolean visible, int width){
		this.id = id;
		this.pos = pos;
		this.visible = visible;
		this.width = width;
	}
	
	public void saveState(IMemento memento){
		if(memento == null){
			Activator.logError("Error saving column state. Missing call parameter.");
			return;
		}
		memento.putInteger(makeKey(KEY_GITBLIT_COLUMN_DESC_POS), this.pos);
		memento.putInteger(makeKey(KEY_GITBLIT_COLUMN_DESC_WIDTH), this.width);
		memento.putBoolean(makeKey(KEY_GITBLIT_COLUMN_DESC_VISIBLE), this.visible);
	}
	
	public boolean loadState(IMemento memento){
		if(memento == null){
			Activator.logError("Error saving column state. Missing call parameter.");
			return false;
		}
		
		ColumnDesc desc = ColumnDesc.valueOf(this.id);
		if(desc == null){
			Activator.logError("ColumnDesc for column " + this.id + " is not registered.");
			return false;
		}
		
		Integer val;
		Boolean b;
		val = memento.getInteger(makeKey(KEY_GITBLIT_COLUMN_DESC_POS));
		if(val == null){
			initDefault();
			return false;
		}
		this.pos = val;
		
		val = memento.getInteger(makeKey(KEY_GITBLIT_COLUMN_DESC_WIDTH));
		if(val == null){
			initDefault();
			return false;
		}
		this.width = val;
		
		b = memento.getBoolean(makeKey(KEY_GITBLIT_COLUMN_DESC_VISIBLE));
		if(b == null){
			initDefault();
			return false;
		}
		this.visible = b;
		return true;
	}
	
	
	protected void init(ColumnDesc desc){
		if(desc == null){
			Activator.logError("ColumnDesc for column " + this.id + " is not registered.");
			return;
		}
		this.pos = desc.getIndex();
		this.width = desc.width;
		this.visible = desc.visible;
		this.id = desc.name();
		
	}
	/**
	 * Init column data with default values
	 */
	private void initDefault(){
		init(ColumnDesc.valueOf(this.id));
	}
	
	private String makeKey(String propId) {
		return (KEY_PREFIX + "." + this.id + "." + propId);
	}

	@Override
	public String toString(){
		return "ColumnData [id=" + id + ", pos=" + pos + ", width=" + width + ", visible=" + visible + "]";
	}
}
