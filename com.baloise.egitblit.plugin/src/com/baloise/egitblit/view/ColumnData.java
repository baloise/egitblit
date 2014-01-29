package com.baloise.egitblit.view;

/**
 * Class holding column configuration (id, position and width)
 * @author MicBag
 *
 */
public class ColumnData{
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

	@Override
	public String toString(){
		return "ColumnData [id=" + id + ", pos=" + pos + ", width=" + width + ", visible=" + visible + "]";
	}
	
}