package com.baloise.egitblit.view.model;

/**
 * Interface which defines common methods of tree view models
 * @author MicBag
 *
 */
public interface GitBlitViewModel{

	/**
	 * @return name of the entry. To be displayed in viewer cell
	 */
	public String getName();
	
	
	/**
	 * @return Tooltip for the cell
	 */
	public String getToolTip();
	/**
	 * @param model building a tree: Parent of this entry
	 */
	public void setParent(GitBlitViewModel model);
	/**
	 * @return Parent of this entry. If not present, returns null
	 */
	public GitBlitViewModel getParent();
}
