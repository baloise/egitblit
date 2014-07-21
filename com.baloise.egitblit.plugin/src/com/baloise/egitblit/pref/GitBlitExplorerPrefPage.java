package com.baloise.egitblit.pref;

import java.net.URL;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

import com.baloise.egitblit.common.GitBlitExplorerException;
import com.baloise.egitblit.gitblit.GitBlitServer;
import com.baloise.egitblit.main.Activator;
import com.baloise.egitblit.pref.PreferenceModel.DoubleClickBehaviour;
import com.baloise.egitblit.view.action.BrowseAction;
import com.baloise.egitblit.view.action.CloneAction;
import com.baloise.egitblit.view.action.CloneOneClickAction;
import com.baloise.egitblit.view.action.CopyAction;

/**
 * Preference Page Page showing the configuration settings of git blit explorer
 * 
 * @author MicBag
 * 
 */
public class GitBlitExplorerPrefPage extends PreferencePage implements IWorkbenchPreferencePage{

	public final static String ID = "com.baloise.egitblit.pref";
	public final static String URL_GITHUB_ISSUE = "https://github.com/baloise/egitblit/issues";

	private PreferenceModel prefModel;
	private TableViewer viewer;
	private Button btOpenGitBlit;
	private Button btCopyUrl;
	private Button btEGitPaste;
	private Button btColViewer;

	private Button btWSGroupName;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	@Override
	public void init(IWorkbench workbench){
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Configure GitBlit Explorer");
	}

	@Override
	protected Control createContents(Composite parent){

		// Root composite which contains all gui elements
		Composite root = new Composite(parent, SWT.NONE);

		// ...do a little bit layout
		GridLayout l = GridLayoutFactory.swtDefaults().create();
		l.numColumns = 2;
		l.marginWidth = 0;
		root.setLayout(l);

		GridData gd = GridDataFactory.swtDefaults().create();
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		gd.horizontalAlignment = SWT.FILL;
		gd.verticalAlignment = SWT.FILL;

		// Table which shows all server properties
		this.viewer = new TableViewer(root, SWT.CHECK | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		Table table = this.viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setLayout(l);
		table.setLayoutData(gd);

		this.viewer.setSorter(new ViewerSorter() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2){
				if(viewer == null || e1 instanceof GitBlitServer == false || e2 instanceof GitBlitServer == false){
					return super.compare(viewer, e1, e2);
				}
				String v1 = ((GitBlitServer)e1).url;
				v1 = v1 == null ? "" : v1;
				return v1.compareTo(((GitBlitServer)e2).url);
			}
		});

