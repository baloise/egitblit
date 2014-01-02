package com.baloise.egitblit.view;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.egit.ui.JobFamilies;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.forms.IMessage;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.ViewPart;

import com.baloise.egitblit.common.GitBlitExplorerException;
import com.baloise.egitblit.gitblit.GitBlitBD;
import com.baloise.egitblit.gitblit.GitBlitRepository;
import com.baloise.egitblit.gitblit.GitBlitServer;
import com.baloise.egitblit.gitblit.ProgressToken;
import com.baloise.egitblit.main.Activator;
import com.baloise.egitblit.main.SharedImages;
import com.baloise.egitblit.pref.PreferenceMgr;
import com.baloise.egitblit.pref.PreferenceModel;
import com.baloise.egitblit.pref.PreferenceModel.DoubleClickBehaviour;
import com.baloise.egitblit.view.action.BrowseAction;
import com.baloise.egitblit.view.action.CloneAction;
import com.baloise.egitblit.view.action.CloneOneClickAction;
import com.baloise.egitblit.view.action.CopyAction;
import com.baloise.egitblit.view.model.ErrorViewModel;
import com.baloise.egitblit.view.model.GitBlitViewModel;
import com.baloise.egitblit.view.model.GroupViewModel;
import com.baloise.egitblit.view.model.ProjectViewModel;

/**
 * View showing GitBlit Repositories by group
 * 
 * @author MicBag
 * 
 */
public class RepoExplorerView extends ViewPart{

	private TreeViewer viewer;

	/**
	 * Enum containing the display modes (Grouped or only the repos)
	 * @author MicBag
	 *
	 */
	private enum ViewMode{
		Group,
		Repository
	};

	/**
	 * Wrapper which prepares the viewer model depending on the ViewMode
	 * @author MicBag
	 *
	 */
	private class ViewerData{
		private List<GroupViewModel> groupModelList = new ArrayList<GroupViewModel>();
		private ViewMode viewMode = ViewMode.Group;
		private int columGroupWidth = 120;
		public ViewerData(){
		}
		
		public void setGroupModel(List<GroupViewModel> list){
			this.groupModelList.clear();
			this.groupModelList.addAll(list);
			update();
		}
		
		public void setViewMode(ViewMode mode){
			this.viewMode = mode;
			update();
		}
		
		public ViewMode switchMode(){
			ViewMode mode;
			if(this.viewMode == ViewMode.Group){
				mode = ViewMode.Repository;
			}
			else{
				mode = ViewMode.Group;
			}
			setViewMode(mode);
			return mode;
		}

		public List<GitBlitViewModel> getModel(){
			switch(viewMode){
				case Group:
					return (List)this.groupModelList;
				case Repository:
					return getProjects();
			}
			return new ArrayList<GitBlitViewModel>();
		}
		
		public List<GitBlitViewModel> getProjects(){
			List<GitBlitViewModel> list = new ArrayList<GitBlitViewModel>();
			for(GitBlitViewModel item : this.groupModelList){
				if(item instanceof ErrorViewModel){
					list.add(item);
					continue;
				}
				list.addAll((List)((GroupViewModel)item).getChilds());
			}
			return list;
		}
		
		/**
		 * Prepare view depending on the current config setting
		 * E.g. displaying and hiding columns, Setting column labels etc.
		 */
		public void update(){
			if(viewer != null){
				// Set the model to display
				viewer.setInput(getModel());
				viewer.refresh(true);
				if(expandCItem != null){
					// sync set the viewmode button
					expandCItem.enable(viewMode == ViewMode.Group);
				}
				
				if(viewMode == ViewMode.Repository){
					// Showing repositories only (no groups)
					if(columGroupWidth < 1){
						columGroupWidth = 120;
					}
					columnGroup.setWidth(columGroupWidth);
					columnGroup.setResizable(true);
					columnGroupRepo.setText("Repository");
					columnGroupRepo.setToolTipText("Name of the repository");
				}
				else{
					// Showing groups
					columGroupWidth = columnGroup.getWidth();
					columnGroup.setWidth(0);
					columnGroup.setResizable(false);
					columnGroupRepo.setText("Group / Repository");
					columnGroupRepo.setToolTipText("Name of the group and its repositories");
				}
			}
		}
		
	};
	
