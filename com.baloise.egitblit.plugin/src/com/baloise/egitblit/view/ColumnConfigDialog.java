package com.baloise.egitblit.view;

import java.util.Collections;
import java.util.List;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.baloise.egitblit.main.Activator;
import com.baloise.egitblit.main.SharedImages;
import com.baloise.egitblit.pref.PreferenceModel;
import com.baloise.egitblit.pref.PreferenceModel.ColumnData;

/**
 * Dialog to configure the group / repo view (display columns, order of columns)
 * 
 * @author MicBag
 * 
 */
public class ColumnConfigDialog extends TitleAreaDialog{

	private TableViewer			viewer;
	private List<ColumnData>	colData;

	public ColumnConfigDialog(Shell parentShell, PreferenceModel prefModel){
		super(parentShell);
		this.colData = prefModel.getColumnData();
	}

	@Override
	public void create(){
		super.create();
		setTitle("Configure group/repository table columns");
		setMessage("Select and order the repository properties you want to see on the \ngroup / repository table", IMessageProvider.NONE);
		setHelpAvailable(false);
	}
	
	@Override
	protected Point getInitialSize(){
		Point p = getShell().computeSize(SWT.DEFAULT, 480, true);
		p.x *= 1.5;
		return p;
	}

	@Override
	protected Control createDialogArea(Composite parent){
		Composite area = (Composite) super.createDialogArea(parent);

		final FormToolkit ftk = new FormToolkit(parent.getDisplay());
		parent.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e){
				ftk.dispose();
			}
		});

		Composite comp = ftk.createComposite(area, SWT.NONE);
		GridLayout l = GridLayoutFactory.swtDefaults().create();
		GridData gd = GridDataFactory.swtDefaults().create();
		l.numColumns = 2;
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		gd.horizontalAlignment = SWT.FILL;
		gd.verticalAlignment = SWT.FILL;
		gd.heightHint = 640;
		comp.setLayout(l);
		comp.setLayoutData(gd);
		gd.heightHint = -1;

		viewer = new TableViewer(comp, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER | SWT.CHECK);
		viewer.getTable().setLayout(l);
		viewer.getTable().setLayoutData(gd);
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.setSorter(new ViewerSorter() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2){
				if(e1 instanceof ColumnData && e2 instanceof ColumnData){
					ColumnData c1 = ((ColumnData) e1);
					ColumnData c2 = ((ColumnData) e2);
					return new Integer(c1.pos).compareTo(c2.pos);
				}
				return super.compare(viewer, e1, e2);
			}
		});

		viewer.getControl().addKeyListener(new KeyListener() {

			@Override
			public void keyReleased(KeyEvent e){
			}

			@Override
			public void keyPressed(KeyEvent e){
				if(e.keyCode == SWT.KEYPAD_ADD || e.keyCode == SWT.KEYPAD_SUBTRACT){
					IStructuredSelection sel = (IStructuredSelection) viewer.getSelection();
					if(sel.isEmpty()){
						return;
					}
					List<Object> list = sel.toList();
					for(Object item : list){
						// ...needed as method here in in SWT.CHECK, because
						// SWT.CHECK will not be fired when manually calling
						// item.setChecked() :-(
						setChecked((ColumnData) item, e.keyCode == SWT.KEYPAD_ADD);
					}
					viewer.setSelection(new StructuredSelection(list));
					viewer.refresh();
					syncCheck();
				}
			}
		});

		final Table table = viewer.getTable();

		final TableViewerColumn vcol = new TableViewerColumn(viewer, SWT.LEFT);
		vcol.getColumn().setText("Column Name & Order");

		vcol.getColumn().pack();
		vcol.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element){
				if(element instanceof ColumnData){
					ColumnDesc desc = ColumnDesc.valueOf(((ColumnData) element).id);
					if(desc != null){
						return desc.label;
					}
				}
				return super.getText(element);
			}
		});

		table.addControlListener(new ControlListener() {
			@Override
			public void controlResized(ControlEvent e){
				vcol.getColumn().setWidth(table.getClientArea().width);
			}

			@Override
			public void controlMoved(ControlEvent e){
			}
		});

		table.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e){
				if((e.detail == SWT.CHECK)){
					TableItem item = (TableItem) e.item;
					if(item != null){
						setChecked((ColumnData) item.getData(), item.getChecked());
					}
					viewer.refresh();
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e){
			}
		});

		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		// --- container for table/row related editing / actions
		Composite btComp = ftk.createComposite(comp, SWT.NONE);
		l = GridLayoutFactory.swtDefaults().create();
		l.numColumns = 1;
		l.marginHeight = 0;
		btComp.setLayout(l);

		gd = GridDataFactory.swtDefaults().create();
		gd.verticalAlignment = SWT.TOP;
		gd.horizontalAlignment = SWT.LEFT;
		btComp.setLayoutData(gd);

		Button btUp = new Button(btComp, SWT.PUSH);
		btUp.setToolTipText("Moving selected column up");

		btUp.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e){
				ISelection isel = viewer.getSelection();
				if(isel.isEmpty()){
					return;
				}
				if(isel instanceof StructuredSelection){
					StructuredSelection sel = (StructuredSelection) isel;
					List<Object> list = sel.toList();
					if(list != null && list.isEmpty() == false){
						for(Object item : list){
							ColumnData data = ((ColumnData) item);
							ColumnData prev = getColumnData(data.pos - 1);
							if(prev == null || prev.pos == 0){
								continue;
							}
							swapPos(data, prev);
						}
						viewer.refresh();
						syncCheck();
						viewer.getTable().showSelection();
					}
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e){
			}
		});
		btUp.setImage(Activator.getImage(SharedImages.Up));

		Button btDown = new Button(btComp, SWT.PUSH);
		btDown.setToolTipText("Moving selected column down");
		btDown.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e){
				ISelection isel = viewer.getSelection();
				if(isel.isEmpty()){
					return;
				}
				if(isel instanceof StructuredSelection){
					StructuredSelection sel = (StructuredSelection) isel;
					List<Object> list = sel.toList();
					if(list != null && !list.isEmpty()){
						Collections.reverse(list); // start with the last element
						for(Object item : list){
							ColumnData data = ((ColumnData) item);
							ColumnData next = getColumnData(data.pos+1);
							if(data.pos == 0){
								continue;
							}
							if(next == null){
								continue;
							}
							swapPos(next, data);
						}
						viewer.refresh();
						syncCheck();
						viewer.getTable().showSelection();
					}
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e){
			}
		});
		
		btDown.setImage(Activator.getImage(SharedImages.Down));

