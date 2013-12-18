package com.baloise.egitblit.pref;

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
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.baloise.egitblit.common.GitBlitServer;
import com.baloise.egitblit.main.Activator;
import com.baloise.egitblit.main.EclipseLog;
import com.baloise.egitblit.main.GitBlitExplorerException;
import com.baloise.egitblit.pref.PreferenceModel.DoubleClickBehaviour;

/**
 * Preference Page Page showing the configuration settings of git blit explorer
 * 
 * @author MicBag
 * 
 */
public class GitBlitExplorerPrefPage extends PreferencePage implements IWorkbenchPreferencePage{

	public final static String ID = "com.baloise.egitblit.pref";

	private PreferenceModel prefModel;
	private TableViewer viewer;
	private Button btOpenGitBlit;
	private Button btCopyUrl;

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

		Composite root = new Composite(parent, SWT.NONE);
		GridLayout l = GridLayoutFactory.swtDefaults().create();
		l.numColumns = 2;
		l.marginWidth = 0;
		root.setLayout(l);

		GridData gd = GridDataFactory.swtDefaults().create();
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		gd.horizontalAlignment = SWT.FILL;
		gd.verticalAlignment = SWT.FILL;

		viewer = new TableViewer(root, SWT.CHECK | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setLayout(l);
		table.setLayoutData(gd);

		TableViewerColumn colActive = createColumn(viewer, "Active", 50);
		colActive.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element){
				return "";
			}
		});

		TableViewerColumn colURL = createColumn(viewer, "URL", 150);
		colURL.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element){
				if(element != null && element instanceof GitBlitServer){
					return ((GitBlitServer) element).url;
				}
				return "";
			}
		});

		TableViewerColumn colUser = createColumn(viewer, "User", 100);
		colUser.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element){
				if(element != null && element instanceof GitBlitServer){
					return ((GitBlitServer) element).user;
				}
				return "";
			}
		});

		TableViewerColumn colPwd = createColumn(viewer, "Password", 100);
		colPwd.getColumn().setText("Password");
		colPwd.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element){
				if(element != null && element instanceof GitBlitServer){
					String txt = ((GitBlitServer) element).password;
					if(txt != null && txt.length() > 0){
						return "********";
					}
				}
				return "";
			}
		});

		Composite btComp = new Composite(root, SWT.NONE);
		l = GridLayoutFactory.swtDefaults().create();
		l.numColumns = 1;
		l.marginHeight = 0;
		btComp.setLayout(l);

		gd = GridDataFactory.swtDefaults().create();
		gd.verticalAlignment = SWT.TOP;
		gd.horizontalAlignment = SWT.LEFT;
		btComp.setLayoutData(gd);

		Button btAdd = new Button(btComp, SWT.PUSH);
		btAdd.setText("Add...");
		btAdd.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e){
				GitBlitServer entry = new GitBlitServer();
				if(processEntry(entry) == Window.OK){
					prefModel.addRepository(entry);
				}
				viewer.refresh();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e){
			}
		});

		final Button btEdit = new Button(btComp, SWT.PUSH);
		btEdit.setText("Edit...");
		btEdit.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e){
				Object sel = viewer.getSelection();
				if(sel instanceof StructuredSelection){
					StructuredSelection ssel = (StructuredSelection) sel;
					sel = ssel.getFirstElement();
					if(sel instanceof GitBlitServer){
						processEntry((GitBlitServer) sel);
					}
				}
				viewer.refresh();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e){
			}
		});

		final Button btRemove = new Button(btComp, SWT.PUSH);
		btRemove.setText("Remove");
		btRemove.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e){
				Object sel = viewer.getSelection();
				if(sel instanceof StructuredSelection){
					StructuredSelection ssel = (StructuredSelection) sel;
					sel = ssel.getFirstElement();
					if(sel instanceof GitBlitServer){
						prefModel.removeRepository((GitBlitServer) sel);
					}
				}
				viewer.refresh();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e){
			}
		});

		Composite comp = new Composite(root, SWT.NONE);
		l = GridLayoutFactory.swtDefaults().create();
		l.numColumns = 1;
		comp.setLayout(l);

		gd = GridDataFactory.swtDefaults().create();
		gd.verticalAlignment = SWT.TOP;
		gd.grabExcessHorizontalSpace = true;
		// gd.grabExcessVerticalSpace = true;
		comp.setLayoutData(gd);

		Group g = new Group(comp, SWT.SHADOW_IN);
		g.setLayout(l);
		g.setLayoutData(gd);
		g.setText("Double click on repository");

		btOpenGitBlit = new Button(g, SWT.RADIO);
		btOpenGitBlit.setText("Open GitBlit in a Browser");

		btCopyUrl = new Button(g, SWT.RADIO);
		btCopyUrl.setText("Copy repository path to clipboard");

		viewer.setContentProvider(new PreferenceModelProvider());
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event){
				Object sel = event.getSelection();
				if(sel instanceof StructuredSelection){
					StructuredSelection ssel = (StructuredSelection) sel;
					sel = ssel.getFirstElement();
					if(sel instanceof GitBlitServer){
						processEntry((GitBlitServer) sel);
					}
				}
				viewer.refresh();
			}
		});

		btEdit.setEnabled(false);
		btRemove.setEnabled(false);
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event){
				boolean sel = !event.getSelection().isEmpty();
				btEdit.setEnabled(sel);
				btRemove.setEnabled(sel);
			}
		});
		
		// --- init data
		this.prefModel = getRepositoryList();
		viewer.setInput(this.prefModel);
		synchDoubleClick(true);
		syncActive(true);
		return root;
	}
	

	protected void applyFieldValues(){
		synchDoubleClick(false);
		syncActive(false);
	}
	
	
	protected void syncActive(boolean doRead){
		if(viewer == null || this.prefModel == null){
			return;
		}
		GitBlitServer server;
		TableItem[] items = viewer.getTable().getItems();
		for(TableItem item : items){
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
	
	protected void synchDoubleClick(boolean doRead){
		if(this.btCopyUrl == null || this.btOpenGitBlit == null || this.prefModel == null){
			return;
		}
		if(doRead){
			DoubleClickBehaviour dbcl = this.prefModel.getDoubleClick();
			switch(dbcl){
				case CopyUrl:
					btCopyUrl.setSelection(true);
					break;
				case OpenGitBlit:
					btOpenGitBlit.setSelection(true);
					break;
				default:
			}
		}
		else{
			if(btCopyUrl.getSelection() == true){
				this.prefModel.setDoubleClick(DoubleClickBehaviour.CopyUrl);
			}
			else
			{
				this.prefModel.setDoubleClick(DoubleClickBehaviour.OpenGitBlit);
			}
		}
	}

	@Override
	protected void performApply(){
		try{
			applyFieldValues();
			PreferenceMgr.saveConfig(this.prefModel);
			firePreferenceChange();
		}catch(GitBlitExplorerException e){
			EclipseLog.error("Error saving preferences.", e);
		}
		super.performApply();
	}

	@Override
	public boolean performCancel(){
		return super.performCancel();
	}

	@Override
	public boolean performOk(){
		try{
			applyFieldValues();
			PreferenceMgr.saveConfig(this.prefModel);
			firePreferenceChange();
		}catch(GitBlitExplorerException e){
			EclipseLog.error("Error saving preferences.", e);
			return false;
		}
		return super.performOk();
	}
	
	private void firePreferenceChange(){
		Activator.getDefault().getPreferenceStore().firePropertyChangeEvent(PreferenceMgr.KEY_GITBLIT_ROOT, null,null);
	}

	private TableViewerColumn createColumn(TableViewer viewer, String title, int width){
		TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
		TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(width);
		column.setResizable(true);
		column.setMoveable(true);
		return viewerColumn;
	}

	private PreferenceModel getRepositoryList(){
		try{
			return PreferenceMgr.readConfig();
		}catch(GitBlitExplorerException e){
			EclipseLog.error("Error reading preferences.", e);
		}
		return null;
	}

	private int processEntry(GitBlitServer entry){
		RepositoryDialog dlg = new RepositoryDialog(getShell());
		dlg.setBlockOnOpen(true);
		dlg.url = entry.url;
		dlg.user = entry.user;
		dlg.pwd = entry.password;
		//dlg.urlSep = entry.urlSeparator;

		int rc = dlg.open();
		if(rc == Window.OK){
			entry.url = dlg.url;
			entry.user = dlg.user;
			entry.password = dlg.pwd;
			//entry.urlSeparator = dlg.urlSep;
		}
		return rc;
	}

	
}