	// ------------------------------------------------------------------------
	// --- Variables
	// ------------------------------------------------------------------------
	private ViewerData viewData;	// Model Wrapper
	private RepoViewSorter repoViewSorter;
	private TreeColumn columnGroupRepo; // Needed to change column text and tooltip
	private TreeColumn columnGroup;
	private StyledLabelProvider labelProvider; 
	private DoubleClickBehaviour dbclick = DoubleClickBehaviour.OpenGitBlit;
	private PreferenceModel prefModel = null;
	private List<GitBlitServer> serverList = null;
	private Action refreshAction;
	private ExpandCItem expandCItem;

	private Form form;
	private boolean omitServerErrors = false;

	// ------------------------------------------------------------------------
	// --- Local actions
	// ------------------------------------------------------------------------
	private class OmitAction extends Action{
		@Override
		public void run(){
			omitServerErrors = !omitServerErrors;
			setChecked(omitServerErrors);
			try{
				prefModel.setOmitServerErrors(omitServerErrors);
				PreferenceMgr.saveConfig(prefModel);
			}catch(Exception e){
				Activator.logError("Error saving preference settings.", e);
			}
			loadRepositories(!omitServerErrors);
		}

		public void refreshLabel(){
			setText("Ignore unavailable servers");
		}

		@Override
		public ImageDescriptor getImageDescriptor(){
			return null;
		}
	};
	/**
	 * Action shown as menu item in form header to omit unavailable servers
	 */
	private OmitAction omitAction = new OmitAction();


	/**
	 * Internal class for showing messages at the form header
	 * 
	 * @author MicBag
	 */
	private class ServerMsg implements IMessage{

		private String msg;
		private int type = IMessageProvider.NONE;

		public ServerMsg(String msg, int type){
			setMessage(msg, type);
		}

		public void setMessage(String msg, int type){
			this.msg = msg;
			this.type = type;
		}

		@Override
		public String getMessage(){
			return msg;
		}

		@Override
		public int getMessageType(){
			return this.type;
		}

		@Override
		public Object getKey(){
			return null;
		}

		@Override
		public Object getData(){
			return null;
		}

		@Override
		public Control getControl(){
			return null;
		}

		@Override
		public String getPrefix(){
			return null;
		}
	};