// Does not work correctly so far: Sorting is not correct (grrr)		
//		final ImageDescriptor refreskImgDesc = Activator.getImageDescriptor(SharedImages.Refresh);
//		Button btReset = new Button(btComp, SWT.PUSH);
//		btReset.setToolTipText("Reset displayed columns and their order");
//		btReset.setImage(refreskImgDesc.createImage());
//		btReset.addSelectionListener(new SelectionListener() {
//			@Override
//			public void widgetSelected(SelectionEvent e){
//				colData.clear();
//				colData.addAll(PreferenceMgr.initColumnData());
//				sortColData();
//
//				viewer.setInput(colData.toArray(new ColumnData[colData.size()]));
//				viewer.refresh();
//				syncCheck();
//			}
//
//			@Override
//			public void widgetDefaultSelected(SelectionEvent e){
//			}
//		});
		
		sortColData();
		this.viewer.setInput(this.colData.toArray(new ColumnData[this.colData.size()]));
		this.viewer.refresh();
		syncCheck();
		return area;
	}
	
	private void sortColData(){
		ColumnData act,next;
		
		int size = this.colData.size()-1;
		for(int i=0;i<size;i++){
			act = this.colData.get(i);
			next = this.colData.get(i+1);
			if(act.visible == false && next.visible == true && act.pos < next.pos){
				swapPos(act,next);
				i=0;
			}
		}
	}

	/**
	 * Selects a column and sets the column width. Needed, because a manually
	 * setChecked does not fire a SWT.CHECK event (why?.... I'm confused) So,
	 * logic has to be placed in a separad method, because this routine is
	 * needed for SPACE and +/- for setting the checkbox
	 * 
	 * @param data
	 *            ColumnData to ajust
	 * @param checked
	 *            true = checked, false = deselect
	 */
	private void setChecked(ColumnData data, boolean checked){
		if(data == null){
			return;
		}
		TableItem item = getTableItem(data);
		if(item == null){
			return;
		}
		if(data.pos == 0){
			// First column can't be removed (group / repo column)
			data.visible = true;
			item.setChecked(true);
			return;
		}
		data.visible = checked;
		item.setChecked(checked);
	}

	/**
	 * Checkboxes in tables behave are a little bit strange, because the check
	 * mark can get lost (somehow....RFM :-)) However, he have to take care
	 * about the item state by ourself.
	 */
	private void syncCheck(){
		for(ColumnData item : this.colData){
			TableItem titem = getTableItem(item);
			if(titem != null){
				titem.setChecked(item.visible);
			}
		}
	}

	/**
	 * Swaps the position values of two ColumnData entries
	 * 
	 * @param cd1
	 *            First ColumnData
	 * @param cd2
	 *            Second ColumnData
	 */
	private void swapPos(ColumnData cd1, ColumnData cd2){
		if(cd1 == null || cd2 == null){
			return;
		}
		int pos = cd1.pos;
		cd1.pos = cd2.pos;
		cd2.pos = pos;
	}

	/**
	 * Return the TableItem which belongs to the passed ColumnData
	 * 
	 * @param data
	 * @return
	 */
	private TableItem getTableItem(ColumnData data){
		if(data == null){
			return null;
		}
		TableItem[] items = viewer.getTable().getItems();
		for(TableItem item : items){
			if(data.equals(item.getData())){
				return item;
			}
		}
		return null;
	}

	/**
	 * Get the columndata by its position
	 * 
	 * @param pos
	 * @return
	 */
	private ColumnData getColumnData(int pos){
		for(ColumnData item : this.colData){
			if(item.pos == pos){
				return item;
			}
		}
		return null;
	}

	/**
	 * For the caller: Deliver the result of this dialog
	 * 
	 * @return list of ColumnData
	 */
	public List<ColumnData> getColumns(){
		return this.colData;
	}
	
	@Override
	protected boolean isResizable() {
	    return true;
	}

}
