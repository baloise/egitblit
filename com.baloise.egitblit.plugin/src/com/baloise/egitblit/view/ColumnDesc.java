package com.baloise.egitblit.view;

import org.eclipse.swt.SWT;

/**
 * Viewer column definition: Defines the columns properties
 * 
 * @author MicBag
 * 
 */
public enum ColumnDesc{
	GroupRepository			("Group / Repository", "Name of the group and its repositories", true, 240, SWT.LEFT),
	Description				("Description", "Description of the repository", true, 180, SWT.LEFT | SWT.Move),
	Owner					("Owner", "Owner(s) of the repository", true, 120, SWT.LEFT | SWT.Move),
	LastChange				("Last Change", "Last change of the repository", true, 100, SWT.RIGHT | SWT.Move),
	LastChangeAuthor		("Last Change Author", "Person who has commited the last change", false, 120, SWT.LEFT | SWT.Move),
	Size					("Size", "Size of the repository", true, 80, SWT.RIGHT | SWT.Move),
	Server					("Gitblit Server", "Address of the hosting Gitblit server", true, 180, SWT.LEFT | SWT.Move),
	Group					("Group", "Group to which the repository belongs to", false, 120, SWT.LEFT | SWT.Move),
	IsFrozen				("Is Frozen", "Is frozen", false, 60, SWT.RIGHT | SWT.Move),
	IsFederated				("Is Federated", "Is federated", false, 80, SWT.RIGHT | SWT.Move),
	IsBare					("Is Bare", "Is bare", false, 60, SWT.RIGHT | SWT.Move),
	HasCommits				("Has Commits", "Has commits", false, 60, SWT.RIGHT | SWT.Move),
	Frequency				("Frequency", "Frequency", false, 80, SWT.LEFT | SWT.Move),
	OriginRepository		("Origin Repository", "Origin repository", false, 120, SWT.LEFT | SWT.Move),
	Origin					("Origin", "Origin", false, 120, SWT.LEFT | SWT.Move),
	Head					("Head", "Head", false, 120, SWT.LEFT | SWT.Move),
	ProjectPath				("Project Path", "Project path", false, 180, SWT.LEFT | SWT.Move),
	GitUrl					("Git Url", "Git Url", false, 320, SWT.LEFT | SWT.Move),
	AllowAuthenticated		("Allow Authenticated", "Allow authenticated", false, 120, SWT.RIGHT | SWT.Move),
	AllowForks				("Allow Forks", "Allow forks", false, 80, SWT.RIGHT | SWT.Move),

	ShowRemoteBranches		("Show Remote Branches", "Show remote branches", false, 80, SWT.RIGHT | SWT.Move),
	UseIncrementalPushTags	("Use Incremental Push Tags", "Use incremental push tags", false, 80, SWT.RIGHT | SWT.Move),
	IncrementalPushTagPrefix("IncrementalPushTagPrefix", "IncrementalPushTagPrefix", false, 120, SWT.LEFT | SWT.Move),
	AccessRestriction		("Access Restriction", "AccessRestriction", false, 120, SWT.LEFT | SWT.Move),
	AuthorizationControl	("AuthorizationControl", "AuthorizationControl", false, 120, SWT.LEFT | SWT.Move),
	FederationStrategy		("Federation Strategy", "Federation strategy", false, 80, SWT.RIGHT | SWT.Move),
	FederationSets			("Federation Sets", "Federation sets", false, 120, SWT.RIGHT | SWT.Move),
	skipSizeCalculation		("Skip Size Calculation", "Skip size calculation", false, 80, SWT.RIGHT | SWT.Move),
	AvailableRefs			("Available Refs", "Available refs", false, 120, SWT.LEFT | SWT.Move),
	IndexedBranches			("Indexed Branches", "Indexed branches", false, 120, SWT.RIGHT | SWT.Move),
	PreReceiveScripts		("Pre Receive Scripts", "Pre receive scripts", false, 120, SWT.RIGHT | SWT.Move),
	PostReceiveScripts		("Post Receive Scripts", "Post receive scripts", false, 120, SWT.RIGHT | SWT.Move),
	MailingLists			("Mailing Lists", "Mailing lists", false, 120, SWT.RIGHT | SWT.Move),
	CustomFields			("Custom Fields", "Custom fields", false, 120, SWT.RIGHT | SWT.Move),
	Forks					("Forks", "Forks", false, 120, SWT.RIGHT | SWT.Move),
	VerifyCommitte			("Verify Committe", "Verify committe", false, 80, SWT.RIGHT | SWT.Move),
	GCThreshold				("GC Threshold", "GC threshold", false, 80, SWT.RIGHT | SWT.Move),
	GcPeriod				("GC Period", "GC period", false, 80, SWT.RIGHT | SWT.Move),
	SkipSummaryMetrics		("SkipSummaryMetrics", "SkipSummaryMetrics", false, 80, SWT.RIGHT | SWT.Move),
	MaxActivityCommits		("MaxActivityCommits", "MaxActivityCommits", false, 120, SWT.RIGHT | SWT.Move),
	MetricAuthorExclusions	("MetricAuthorExclusions", "MetricAuthorExclusions", false, 120, SWT.LEFT | SWT.Move),

	IsCollectingGarbage		("IsCollectingGarbage", "IsCollectingGarbage", false, 80, SWT.RIGHT | SWT.Move),
	LastGC					("Last GC", "Last GC", false, 80, SWT.RIGHT | SWT.Move),
	SparkleshareId			("Sparkle Share Id", "Sparkle share id", false, 80, SWT.LEFT | SWT.Move);

	final public boolean	visible;	// Show initially
	final public int		width;		// Default width
	final public String		label;		// Default label
	final public String		toolTip;	// Default tooltip
	final public int		style;		// Column SWT style

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
	
	public int getIndex(){
		ColumnDesc[] items = values();
		for(int i=0; i < items.length;i++){
			if(this.equals(items[i])){
				return i;
			}
		}
		return -1;
	}
}