		// --- add activation check box
		TableViewerColumn colActive = createColumn(this.viewer, "Active", 50);
		colActive.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element){
				return "";
			}
		});

		// --- add url column
		TableViewerColumn colURL = createColumn(this.viewer, "URL", 150);
		colURL.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element){
				if(element instanceof GitBlitServer){
					return ((GitBlitServer)element).url;
				}
				return "";
			}
		});

		// --- add user column
		TableViewerColumn colUser = createColumn(this.viewer, "User", 100);
		colUser.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element){
				if(element instanceof GitBlitServer){
					return ((GitBlitServer)element).user;
				}
				return "";
			}
		});

		// --- add masked password column
		TableViewerColumn colPwd = createColumn(this.viewer, "Password", 100);
		colPwd.getColumn().setText("Password");
		colPwd.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element){
				if(element instanceof GitBlitServer){
					String txt = ((GitBlitServer)element).password;
					if(txt != null && txt.length() > 0){
						return "********";
					}
				}
				return "";
			}
		});

		// --- container for table/row related editing / actions
		Composite btComp = new Composite(root, SWT.NONE);
		l = GridLayoutFactory.swtDefaults().create();
		l.numColumns = 1;
		l.marginHeight = 0;
		btComp.setLayout(l);

		gd = GridDataFactory.swtDefaults().create();
		gd.verticalAlignment = SWT.TOP;
		gd.horizontalAlignment = SWT.LEFT;
		btComp.setLayoutData(gd);

		// --- add new server button
		Button btAdd = new Button(btComp, SWT.PUSH);
		btAdd.setText("Add...");
		btAdd.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e){
				GitBlitServer entry = new GitBlitServer();
				if(processEntry(entry) == Window.OK){
					prefModel.addRepository(entry);
					viewer.refresh();
					TableItem[] items = viewer.getTable().getItems();
					for(TableItem item: items){
						if(entry.equals(item.getData())){
							item.setChecked(true);
							break;
						}
					}
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e){}
		});

		// --- edit server button
		final Button btEdit = new Button(btComp, SWT.PUSH);
		btEdit.setText("Edit...");
		btEdit.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e){
				Object sel = viewer.getSelection();
				if(sel instanceof StructuredSelection){
					StructuredSelection ssel = (StructuredSelection)sel;
					sel = ssel.getFirstElement();
					if(sel instanceof GitBlitServer){
						processEntry((GitBlitServer)sel);
					}
				}
				viewer.refresh();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e){}
		});

		// --- remove server button
		final Button btRemove = new Button(btComp, SWT.PUSH);
		btRemove.setText("Remove");
		btRemove.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e){
				Object sel = viewer.getSelection();
				if(sel instanceof StructuredSelection){
					StructuredSelection ssel = (StructuredSelection)sel;
					sel = ssel.getFirstElement();
					if(sel instanceof GitBlitServer){
						prefModel.removeRepository((GitBlitServer)sel);
						viewer.remove((GitBlitServer)sel);
						viewer.refresh();
					}
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e){}
		});
		// --- container for double click behaviour radio buttons
		l = GridLayoutFactory.swtDefaults().create();
		l.numColumns = 1;

		gd = GridDataFactory.swtDefaults().create();
		gd.horizontalAlignment = SWT.FILL;
		gd.verticalAlignment = SWT.TOP;
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = false;

		Group g = new Group(root, SWT.SHADOW_IN);
		g.setLayout(l);
		g.setLayoutData(gd);
		g.setText("Double click on repository entry");

		btEGitPaste = createRadioButton(new CloneAction(null), g);
		btOpenGitBlit = createRadioButton(new BrowseAction(null), g);
		btCopyUrl = createRadioButton(new CopyAction(null), g);

		// --- Open edit dialog when a given row is double clicked
		viewer.setContentProvider(new PreferenceModelProvider());
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event){
				Object sel = event.getSelection();
				if(sel instanceof StructuredSelection){
					StructuredSelection ssel = (StructuredSelection)sel;
					sel = ssel.getFirstElement();
					if(sel instanceof GitBlitServer){
						processEntry((GitBlitServer)sel);
					}
				}
				viewer.refresh();
			}
		});

		// --- init buttons by default
		btEdit.setEnabled(false);
		btRemove.setEnabled(false);

		// --- Sync button state corresponding to table selection
		this.viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event){
				boolean sel = !event.getSelection().isEmpty();
				btEdit.setEnabled(sel);
				btRemove.setEnabled(sel);
			}
		});

		new Label(root, SWT.NONE);
		g = new Group(root, SWT.SHADOW_IN);
		g.setText("Action Settings");
		g.setLayout(l);
		gd = GridDataFactory.swtDefaults().create();
		gd.horizontalAlignment = SWT.FILL;
		gd.verticalAlignment = SWT.TOP;
		gd.grabExcessHorizontalSpace = false;
		gd.grabExcessVerticalSpace = false;
		g.setLayoutData(gd);

		this.btWSGroupName = createCheckButton(new CloneOneClickAction(null), "Add group name to working set name", g);
		this.btWSGroupName.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e){
				prefModel.setWSGroupNameEnabled(btWSGroupName.getSelection());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e){}
		});

		
		new Label(root, SWT.NONE);
		g = new Group(root, SWT.SHADOW_IN);
		g.setText("Appearance");
		g.setLayout(l);
		g.setLayoutData(gd);

		btColViewer = new Button(g, SWT.CHECK);
		btColViewer.setText("Decorate repository table");
		btColViewer.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e){
				prefModel.setDecorateView(btColViewer.getSelection());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e){}
		});

		new Label(root, SWT.NONE); // filler
		Link link = new Link(root, SWT.NONE);
		link.setText("<a>Report issue / Feeedback</a>");
		link.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event){
				try{
					PlatformUI.getWorkbench().getBrowserSupport().createBrowser("eGitBlit issue page").openURL(new URL(URL_GITHUB_ISSUE));
				}
				catch(Exception e){
					Activator.showAndLogError("Can't open eGitBlit issue page", e);
				}
			}
		});
		initData();
		return root;
	}

	private Button createRadioButton(Action action, Group g){
		Button ret = new Button(g, SWT.RADIO);
		ret.setText(action.getText());
		ret.setImage(action.getImageDescriptor().createImage());
		return ret;
	}

  private Button createCheckButton(Action action, String actionText, Group g){
    Button ret = new Button(g, SWT.CHECK);
    ret.setText(action.getText() + ": " + actionText);
    ret.setImage(action.getImageDescriptor().createImage());
    return ret;
  }

  /**
	 * Apply all field settings of preferences to preference model.
	 */
	protected void applyFieldValues(){
		synchDoubleClick(false);
		syncActive(false);
	}

	protected void initData(){
		if(this.viewer != null){
			this.prefModel = readPreferenceSettings();
			viewer.setInput(this.prefModel);
			synchDoubleClick(true);
			syncActive(true);
			this.btColViewer.setSelection(prefModel.isDecorateView());
			this.btWSGroupName.setSelection(prefModel.isWSGroupNameEnabled());
		}
	}

	/**
	 * @param doRead
	 *           true = read from model to gui, false = save gui to model
	 */
	protected void syncActive(boolean doRead){
		if(viewer == null || this.prefModel == null){
			return;
		}
		GitBlitServer server;
		TableItem[] items = viewer.getTable().getItems();
		for(TableItem item: items){
			if(item.getData() instanceof GitBlitServer){
				server = (GitBlitServer)item.getData();
				if(doRead){
					item.setChecked(server.active);
				}
				else{
					server.active = item.getChecked();
				}
			}
		}
	}

	/**
	 * @param doRead
	 *           true = read from model to gui, false = save gui to model
	 */
	protected void synchDoubleClick(boolean doRead){
		if(this.btCopyUrl == null || this.btOpenGitBlit == null || this.prefModel == null){
			return;
		}
		if(doRead){
			DoubleClickBehaviour dbcl = this.prefModel.getDoubleClick();
			switch(dbcl){
				case PasteEGit:
					this.btEGitPaste.setSelection(true);
					this.btCopyUrl.setSelection(false);
					this.btOpenGitBlit.setSelection(false);
					break;
				case CopyUrl:
					this.btEGitPaste.setSelection(false);
					this.btCopyUrl.setSelection(true);
					this.btOpenGitBlit.setSelection(false);
					break;
				case OpenGitBlit:
					this.btEGitPaste.setSelection(false);
					this.btCopyUrl.setSelection(false);
					this.btOpenGitBlit.setSelection(true);
					break;
				default:
					this.btEGitPaste.setSelection(true);
					this.btCopyUrl.setSelection(false);
					this.btOpenGitBlit.setSelection(false);
			}
		}
		else{
			if(btEGitPaste.getSelection() == true){
				this.prefModel.setDoubleClick(DoubleClickBehaviour.PasteEGit);
			}
			else if(btCopyUrl.getSelection() == true){
				this.prefModel.setDoubleClick(DoubleClickBehaviour.CopyUrl);
			}
			else if(btOpenGitBlit.getSelection() == true){
				this.prefModel.setDoubleClick(DoubleClickBehaviour.OpenGitBlit);
			}
		}
	}

	@Override
	protected void performDefaults(){
		initData();
		super.performDefaults();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#performCancel()
	 */
	@Override
	public boolean performCancel(){
		// initData();
		return super.performCancel();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 */
	@Override
	public boolean performOk(){
		try{
			applyFieldValues();
			PreferenceMgr.saveConfig(this.prefModel);
			firePreferenceChange();
		}
		catch(GitBlitExplorerException e){
			Activator.logError("Error saving preferences.", e);
			return false;
		}
		return super.performOk();
	}

	/**
	 * Because this dialog uses regular swt/jface controls, preference changes are not published
	 * This method publishes a change event by using the root key of gitblit preferences PreferenceMgr.KEY_GITBLIT_ROOT
	 */
	private void firePreferenceChange(){
		Activator.getDefault().getPreferenceStore().firePropertyChangeEvent(PreferenceMgr.KEY_GITBLIT_ROOT, null, null);
	}

	/**
	 * Creates a table column
	 * 
	 * @param viewer
	 * @param title
	 * @param width
	 * @return
	 */
	private TableViewerColumn createColumn(TableViewer viewer, String title, int width){
		TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
		TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(width);
		column.setResizable(true);
		column.setMoveable(true);
		return viewerColumn;
	}

	/**
	 * Wrapper method reading stored config
	 * 
	 * @return
	 */
	private PreferenceModel readPreferenceSettings(){
		try{
			return PreferenceMgr.readConfig();
		}
		catch(GitBlitExplorerException e){
			Activator.logError("Error reading preferences.", e);
		}
		return null;
	}

	/**
	 * initializes the {@link RepositoryDialog} and opens the dialog
	 * 
	 * @param entry
	 *           entry to be displayed and updated
	 * @return Eclipse {@link Window} code OK or CAMCEL
	 */
	private int processEntry(GitBlitServer entry){
		RepositoryDialog dlg = new RepositoryDialog(getShell());
		dlg.setBlockOnOpen(true);
		dlg.url = entry.url;
		dlg.user = entry.user;
		dlg.pwd = entry.password;
		// dlg.urlSep = entry.urlSeparator;

		int rc = dlg.open();
		if(rc == Window.OK){
			entry.url = dlg.url;
			entry.user = dlg.user;
			entry.password = dlg.pwd;
			entry.active = true;
		}
		return rc;
	}

}