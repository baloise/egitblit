package com.baloise.egitblit.view;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.Command;
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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.forms.IMessage;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.baloise.egitblit.common.GitBlitExplorerException;
import com.baloise.egitblit.gitblit.GitBlitBD;
import com.baloise.egitblit.gitblit.GitBlitRepository;
import com.baloise.egitblit.gitblit.GitBlitServer;
import com.baloise.egitblit.gitblit.ProgressToken;
import com.baloise.egitblit.main.Activator;
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
	private List<GroupViewModel> rootModel = new ArrayList<GroupViewModel>();
	private StyledLabelProvider labelProvider;
	private DoubleClickBehaviour dbclick = DoubleClickBehaviour.OpenGitBlit;

	private PreferenceModel prefModel = null;
	private List<GitBlitServer> serverList = null;
	private Action refreshAction;

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

	/**
	 * Action shown as menu item in form header to omit unavailable servers
	 */
	private OmitAction omitAction = new OmitAction();

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

	// ------------------------------------------------------------------------
	public RepoExplorerView(){
	}

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

		final FormToolkit ftk = new FormToolkit(parent.getDisplay());
		parent.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e){
				ftk.dispose();
			}
		});

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

		// --- Use Formtoolkit, because of header preparing
		this.form = ftk.createForm(parent);
		form.setText("GitBlit Repository Explorer");
		GridDataFactory.fillDefaults().grab(true, true).applyTo(form);
		ftk.decorateFormHeading(form);

		final ImageDescriptor refreskImgDesc = getImageFromPlugin("refresh_tab.gif");

		// --------------------------------------------------------------------
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

		this.form.getToolBarManager().add(refreshAction);
		this.form.getToolBarManager().update(true);

		IMenuManager hmgr = form.getMenuManager();
		hmgr.add(omitAction);
		omitAction.setChecked(this.prefModel.isOmitServerErrors());
		hmgr.update(true);

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
					String labelText = labelProvider.getColumnText((GitBlitViewModel) element, columnIndex);
					isMatch |= wordMatches(labelText);
				}
				return isMatch;
			}
		};

		// --------------------------------------------------------------------
		FilteredTree filteredTree = new FilteredTree(comp, SWT.FULL_SELECTION | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL, filter, true);
		viewer = filteredTree.getViewer();

		// viewer = new TreeViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(new RepoContentProvider());
		this.labelProvider = new StyledLabelProvider(viewer);
		viewer.setLabelProvider(this.labelProvider);

		// --------------------------------------------------------------------
		// --- table columns
		// --------------------------------------------------------------------
		l = GridLayoutFactory.swtDefaults().create();
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		gd.horizontalAlignment = SWT.FILL;
		gd.verticalAlignment = SWT.FILL;
		viewer.getControl().setLayoutData(gd);

		viewer.getTree().setHeaderVisible(true);
		viewer.getTree().setLinesVisible(true);

		TreeColumn colGroup = new TreeColumn(viewer.getTree(), SWT.LEFT);
		colGroup.setAlignment(SWT.LEFT);
		colGroup.setText("Group / Repository");
		colGroup.setWidth(240);

		TreeColumn colDesc = new TreeColumn(viewer.getTree(), SWT.LEFT);
		colDesc.setAlignment(SWT.LEFT);
		colDesc.setText("Description");
		colDesc.setWidth(180);

		TreeColumn ownerDesc = new TreeColumn(viewer.getTree(), SWT.LEFT);
		ownerDesc.setAlignment(SWT.LEFT);
		ownerDesc.setText("Owner");
		ownerDesc.setWidth(120);

		TreeColumn changeDesc = new TreeColumn(viewer.getTree(), SWT.LEFT);
		changeDesc.setAlignment(SWT.RIGHT);
		changeDesc.setText("Last Change");
		changeDesc.setWidth(100);

		TreeColumn sizeDesc = new TreeColumn(viewer.getTree(), SWT.LEFT);
		sizeDesc.setAlignment(SWT.RIGHT);
		sizeDesc.setText("Size");
		sizeDesc.setWidth(80);

		TreeColumn serverDesc = new TreeColumn(viewer.getTree(), SWT.LEFT);
		serverDesc.setAlignment(SWT.LEFT);
		serverDesc.setText("Gitblit Server");
		serverDesc.setWidth(180);

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

		// Final assembling & initialization
		viewer.getControl().setMenu(mgr.createContextMenu(viewer.getControl()));
		viewer.setSorter(new RepoViewSorter());
		viewer.setInput(rootModel);
		ColumnViewerToolTipSupport.enableFor(viewer);
		loadRepositories(true);
	}

	public final static String CMD_EGIT_PUSH = "org.eclipse.egit.ui.team.Push";

	// org.eclipse.egit.ui.team.Push handler
	private final static Command getEGitCommand(String cmd){
		ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getService(ICommandService.class);
		if(commandService == null){
			// Eclipse not ready
			return null;
		}
		return commandService.getCommand(cmd);
	}

	private void setRepoList(final List<GroupViewModel> list){
		rootModel.clear();
		rootModel.addAll(list);
		viewer.refresh(true);
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

	/**
	 * Gets the {@link ImageDescriptor} of the passed image The image must be
	 * located in the "/icons/" folder of the source project
	 * 
	 * @param name
	 *            Name of the image
	 * @return {@link ImageDescriptor}
	 */
	private final static ImageDescriptor getImageFromPlugin(String name){
		return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "/icons/" + name);
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
								pModel.setToolTip("Repository has no commits. Can´t show repository summary in GitBlit");
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
