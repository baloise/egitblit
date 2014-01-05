package com.baloise.egitblit.view;

import org.eclipse.swt.SWT;

/**
 * Viewer column definition: Defines the columns properties
 * @author MicBag
 *
 */
public enum ColumnDesc{
	GroupRepository		("Group / Repository", "Name of the group and its repositories", true, 240, SWT.LEFT),
	Description			("Description", "Description of the repository", true, 180, SWT.LEFT | SWT.Move),
	Owner				("Owner", "Owner(s) of the repository", true, 120, SWT.LEFT | SWT.Move),
	LastChange			("Last Change", "Last change of the repository", true, 100, SWT.RIGHT | SWT.Move),
	LastChangeAuthor	("Last Change Author", "Person who has commited the last change", false, 120, SWT.LEFT | SWT.Move),
	Size				("Size", "Size of the repository", true, 80, SWT.RIGHT | SWT.Move),
	Server				("Gitblit Server", "Address of the hosting Gitblit server", true, 180, SWT.LEFT | SWT.Move),
	Group				("Group", "Group to which the repository belongs to", false, 120, SWT.LEFT | SWT.Move),
	IsFrozen			("Is Frozen", "Is frozen", false, 60, SWT.RIGHT | SWT.Move),
	IsFederated			("Is Federated", "Is federated", false, 80, SWT.RIGHT | SWT.Move),
	IsBare				("Is Bare", "Is bare", false, 60, SWT.RIGHT | SWT.Move),
	Frequency			("Frequency", "Frequency", false, 80, SWT.LEFT | SWT.Move),
	OriginRepository	("Origin Repository", "Origin repository", false, 120, SWT.LEFT | SWT.Move),
	Origin				("Origin","Origin", false, 120, SWT.LEFT | SWT.Move),
	Head				("Head", "Head", false, 120, SWT.LEFT | SWT.Move),
	AllowAuthenticated  ("Allow Authenticated", "Allow authenticated", false, 120, SWT.RIGHT | SWT.Move),
	AllowForks 			("Allow Forks", "Allow forks", false, 80, SWT.RIGHT | SWT.Move);


	final public boolean	visible; // Show initially
	final public int		width;  // Default width
	final public String		label;  // Default label
	final public String		toolTip;// Default tooltip
	final public int		style;  // Column SWT style

	ColumnDesc(String label, String tooltip, boolean doShowColumn, int width, int style){
		this.label = label;
		this.toolTip = tooltip;
		this.visible = doShowColumn;
		this.width = width;
		this.style = style;
	}

	public boolean isMovable(){
		return(SWT.Move == (this.style & SWT.Move));
	}
	
	

}
