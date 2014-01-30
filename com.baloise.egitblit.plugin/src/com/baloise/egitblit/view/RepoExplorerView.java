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
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.contexts.IContextService;
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
import com.baloise.egitblit.view.action.ActionFactory;
import com.baloise.egitblit.view.action.BrowseAction;
import com.baloise.egitblit.view.action.CloneAction;
import com.baloise.egitblit.view.action.CloneOneClickAction;
import com.baloise.egitblit.view.action.CopyAction;
import com.baloise.egitblit.view.action.ViewActionBase;
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

	public final static String CONTEXT_ID = "com.baloise.egitblit.context";
	
	public final static String KEY_GITBLIT_OMIT_SERVER_ERROR = "com.baloise.egitblit.view.omitservererror";
	public final static String KEY_GITBLIT_SHOW_GROUPS = "com.baloise.egitblit.view.showgroups";
	/**
	 * Enum containing the display modes (Grouped or only the repos)
	 * 
	 * @author MicBag
	 * 
	 */
	private enum ViewMode{
		Group, Repository
	};

	/**
	 * Wrapper which prepares the viewer model depending on the ViewMode
	 * 
	 * @author MicBag
	 * 
	 */
	private class ViewerData{
		private List<GroupViewModel>	groupModelList	= new ArrayList<GroupViewModel>();
		private ViewMode				viewMode		= ViewMode.Group;

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
			}else{
				mode = ViewMode.Group;
			}
			setViewMode(mode);
			return mode;
		}

		public List<GitBlitViewModel> getModel(){
			switch(viewMode){
				case Group:
					return (List) this.groupModelList;
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
				list.addAll((List) ((GroupViewModel) item).getChilds());
			}
			return list;
		}

		/**
		 * Prepare view depending on the current config setting E.g. displaying
		 * and hiding columns, Setting column labels etc.
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
				TreeColumn col = colFactory.getColumn(ColumnDesc.GroupRepository);
				if(col == null){
					return;
				}
				if(viewMode == ViewMode.Repository){
					// Showing repositories only (no groups)
					col.setText("Repository");
					col.setToolTipText("Name of the repository");
				}else{
					// Showing groups
					col.setText("Group / Repository");
					col.setToolTipText("Name of the group and its repositories");
				}
			}
		}

	};

	// ------------------------------------------------------------------------
	// --- Variables
	// ------------------------------------------------------------------------
	private TreeViewer				viewer;
	private ColumnFactory			colFactory;
	private ActionFactory   		actionFactory;
	private IMemento				memento = null;
	private ViewerData				viewData;		
	private StyledLabelProvider		labelProvider;
	private DoubleClickBehaviour	dbclick				= DoubleClickBehaviour.OpenGitBlit;
	private PreferenceModel			prefModel			= new PreferenceModel();
	private Action					refreshAction;
	private ExpandCItem				expandCItem;

	private Form					form;
	private Boolean					omitServerErrors	= null;

	// ------------------------------------------------------------------------
	// --- Local actions
	// ------------------------------------------------------------------------
	private class OmitAction extends Action{
		@Override
		public void runWithEvent(Event event){
			if(omitServerErrors == null){
				omitServerErrors = true;
			}
			omitServerErrors = !omitServerErrors;
			setChecked(omitServerErrors);
			try{
				prefModel.setOmitServerErrors(omitServerErrors);
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
	private OmitAction	omitAction	= new OmitAction();

	/**
	 * Internal class for showing messages at the form header
	 * 
	 * @author MicBag
	 */
	private class ServerMsg implements IMessage{
		private String	msg;
		private int		type	= IMessageProvider.NONE;

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
	// Subscribe on preference changes
	IPropertyChangeListener	propChangeListener	= new IPropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent event){
			String prop = event.getProperty();
			if(prop != null && prop.startsWith(PreferenceMgr.KEY_GITBLIT_ROOT) && viewer != null){
				initPreferences(true);
				loadRepositories(false);
			}
		}
	};

	/**
	 * ...guess
	 */
	public RepoExplorerView(){
	}

	/*
	 * (non-Javadoc)
	 * 
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
	 * @see org.eclipse.ui.part.ViewPart#saveState(org.eclipse.ui.IMemento)
	 */
	@Override
	public void saveState(IMemento memento){
		super.saveState(memento);
		saveViewState(memento);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite, org.eclipse.ui.IMemento)
	 */
	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException{
		super.init(site, memento);
		this.memento = memento;
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
		
		// --- Register context to separate keybindings form standard eclipse (see plugin.xml)
		IContextService contextService = (IContextService)getSite().getService(IContextService.class);
		contextService.activateContext(CONTEXT_ID);
		
		// --------------------------------------------------------------------
		// Sync preferences
		// --------------------------------------------------------------------
		Activator.getDefault().getPreferenceStore().addPropertyChangeListener(propChangeListener);
		initPreferences(false);

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
		// --- sync with EGit: Remote repository related action will force a
		// reload
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

		this.form.getBody().setLayout(l);
		this.form.getBody().setLayoutData(gd);

		Composite comp = ftk.createComposite(form.getBody(), SWT.NONE);
		comp.setLayout(l);
		comp.setLayoutData(gd);

		// Init viewer
		createViewer(comp);

		// --------------------------------------------------------------------
		// --- Actions
		// --------------------------------------------------------------------
		final ImageDescriptor refreskImgDesc = Activator.getImageDescriptor(SharedImages.Refresh);
		this.refreshAction = new Action("Reload Repositories") {
			@Override
			public void run(){
				loadRepositories(true);
			}

			@Override
			public ImageDescriptor getImageDescriptor(){
				return refreskImgDesc;
			}
		};

		this.expandCItem = new ExpandCItem(viewer);
		final ImageDescriptor treeMode = Activator.getImageDescriptor(SharedImages.TreeMode);
		Action hideGroupsAllAction = new Action("Show groups or projects only", SWT.TOGGLE) {
			@Override
			public void run(){
				viewer.getTree().setRedraw(false);
				ViewMode mode = viewData.switchMode();
				prefModel.setShowGroups(ViewMode.Group == mode);
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

		IMenuManager hmgr = this.form.getMenuManager();
		hmgr.add(this.omitAction);
		this.omitAction.setChecked(this.omitServerErrors);
		final Action prefAction = new Action("Open preferences"){
			@Override
			public void runWithEvent(Event event){
				ViewActionBase.openPreferences();
			}
		};
		hmgr.add(prefAction);

		hmgr.add(new Separator());
		final ImageDescriptor desc = Activator.getSharedImageDescriptor(ISharedImages.IMG_DEF_VIEW);
		hmgr.add(new Action("Configure table columns", desc) {
			@Override
			public void runWithEvent(Event event){
				Shell shell = new Shell(event.widget.getDisplay());
				shell.setImage(desc.createImage());
				shell.setText(getText());
				colFactory.update(prefModel, true);
				ColumnConfigDialog dialog = new ColumnConfigDialog(shell, prefModel);
				dialog.create();
				Window.setDefaultImage(Activator.getSharedImage(ISharedImages.IMG_DEF_VIEW));
				if(dialog.open() == Window.OK){
					prefModel.setColumnData(dialog.getColumns());
					colFactory.update(prefModel, false);
				}
			}
		});

		hmgr.add(new Action("Reset table columns", refreskImgDesc) {
			@Override
			public void runWithEvent(Event event){
				colFactory.createColumns(null);
			}
		});

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
						mgr.add(actionFactory.getAction(CloneAction.ID));
						mgr.add(actionFactory.getAction(CloneOneClickAction.ID));
						mgr.add(new Separator());
						mgr.add(actionFactory.getAction(BrowseAction.ID));
						mgr.add(new Separator());
						mgr.add(actionFactory.getAction(CopyAction.ID));
					}
				}
			}
		});

		// --------------------------------------------------------------------
		// Final assembling & initialization
		// --------------------------------------------------------------------
		this.viewer.getControl().setMenu(mgr.createContextMenu(viewer.getControl()));
		ColumnViewerToolTipSupport.enableFor(viewer);

		// Preparing the model: Initializing viewData wrapper and loading
		// repositories, sync related ui controls
		this.viewData = new ViewerData();
		loadRepositories(false);

		boolean mode = this.prefModel.isShowGroups();
		hideGroupsAllAction.setChecked(mode);
		if(mode == true){
			viewData.setViewMode(ViewMode.Group);
		}else{
			viewData.setViewMode(ViewMode.Repository);
		}
	}

	/**
	 * Initializes the viewer
	 * 
	 * @param parent
	 */
	private void createViewer(Composite parent){
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
					TreeColumn item = treeViewer.getTree().getColumn(columnIndex);
					if(item != null && item.getWidth() == 0){
						// column is invisible: Ignore
						continue;
					}
					String labelText = labelProvider.getColumnText((GitBlitViewModel) element, colFactory.getColumnDesc(columnIndex));
					isMatch |= wordMatches(labelText);
				}
				return isMatch;
			}
		};
		// --------------------------------------------------------------------
		// Create Viewer
		// --------------------------------------------------------------------
		FilteredTree filteredTree = new FilteredTree(parent, SWT.FULL_SELECTION | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL, filter, true);
		viewer = filteredTree.getViewer();
		colFactory = new ColumnFactory(viewer);

		// --------------------------------------------------------------------
		// Create columns from preferences
		// --------------------------------------------------------------------
		loadViewState();
		colFactory.createColumns(prefModel);

		TreeColumn[] a = viewer.getTree().getColumns();
		// viewer = new TreeViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(new RepoContentProvider());
		this.labelProvider = new StyledLabelProvider(viewer);
		viewer.setLabelProvider(this.labelProvider);

		// --------------------------------------------------------------------
		// --- Init view settings (after all components are created
		// --------------------------------------------------------------------
		this.dbclick = prefModel.getDoubleClick();
		this.labelProvider.setDecorateLabels(prefModel.isDecorateView());

		// --------------------------------------------------------------------
		// --- Viewer layout
		// --------------------------------------------------------------------
		GridData gd = GridDataFactory.swtDefaults().create();
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		gd.horizontalAlignment = SWT.FILL;
		gd.verticalAlignment = SWT.FILL;

		viewer.getControl().setLayoutData(gd);
		viewer.getTree().setHeaderVisible(true);
		viewer.getTree().setLinesVisible(true);

		// --------------------------------------------------------------------
		// Drag support
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
		
		this.actionFactory = new ActionFactory(this);
		actionFactory.addAction(new BrowseAction(viewer));
		actionFactory.addAction(new CloneAction(viewer));
		actionFactory.addAction(new CloneOneClickAction(viewer));
		actionFactory.addAction(new CopyAction(viewer));
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
	private void initPreferences(boolean setFields){
		try{
			// --- Load settings
			this.prefModel = PreferenceMgr.readConfig();
			loadViewState();

			// --- Init variables
			this.dbclick = prefModel.getDoubleClick();
			if(this.omitServerErrors == null){
				this.omitServerErrors = prefModel.isOmitServerErrors();
			}
			
			if(setFields){
				// --- Init preference related fields
				if(this.omitAction != null){
					this.omitAction.setChecked(this.omitServerErrors);
				}
				if(this.labelProvider != null){
					this.labelProvider.setDecorateLabels(this.prefModel.isDecorateView());
				}
			}
			return;
		}catch(GitBlitExplorerException e){
			Activator.logError("Error initializing view with preference settings.", e);
		}
	}

	private List<String> getOmittedServerErrorUrls(){
		List<String> res = new ArrayList<String>();
		for(GitBlitServer item :  prefModel.getServerList()){
			if(item.serverError == true && res.contains(item.url) == false){
				res.add(item.url);
			}
		}
		return res;
	}



	/**
	 * Reading repos / projects from gitblit
	 * 
	 * @return reload Reload serverList
	 */
	private void loadRepositories(boolean reload){
		if(reload){
			PreferenceMgr.readServerList(this.prefModel);
		}

		Job job = new Job("Gitblit Repository Explorer") {
			@Override
			protected IStatus run(IProgressMonitor monitor){
				final List<GroupViewModel> modelList = new ArrayList<GroupViewModel>();
				try{
					final IProgressMonitor fmon = monitor;

					// --- Read preferences
					int size = prefModel.getServerList().size();
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
					GitBlitBD bd = new GitBlitBD(prefModel.getServerList());
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
					modelList.add(new ErrorViewModel("Error reading repositories from Gitblit."));
					List<GitBlitServer> list = prefModel.getServerList();
					for(GitBlitServer item : list){
						if(item.active && item.serverError){
							modelList.add(new ErrorViewModel("Server \"" + item.url + "\" is unavailable."));
						}
					}
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
				setHeaderMessage(getOmittedServerErrorUrls());
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
				if(this.omitServerErrors == false){
					this.form.setMessage("There is one unavailable Gitblit server", status, msgList.toArray(new ServerMsg[msgs.size()]));
				}
				else{
					this.form.setMessage("There is one omitted Gitblit server", status, msgList.toArray(new ServerMsg[msgs.size()]));					
				}
			}else{
				if(this.omitServerErrors == false){
					this.form.setMessage("There are unavailable Gitblit servers", status, msgList.toArray(new ServerMsg[msgs.size()]));
				}
				else{
					this.form.setMessage("There are " + msgs.size() + " omitted Gitblit servers", status, msgList.toArray(new ServerMsg[msgs.size()]));
				}
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
				return this.actionFactory.getAction(BrowseAction.ID);
			case CopyUrl:
				return this.actionFactory.getAction(CopyAction.ID);
			case PasteEGit:
				return this.actionFactory.getAction(CloneAction.ID);
			default:
				return this.actionFactory.getAction(CloneAction.ID);
		}
	}

	public void saveViewState(IMemento memento){
		if(this.memento == null){
			return;
		}
		colFactory.update(this.prefModel, true);
		
		this.colFactory.update(this.prefModel, true);
		List<ColumnData> list = this.prefModel.getColumnData();
		for(ColumnData item : list){
			item.saveState(memento);
		}
		memento.putBoolean(KEY_GITBLIT_OMIT_SERVER_ERROR, this.prefModel.isOmitServerErrors());
		memento.putBoolean(KEY_GITBLIT_SHOW_GROUPS, this.prefModel.isShowGroups());
	}
	
	public void loadViewState(){
		if(this.memento == null || this.prefModel == null){
			return;
		}
		List<ColumnData> list = ColumnFactory.createColumnData();
		this.prefModel.setColumnData(list);
		for(ColumnData item : list){
			item.loadState(this.memento);
		}
		Boolean val = memento.getBoolean(KEY_GITBLIT_OMIT_SERVER_ERROR);
		this.prefModel.setOmitServerErrors(val != null ? val : true);
		
		val = memento.getBoolean(KEY_GITBLIT_SHOW_GROUPS);
		this.prefModel.setShowGroups(val != null ? val : true);
	}
}