	// ------------------------------------------------------------------------
	// Separate instance to unregiter listener at dispose cycle
	IPropertyChangeListener propChangeListener = new IPropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent event){
			String prop = event.getProperty();
			if(prop != null && prop.startsWith(PreferenceMgr.KEY_GITBLIT_ROOT) && viewer != null){
				loadRepositories(true);
			}
		}
	};

	/**
	 * ...guess 
	 */
	public RepoExplorerView(){
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	@Override
	public void dispose(){
		Activator.getDefault().getPreferenceStore().removePropertyChangeListener(propChangeListener);
		super.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets
	 * .Composite)
	 */
	@Override
	public void createPartControl(Composite parent){

		// --------------------------------------------------------------------
		// Sync preferences
		// --------------------------------------------------------------------
		Activator.getDefault().getPreferenceStore().addPropertyChangeListener(propChangeListener);
		initPreferences();

		// --------------------------------------------------------------------
		// --- FormToolkit
		// --------------------------------------------------------------------
		final FormToolkit ftk = new FormToolkit(parent.getDisplay());
		parent.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e){
				ftk.dispose();
			}
		});

		// --------------------------------------------------------------------
		// --- sync with EGit: Remote repository related action will force a reload
		// --------------------------------------------------------------------
		IJobManager jobMan = Job.getJobManager();
		jobMan.addJobChangeListener(new IJobChangeListener() {
			@Override
			public void sleeping(IJobChangeEvent event){
			}

			@Override
			public void scheduled(IJobChangeEvent event){
			}

			@Override
			public void running(IJobChangeEvent event){
			}

			@Override
			public void done(IJobChangeEvent event){
				Job job = event.getJob();
				if(job != null && job.belongsTo(JobFamilies.PUSH) == true){
					loadRepositories(true);
				}
			}

			@Override
			public void awake(IJobChangeEvent event){
			}

			@Override
			public void aboutToRun(IJobChangeEvent event){
			}
		});


		// --------------------------------------------------------------------
		// --- Use Formtoolkit, because of header preparing
		// --------------------------------------------------------------------
		this.form = ftk.createForm(parent);
		form.setText("GitBlit Repository Explorer");
		GridDataFactory.fillDefaults().grab(true, true).applyTo(form);
		ftk.decorateFormHeading(form);

		// --------------------------------------------------------------------
		// Layout
		// --------------------------------------------------------------------
		GridLayout l = GridLayoutFactory.swtDefaults().create();
		GridData gd = GridDataFactory.swtDefaults().create();
		l.numColumns = 1;
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		gd.horizontalAlignment = SWT.FILL;
		gd.verticalAlignment = SWT.FILL;

		form.getBody().setLayout(l);
		form.getBody().setLayoutData(gd);

		Composite comp = ftk.createComposite(form.getBody(), SWT.NONE);
		comp.setLayout(l);
		comp.setLayoutData(gd);

		// Init viewer
		initViewer(comp);

		// --------------------------------------------------------------------
		// --- Actions
		// --------------------------------------------------------------------
		final ImageDescriptor refreskImgDesc = Activator.getImageDescriptor(SharedImages.Refresh);
		refreshAction = new Action("Reload Repositories") {
			@Override
			public void run(){
				loadRepositories(true);
			}

			@Override
			public ImageDescriptor getImageDescriptor(){
				return refreskImgDesc;
			}
		};
		
		expandCItem = new ExpandCItem(viewer);
		final ImageDescriptor treeMode = Activator.getImageDescriptor(SharedImages.TreeMode);
		Action hideGroupsAllAction = new Action("Hide Groups",SWT.TOGGLE) {
			@Override
			public void run(){
				viewer.getTree().setRedraw(false);
				ViewMode mode = viewData.switchMode();
				prefModel.setShowGroups(ViewMode.Group == mode);
				try{
					PreferenceMgr.saveConfig(prefModel);
				}catch(Exception e){
					Activator.logError("Error saving preference settings.", e);
				}
				viewer.refresh(true);
				viewer.getTree().setRedraw(true);
			}

			@Override
			public ImageDescriptor getImageDescriptor(){
				return treeMode;
			}
		};
		
		hideGroupsAllAction.setChecked(true);
		this.form.getToolBarManager().add(expandCItem);
		this.form.getToolBarManager().add(hideGroupsAllAction);
		this.form.getToolBarManager().add(refreshAction);
		this.form.getToolBarManager().update(true);

		IMenuManager hmgr = form.getMenuManager();
		hmgr.add(omitAction);
		omitAction.setChecked(this.prefModel.isOmitServerErrors());
		hmgr.update(true);

		// --------------------------------------------------------------------
		// Context menu of a selected row
		// --------------------------------------------------------------------
		final MenuManager mgr = new MenuManager();
		mgr.setRemoveAllWhenShown(true);

		mgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager){
				IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
				if(selection != null){
					GitBlitViewModel model = (GitBlitViewModel) selection.getFirstElement();
					if(model instanceof ProjectViewModel){
						ProjectViewModel pm = (ProjectViewModel) model;
						if(model != null && pm.getGitURL() != null && pm.getGitURL().trim().isEmpty() == false){
							if(CloneAction.getEGitCommand() != null){
								mgr.add(new CloneAction(viewer));
								mgr.add(new CloneOneClickAction(viewer));
								mgr.add(new Separator());
							}
							if(pm.hasCommits() == true){
								mgr.add(new BrowseAction(viewer));
								mgr.add(new Separator());
							}
							mgr.add(new CopyAction(viewer));

						}

					}
				}
			}
		});

		// --------------------------------------------------------------------
		// Final assembling & initialization
		// --------------------------------------------------------------------
		viewer.getControl().setMenu(mgr.createContextMenu(viewer.getControl()));
		ColumnViewerToolTipSupport.enableFor(viewer);
		initTreeSorter();

		// Preparing the model: Initializing viewData wrapper and loading repositories, sync related ui controls
		this.viewData = new ViewerData();
		loadRepositories(true);
		boolean mode = this.prefModel.isShowGroups();
		hideGroupsAllAction.setChecked(mode);
		if(mode == true){
			viewData.setViewMode(ViewMode.Group);
			columnGroup.setWidth(0);
		}
		else{
			viewData.setViewMode(ViewMode.Repository);
			columnGroup.setWidth(120);
		}

	}
	
	/**
	 * Initializes the viewer
	 * @param parent
	 */
	private void initViewer(Composite parent){
		// --------------------------------------------------------------------
		// Viewer
		// --------------------------------------------------------------------
		PatternFilter filter = new PatternFilter() {
			// Copied from
			// http://eclipsesource.com/blogs/2012/10/26/filtering-tables-in-swtjface/
			protected boolean isLeafMatch(final Viewer viewer, final Object element){
				TreeViewer treeViewer = (TreeViewer) viewer;
				int numberOfColumns = treeViewer.getTree().getColumnCount();
				RepoLabelProvider labelProvider = (RepoLabelProvider) treeViewer.getLabelProvider();
				boolean isMatch = false;
				for(int columnIndex = 0; columnIndex < numberOfColumns; columnIndex++){
					String labelText = labelProvider.getColumnText((GitBlitViewModel) element, ColumnFactory.getColumnDesc(treeViewer,columnIndex));
					isMatch |= wordMatches(labelText);
				}
				return isMatch;
			}
		};
		// --------------------------------------------------------------------
		FilteredTree filteredTree = new FilteredTree(parent, SWT.FULL_SELECTION | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL, filter, true);
		viewer = filteredTree.getViewer();

		// viewer = new TreeViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(new RepoContentProvider());
		this.labelProvider = new StyledLabelProvider(viewer);
		viewer.setLabelProvider(this.labelProvider);

		// --------------------------------------------------------------------
		// --- table columns
		// --------------------------------------------------------------------
		//GridLayout l = GridLayoutFactory.swtDefaults().create();
		
		GridData gd = GridDataFactory.swtDefaults().create();
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		gd.horizontalAlignment = SWT.FILL;
		gd.verticalAlignment = SWT.FILL;
		viewer.getControl().setLayoutData(gd);

		viewer.getTree().setHeaderVisible(true);
		viewer.getTree().setLinesVisible(true);
		
		// IMPORTANT: See ColumnDesc Enum for column order
		columnGroupRepo = ColumnFactory.addColumn(ColumnDesc.GroupRepository, viewer, "Group / Repository", SWT.LEFT, SWT.LEFT, 240);
		columnGroupRepo.setToolTipText("Name of the group and its repositories");

		TreeColumn col = ColumnFactory.addColumn(ColumnDesc.Description, viewer, "Description", SWT.LEFT, SWT.LEFT, 180);
		col.setToolTipText("Description of the repository");

		col = ColumnFactory.addColumn(ColumnDesc.Description, viewer, "Owner", SWT.LEFT, SWT.LEFT, 120);
		col.setToolTipText("Owner(s) of the repository");

		col = ColumnFactory.addColumn(ColumnDesc.LastChange, viewer, "Last Change", SWT.LEFT, SWT.RIGHT, 100);
		col.setToolTipText("Last change of the repository");

		col = ColumnFactory.addColumn(ColumnDesc.Size, viewer, "Size", SWT.LEFT, SWT.RIGHT, 80);
		col.setToolTipText("Size of the repository");

		col = ColumnFactory.addColumn(ColumnDesc.Server, viewer, "Gitblit Server", SWT.LEFT, SWT.LEFT, 180);
		col.setToolTipText("Address of the hosting Gitblit server");

		columnGroup = ColumnFactory.addColumn(ColumnDesc.Group, viewer, "Group", SWT.LEFT, SWT.LEFT, 0);
		columnGroup.setToolTipText("Group of the repository");

		// --------------------------------------------------------------------
		// Adding viewer interaction
		// --------------------------------------------------------------------
		int operations = DND.DROP_COPY | DND.DROP_MOVE;
		Transfer[] transferTypes = new Transfer[] { TextTransfer.getInstance() };
		viewer.addDragSupport(operations, transferTypes, new RepoDragListener(viewer));
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event){
				getDoubleClickAction().run();
			}
		});
	}

	private void initTreeSorter(){
		repoViewSorter = new RepoViewSorter();
		viewer.setSorter(repoViewSorter);
		viewer.getTree().setSortColumn(viewer.getTree().getColumn(0));
		viewer.getTree().setSortDirection(SWT.UP);
		viewer.refresh();
		
		TreeColumn[] cols = viewer.getTree().getColumns();
		for(final TreeColumn item : cols){
			item.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e){
					TreeColumn col = viewer.getTree().getSortColumn();
					viewer.getTree().setSortColumn(item);
					if(item.equals(col)){
						int dir = viewer.getTree().getSortDirection() == SWT.UP ? SWT.DOWN : SWT.UP;
						viewer.getTree().setSortDirection(dir);
					}
					viewer.refresh();
				}
			});
		}
	}

	private void setRepoList(final List<GroupViewModel> list){
		if(this.viewData == null){
			this.viewData = new ViewerData();
		}
		this.viewData.setGroupModel(list);
	}

	/**
	 * Loads & sync the preferences with view
	 */
	private void initPreferences(){
		try{
			this.prefModel = PreferenceMgr.readConfig();
			if(prefModel != null){
				dbclick = prefModel.getDoubleClick();
				omitServerErrors = prefModel.isOmitServerErrors();
				if(labelProvider != null){
					labelProvider.setDecorateLabels(prefModel.isColorColumns());
				}
			}
			return;
		}catch(GitBlitExplorerException e){
			Activator.logError("Error initializing view with preference settings.", e);
		}
	}

	private List<String> getOmittedServerUrls(){
		List<String> res = new ArrayList<String>();
		for(GitBlitServer item : this.serverList){
			if(item.serverError == true){
				res.add(item.url);
			}
		}
		return res;
	}

	/**
	 * Init the server list to use from preferences
	 * 
	 * @param reload
	 */
	private void initServerList(boolean reload){
		List<GitBlitServer> slist = null;

		if(reload == true){
			// Reload server definitions to do a full refresh
			initPreferences();
		}
		try{
			slist = this.prefModel.getServerList();
		}catch(Exception e){
			Activator.logError("Error reading preferences", e);
			return;
		}
		if(reload == true || this.serverList == null || this.serverList.isEmpty() || omitServerErrors == false){
			this.serverList = slist;
			// return with initial list & state
			return;
		}
	}

	/**
	 * Reading repos / projects from gitblit
	 * 
	 * @return reload Currently not supported
	 */
	private void loadRepositories(boolean reload){
		initServerList(reload);
		
		Job job = new Job("Gitblit Repository Explorer") {
			@Override
			protected IStatus run(IProgressMonitor monitor){
				final List<GroupViewModel> modelList = new ArrayList<GroupViewModel>();
				try{
					final IProgressMonitor fmon = monitor;

					// --- Read preferences
					int size = serverList.size();
					monitor.beginTask("Reading repositories form Gitblit Server", size + 1);

					ProgressToken token = new ProgressToken() {
						@Override
						public void startWork(String msg){
							fmon.subTask(msg);
						}

						@Override
						public void endWork(){
							fmon.worked(1);
						}

						@Override
						public boolean isCanceled(){
							return fmon.isCanceled();
						}
					};

					// --- Reading & prepare grouped
					GitBlitBD bd = new GitBlitBD(serverList);
					List<GitBlitRepository> projList = bd.readRepositories(token, true, omitServerErrors);

					// Not canceling here to prepare the result which we have
					// received so far
					fmon.subTask("Preparing result.");

					// --- prepare & get groups
					List<String> groupNames = prepareGroups(projList);
					if(groupNames.isEmpty()){
						String msg = "No GitBlit server is active or all servers are unreachable.";
						Activator.showInfo(msg);
						modelList.add(new ErrorViewModel(msg));
						syncWithUi(modelList);
						return Status.CANCEL_STATUS;
					}
					// Prepare group & child repos
					GroupViewModel gModel;
					ProjectViewModel pModel;
					List<GitBlitRepository> grList;
					for(String item : groupNames){
						// Get repos by group
						grList = getReposByGroup(projList, item);
						gModel = new GroupViewModel(item);
						modelList.add(gModel);
						// add childs
						for(GitBlitRepository pitem : grList){
							pModel = new ProjectViewModel(pitem);
							if(pitem.hasCommits == false){
								pModel.setToolTip("Repository has no commits. Can�t show repository summary in GitBlit");
							}
							gModel.addChild(pModel);
						}
					}
					fmon.worked(1);
					syncWithUi(modelList);
					return Status.OK_STATUS;
				}catch(Exception e){
					Activator.showAndLogError("", e);
					modelList.add(new ErrorViewModel("Error reading projects from Gitblit. Check your preference settings.."));
					syncWithUi(modelList);
					return Status.CANCEL_STATUS;
				}
			}
		};

		job.addJobChangeListener(new IJobChangeListener() {
			@Override
			public void sleeping(IJobChangeEvent event){
			}

			@Override
			public void scheduled(IJobChangeEvent event){
				enableRefreshButton(false);
			}

			@Override
			public void running(IJobChangeEvent event){
			}

			@Override
			public void done(IJobChangeEvent event){
				enableRefreshButton(true);
			}

			@Override
			public void awake(IJobChangeEvent event){
			}

			@Override
			public void aboutToRun(IJobChangeEvent event){
			}
		});
		job.schedule();
	}

	/**
	 * Enables / disables refresh button while performing an refresh action
	 * 
	 * @param enalbe
	 *            true = enabled, false = nope
	 */
	private void enableRefreshButton(final boolean enable){
		if(this.form == null || this.form.isDisposed() == true){
			// Sometimes, this happns. Grrr.. don't know how and why (but always
			// when preferences is open while applying changes)
			Activator.logError("Form is disposed. Can't refresh ui fields. State of ui is undefined");
			// Activator.showInfo("An internal error occured. \nPlease use the refesh button or reopen the Gitblit repository explorer view");
			return;
		}
		this.form.getDisplay().asyncExec(new Runnable() {
			public void run(){
				if(refreshAction != null){
					refreshAction.setEnabled(enable);
					form.setBusy(!enable);
				}
			}
		});
	}

	/**
	 * Sync ui fields out of another thread
	 */
	private void syncWithUi(final List<GroupViewModel> list){
		if(this.form == null || this.form.isDisposed() == true){
			// Sometimes, this happns. Grrr.. don't know how and why (but always
			// when preferences is open while applying changes)
			Activator.logError("Form is disposed. Can't refresh ui fields. State of ui is undefined");
			// Activator.showInfo("An internal error occured. \nPlease use the refesh button or reopen the Gitblit repository explorer view");
			return;
		}

		this.form.getDisplay().asyncExec(new Runnable() {
			public void run(){
				omitAction.refreshLabel();
				setRepoList(list);
				setHeaderMessage(getOmittedServerUrls());
			}
		});
	}

	/**
	 * Set a header message
	 * 
	 * @param msgs
	 *            messages to display
	 */
	private void setHeaderMessage(List<String> msgs){
		// setBusy needed. Otherwise the menu at the form title will be disposed
		// ...strange!? Maybe eclipse bug or just "RFM"
		boolean orgState = this.form.isBusy();
		this.form.setBusy(true);
		if(msgs != null && msgs.size() > 0){
			List<ServerMsg> msgList = new ArrayList<ServerMsg>();
			for(String item : msgs){
				msgList.add(new ServerMsg(item + " is unavailable", IMessageProvider.INFORMATION));
			}
			int status = IMessageProvider.ERROR;
			if(omitServerErrors == true){
				status = IMessageProvider.WARNING;
			}
			if(msgs.size() == 1){
				this.form.setMessage("There is one omitted Gitblit server", status, msgList.toArray(new ServerMsg[msgs.size()]));
			}else{
				this.form.setMessage("There are " + msgs.size() + " omitted Gitblit servers", status, msgList.toArray(new ServerMsg[msgs.size()]));
			}
		}else{
			this.form.setMessage(null);
		}
		this.form.setBusy(orgState);
	}

	/**
	 * Extract the list of groups of the passed repos
	 * 
	 * @param list
	 * @return
	 */
	private List<String> prepareGroups(List<GitBlitRepository> list){
		List<String> res = new ArrayList<String>();
		if(list == null){
			return res;
		}

		for(GitBlitRepository item : list){
			if(item.groupName == null){
				// Set defaults
				item.groupName = GitBlitRepository.GROUP_MAIN;
			}
			if(res.contains(item.groupName) == false){
				res.add(item.groupName);
			}
		}
		return res;
	}

	/**
	 * Get repositories by group
	 * 
	 * @param list
	 * @param groupName
	 * @return
	 */
	private List<GitBlitRepository> getReposByGroup(List<GitBlitRepository> list, String groupName){
		List<GitBlitRepository> res = new ArrayList<GitBlitRepository>();
		if(list == null || list.isEmpty()){
			return res;
		}
		if(groupName == null){
			res.addAll(list);
			return res;
		}
		for(GitBlitRepository item : list){
			if(groupName.equalsIgnoreCase(item.groupName)){
				if(res.contains(item) == false){
					res.add(item);
				}
			}
		}
		return res;
	}

	@Override
	public void setFocus(){
	}

	/**
	 * Determinate double click action, based on preferences
	 * 
	 * @param dclick
	 * @return
	 */
	public Action getDoubleClickAction(){
		switch(dbclick){
			case OpenGitBlit:
				return new BrowseAction(this.viewer);
			case CopyUrl:
				return new CopyAction(this.viewer);
			case PasteEGit:
				return new CloneAction(this.viewer);
			default:
				return new CloneAction(this.viewer);
		}
	}
}
