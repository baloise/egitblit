package com.baloise.egitblit.view;

import static com.baloise.egitblit.common.GitBlitRepository.GROUP_MAIN;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
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

import com.baloise.egitblit.common.GitBlitBD;
import com.baloise.egitblit.common.GitBlitRepository;
import com.baloise.egitblit.main.Activator;
import com.baloise.egitblit.main.EclipseLog;
import com.baloise.egitblit.main.GitBlitExplorerException;
import com.baloise.egitblit.pref.PreferenceMgr;
import com.baloise.egitblit.pref.PreferenceModel;
import com.baloise.egitblit.pref.PreferenceModel.DoubleClickBehaviour;
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

	private boolean isDoubleClickOpenGit = false;
	private Image imgRefesh = null;

	// ------------------------------------------------------------------------
	// --- Actions
	// ------------------------------------------------------------------------
	// Copy selected Project to clipboard
	Action actionCopyClipboard = null;
	// Open gitblit summary
	private Action actionOpenGitBlit = null;

	// ------------------------------------------------------------------------
	// Separate instance to unregiter listener at dispose cycle
	IPropertyChangeListener propChangeListener = new IPropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent event){
			String prop = event.getProperty();
			if(prop != null && prop.startsWith(PreferenceMgr.KEY_GITBLIT_ROOT) && viewer != null){
				initViewModel();
			}
		}
	};

	// ------------------------------------------------------------------------
	// ------------------------------------------------------------------------
	public RepoExplorerView(){
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	@Override
	public void dispose(){
		super.dispose();
		Activator.getDefault().getPreferenceStore().removePropertyChangeListener(propChangeListener);
		if(imgRefesh != null){
			imgRefesh.dispose();
			imgRefesh = null;
		}
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
		initDoubleBehaviour();
		Activator.getDefault().getPreferenceStore().addPropertyChangeListener(propChangeListener);
		PreferenceMgr.isValidConfig();

		final FormToolkit ftk = new FormToolkit(parent.getDisplay()); 
		parent.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				ftk.dispose();
			}
		});
		
		// --- Use Formtoolkit, because of header preparing
		Form form = ftk.createForm(parent);
		form.setText("GitBlit Repository Explorer");
		GridDataFactory.fillDefaults().grab(true, true).applyTo(form);
		ftk.decorateFormHeading(form);
		
		final ImageDescriptor refreskImgDesc = getImageFromPlugin("refresh_tab.gif");
		if(refreskImgDesc != null){
			imgRefesh = refreskImgDesc.createImage();
		}

		Action rAction = new Action("Reload Repositories"){
			@Override
			public void run(){
				initViewModel();
			}

			@Override
			public ImageDescriptor getImageDescriptor(){
				return refreskImgDesc;
			}
			
		};
		form.getToolBarManager().add(rAction);
		form.getToolBarManager().update(true);
		
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
			// Copied from http://eclipsesource.com/blogs/2012/10/26/filtering-tables-in-swtjface/
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
		viewer.setLabelProvider(new RepoLabelProvider());

		this.actionOpenGitBlit = new OpenGitBlitAction(viewer);
		this.actionCopyClipboard = new CopyClipBoardAction(viewer);

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
		colGroup.setWidth(280);

		TreeColumn colDesc = new TreeColumn(viewer.getTree(), SWT.LEFT);
		colDesc.setAlignment(SWT.LEFT);
		colDesc.setText("Description");
		colDesc.setWidth(320);

		// --------------------------------------------------------------------
		// Adding viewer interaction
		// --------------------------------------------------------------------
		int operations = DND.DROP_COPY | DND.DROP_MOVE;
		Transfer[] transferTypes = new Transfer[] { TextTransfer.getInstance() };
		viewer.addDragSupport(operations, transferTypes, new RepoDragListener(viewer));
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event){
				getDoubleClickAction(true).run();
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
							mgr.add(getDoubleClickAction(false));
						}
					}
				}
			}
		});

		viewer.getControl().setMenu(mgr.createContextMenu(viewer.getControl()));

		initViewModel();
	}

	private final static ImageDescriptor getImageFromPlugin(String name){
		return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "/icons/" + name);
	}

	private void initViewModel(){
		rootModel = readRepositories(true);
		if(viewer != null){
			viewer.setInput(rootModel);
		}
		initDoubleBehaviour();
	}

	/**
	 * Init double click behaviour based on preference settings
	 */
	private void initDoubleBehaviour(){
		try{
			PreferenceModel pref = PreferenceMgr.readConfig();
			if(pref == null || DoubleClickBehaviour.OpenGitBlit.equals(pref.getDoubleClick())){
				isDoubleClickOpenGit = true;
			}else{
				isDoubleClickOpenGit = false;
			}
			return;
		}catch(GitBlitExplorerException e){
			EclipseLog.error("Error initializing view with preference settings.", e);
		}
		isDoubleClickOpenGit = true;
	}

	private class Holder<T> {
		public T value;

		public Holder(T value){
			this.value = value;
		}
	};

	/**
	 * Reading repos / projects from gitblit
	 * 
	 * @return
	 */
	private List<GroupViewModel> readRepositories(final boolean reload){
		final Holder<Boolean> noAccess = new Holder<Boolean>(Boolean.FALSE);

		final List<GroupViewModel> modelList = new ArrayList<GroupViewModel>();
		BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
			public void run(){
				try{
					Map<String, List<GitBlitRepository>> groupMap = readGroups();
					if(groupMap == null || groupMap.isEmpty()){
						final String msg = "No GitBlit repositories defined. Please add a GitBlit server location via preferences.";
						showMessage(IStatus.INFO, msg);
						modelList.add(new ErrorViewModel(msg));
						return;
					}
					GroupViewModel gModel;
					for(String groupName : groupMap.keySet()){
						// Adding group
						gModel = new GroupViewModel(groupName);
						modelList.add(gModel);

						// Adding childs of group
						List<GitBlitRepository> pList = groupMap.get(groupName);
						for(GitBlitRepository item : pList){
							gModel.addChild(new ProjectViewModel(item));
						}
					}
				}catch(Exception e){
					EclipseLog.error("Error reading project from GitBlit", e);
					showMessage(IStatus.ERROR, "Error reading project from GitBlit\n" + e.toString());
					modelList.add(new ErrorViewModel("Error reading projects from GitBlit. Check your preference settings, please."));
				}
			}
		});

		if(noAccess.value){
			modelList.add(new ErrorViewModel("Please configure GitBlit Explorer in preferences, first."));
		}
		return modelList;
	}

	/**
	 * Read repositories and sort them by group
	 * 
	 * @return Map of repositories by group name
	 * @throws GitBlitExplorerException
	 * @throws Exception
	 */
	public Map<String, List<GitBlitRepository>> readGroups() throws GitBlitExplorerException{
		Map<String, List<GitBlitRepository>> res = new TreeMap<String, List<GitBlitRepository>>();

		// Read preferences
		PreferenceModel prefModel = PreferenceMgr.readConfig();

		// Get GitBlit BD
		GitBlitBD bd = new GitBlitBD(prefModel.getServerList());
		List<GitBlitRepository> projList = bd.readRepositories();

		for(GitBlitRepository item : projList){
			if(item.groupName == null)
				item.groupName = GROUP_MAIN;
			List<GitBlitRepository> rlist = res.get(item.groupName);
			if(rlist == null){
				rlist = new ArrayList<GitBlitRepository>();
				res.put(item.groupName, rlist);
			}
			if(rlist.contains(item) == false){
				rlist.add(item);
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
	public Action getDoubleClickAction(boolean dclick){
		if(dclick){
			if(isDoubleClickOpenGit){
				return actionOpenGitBlit;
			}else{
				return actionCopyClipboard;
			}
		}else{
			if(isDoubleClickOpenGit){
				return actionCopyClipboard;
			}else{
				return actionOpenGitBlit;
			}
		}
	}

	private void showMessage(final int level, final String msg){
		getSite().getShell().getDisplay().asyncExec(new Runnable() {
			public void run(){
				MessageDialog.open(level, getSite().getShell(), "GitBlit Explorer", msg, SWT.NONE);
			}
		});
	}

}
