package com.baloise.egitblit.view;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
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
import com.baloise.egitblit.main.EclipseHelper;
import com.baloise.egitblit.pref.PreferenceMgr;
import com.baloise.egitblit.pref.PreferenceModel;
import com.baloise.egitblit.pref.PreferenceModel.DoubleClickBehaviour;
import com.baloise.egitblit.view.action.CopyClipBoardAction;
import com.baloise.egitblit.view.action.OpenGitBlitAction;
import com.baloise.egitblit.view.action.PasteToEGitAction;
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
	private List<GroupViewModel> rootModel;
	private RepoLabelProvider labelProvider;
	private DoubleClickBehaviour dbclick = DoubleClickBehaviour.OpenGitBlit;

	private PreferenceModel prefModel = null;
	private List<GitBlitServer> serverList = null;

	private boolean omitServerErrors = false;
	private boolean coloringColumns = false;

	private class OmitAction extends Action{
		@Override
		public void run(){
			omitServerErrors = !omitServerErrors;
			setChecked(omitServerErrors);
			try{
				prefModel.setOmitServerErrors(omitServerErrors);
				PreferenceMgr.saveConfig(prefModel);
			}catch(Exception e){
				EclipseHelper.logError("Error saving preference settings.", e);
			}
			loadRepositories(!omitServerErrors);
		}

		public void refreshLabel(){
			setText("Ignore unavailable servers" + " (" + getOmittedServerSize() + " servers are omitted)");
		}

		@Override
		public ImageDescriptor getImageDescriptor(){
			return null;
		}
	};

	private OmitAction omitAction = new OmitAction();

	// ------------------------------------------------------------------------
	// --- Actions
	// ------------------------------------------------------------------------

	// ------------------------------------------------------------------------
	// Separate instance to unregiter listener at dispose cycle
	IPropertyChangeListener propChangeListener = new IPropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent event){
			String prop = event.getProperty();
			if(prop != null && prop.startsWith(PreferenceMgr.KEY_GITBLIT_ROOT) && viewer != null){
				initPreferences();
				loadRepositories(true);
				
			}
		}
	};

	// ------------------------------------------------------------------------
	public RepoExplorerView(){
	}

	@Override
	public void dispose(){
		super.dispose();
	}

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

		// --- Use Formtoolkit, because of header preparing
		Form form = ftk.createForm(parent);
		form.setText("GitBlit Repository Explorer");
		GridDataFactory.fillDefaults().grab(true, true).applyTo(form);
		ftk.decorateFormHeading(form);

		final ImageDescriptor refreskImgDesc = getImageFromPlugin("refresh_tab.gif");

		Action rAction = new Action("Reload Repositories") {
			@Override
			public void run(){
				loadRepositories(true);
			}

			@Override
			public ImageDescriptor getImageDescriptor(){
				return refreskImgDesc;
			}
		};
		form.getToolBarManager().add(rAction);
		omitAction.setChecked(this.prefModel.isOmitServerErrors());
		form.getToolBarManager().update(true);

		IMenuManager hmgr = form.getMenuManager();
		hmgr.add(omitAction);
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
				ITableLabelProvider labelProvider = (ITableLabelProvider) treeViewer.getLabelProvider();
				boolean isMatch = false;
				for(int columnIndex = 0; columnIndex < numberOfColumns; columnIndex++){
					String labelText = labelProvider.getColumnText(element, columnIndex);
					isMatch |= wordMatches(labelText);
				}
				return isMatch;
			}
		};

		FilteredTree filteredTree = new FilteredTree(comp, SWT.FULL_SELECTION | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL, filter, true);
		viewer = filteredTree.getViewer();

		// viewer = new TreeViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(new RepoContentProvider());
		this.labelProvider = new RepoLabelProvider(viewer);
		viewer.setLabelProvider(this.labelProvider);

		l = GridLayoutFactory.swtDefaults().create();
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		gd.horizontalAlignment = SWT.FILL;
		gd.verticalAlignment = SWT.FILL;
		viewer.getControl().setLayoutData(gd);

		viewer.getTree().setHeaderVisible(true);

		TreeColumn colGroup = new TreeColumn(viewer.getTree(), SWT.LEFT);
		viewer.getTree().setLinesVisible(true);
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
		changeDesc.setText("LastChange");
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
							if(PasteToEGitAction.getEGitCommand() != null){
								mgr.add(new PasteToEGitAction(viewer));
								mgr.add(new Separator());
							}
							if(pm.hasCommits() == true){
								mgr.add(new OpenGitBlitAction(viewer));
								mgr.add(new Separator());
							}
							mgr.add(new CopyClipBoardAction(viewer));

						}
					}
				}
			}
		});

		viewer.getControl().setMenu(mgr.createContextMenu(viewer.getControl()));
		viewer.setSorter(new RepoViewSorter());

		ColumnViewerToolTipSupport.enableFor(viewer);

		loadRepositories(true);
	}

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
			EclipseHelper.logError("Error initializing view with preference settings.", e);
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

	private int getOmittedServerSize(){
		int val = 0;
		for(GitBlitServer item : this.serverList){
			if(item.serverError == true){
				val++;
			}
		}
		return val;
	}

	private void initServerList(boolean reload){
		List<GitBlitServer> slist = null;

		if(reload == true){
			// Reload server definitions to do a full refresh
			initPreferences();
		}
		try{
			slist = this.prefModel.getServerList();
		}catch(Exception e){
			EclipseHelper.logError("Error reading preferences", e);
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
		this.omitAction.refreshLabel();
		
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
					};

					// --- Reading & prepare grouped
					GitBlitBD bd = new GitBlitBD(serverList);
					List<GitBlitRepository> projList = bd.readRepositories(token, true, omitServerErrors);

					fmon.subTask("Preparing result.");

					// --- prepare & get groups
					List<String> groupNames = prepareGroups(projList);
					if(groupNames.isEmpty()){
						final String msg = "No GitBlit server is active or all servers are unreachable. Please add and/or activate a GitBlit server via preferences.";
						EclipseHelper.showInfo(msg);
						modelList.add(new ErrorViewModel(msg));
						rootModel = modelList;
						syncWithUi();
						return Status.CANCEL_STATUS;
					}
					// Prepare group & child repos
					GroupViewModel gModel;
					ProjectViewModel pModel;
					List<GitBlitRepository> grList;
					for(String item : groupNames){
						// Get repos by group
						grList = getReposByGroup(projList,item);
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
					rootModel = modelList;
					syncWithUi();
					return Status.OK_STATUS;
				}catch(Exception e){
					EclipseHelper.showAndLogError("", e);
					modelList.add(new ErrorViewModel("Error reading projects from Gitblit. Check your preference settings.."));
					rootModel = modelList;
					syncWithUi();
					return Status.CANCEL_STATUS;
				}
			}
		};
		job.schedule();
	}
	
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
	
	private void syncWithUi(){
		Display.getDefault().asyncExec(new Runnable() {
			public void run(){
				omitAction.refreshLabel();
				viewer.setInput(rootModel);
			}
		});
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
				return new OpenGitBlitAction(this.viewer);
			case CopyUrl:
				return new CopyClipBoardAction(this.viewer);
			case PasteEGit:
				return new PasteToEGitAction(this.viewer);
			default:
				return new PasteToEGitAction(this.viewer);
		}
	}
}
