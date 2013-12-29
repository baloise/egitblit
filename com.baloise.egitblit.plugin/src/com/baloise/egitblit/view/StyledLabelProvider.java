package com.baloise.egitblit.view;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import com.baloise.egitblit.gitblit.GitBlitRepository;
import com.baloise.egitblit.main.Activator;
import com.baloise.egitblit.view.model.ErrorViewModel;
import com.baloise.egitblit.view.model.GitBlitViewModel;
import com.baloise.egitblit.view.model.GroupViewModel;
import com.baloise.egitblit.view.model.ProjectViewModel;
import com.gitblit.utils.ArrayUtils;

/**
 * Preparing ofe repo true/table labels and its decorations
 * 
 * @author MicBag
 * 
 */
public class StyledLabelProvider extends StyledCellLabelProvider{

	private TreeViewer viewer;
	private boolean decorateLabels = false;
	private Map<String, Color> colMap = new HashMap<String, Color>();
	private Map<String, Image> imgMap = new HashMap<String, Image>();
	private Font italicFont;
	private Font boldFont;
	private Font boldItalicFont;

	private int cellHeight;
	
	public StyledLabelProvider(TreeViewer viewer){
		this.viewer = viewer;

		initFonts();

		viewer.getTree().addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e){
				disposeColors();
				disposeFonts();
				disposeImages();
			}
		});
	}

	@Override
	public void dispose(){
	}

	public void setDecorateLabels(boolean yesNo){
		this.decorateLabels = yesNo;
	}

	// ------------------------------------------------------------------------
	// --- Styler
	// ------------------------------------------------------------------------

	private class BaseStyler extends Styler{

		private Color fgCol;
		private Color bgCol;
		private Font font;

		public BaseStyler(Font font, Color fgCol, Color bgCol){
			this.fgCol = fgCol;
			this.bgCol = bgCol;
			this.font = font;
		}

		@Override
		public void applyStyles(TextStyle textStyle){
			textStyle.foreground = this.fgCol;
			textStyle.background = this.bgCol;
			textStyle.font = this.font;
		}
	};

	@Override
	public void update(ViewerCell cell){
		// --- Get the current column and element
		int columnIndex = cell.getColumnIndex();
		Object element = cell.getElement();

		if(element instanceof GitBlitViewModel == true){
			GitBlitViewModel model = (GitBlitViewModel) element;
			String label = getColumnText(model, columnIndex);
			Color fgCol = null;
			Color bgCol = null;
			Font font = null;
			Image image = null;

			if(label != null){
				Font tfont = cell.getFont();
				tfont.getFontData();
				int lstart = 0;
				int lend = label.length();
				
				StyledString text = new StyledString();
				if(this.decorateLabels){
					fgCol = getForgroundColor(model, columnIndex);
					bgCol = getBackgroundColor(model, columnIndex);
					if(model instanceof GroupViewModel){
						switch(columnIndex){
							case 0:
								image = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
								break;
							default:
								fgCol = this.viewer.getTree().getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY);
						}
//						TreeItem item = (TreeItem) cell.getItem();
//						item.setImage(getImage(cell.getControl().getDisplay(),"#FFFFFF"));
						//cell.setBackground(this.viewer.getTree().getDisplay().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
					}
					// Assign font and other decorations
					if(model instanceof ProjectViewModel){
						switch(columnIndex){
							case 0:
								image = getImage(cell.getControl().getDisplay(),((ProjectViewModel) model).getColor());
								break;
							case 3:
								long diff = System.currentTimeMillis() - ((ProjectViewModel) element).getLastChange().getTime();
								TimeAgo ago = TimeAgo.getAgo(diff);
								long val = ago.getValue(diff);
								switch(ago){
									case Now:
										font = boldItalicFont;
										break;
									case Mins:
										font = boldItalicFont;
										break;
									case Hours:
										font = boldItalicFont;
										break;
									case Days:
										if(val < 2){
											font = boldItalicFont;
										}
										if(val < 7){
											font = italicFont;
										}
										break;
									case Months:
										break;
									case Years:
										break;
								}
								break;
							case 2:
								font = italicFont;
								break;
							case 4:
								if(((ProjectViewModel)model).hasCommits() == false){
									font = italicFont;
								}
								break;
						}
					}
				}
				text.append(label);
				text.setStyle(lstart, lend, new BaseStyler(font, fgCol, bgCol));
				if(model instanceof GroupViewModel && columnIndex == 0 && decorateLabels == true){
					int size = ((GroupViewModel)model).getChilds().size();
					if(size > 0){
						text.append(" (" + size + ")",StyledString.COUNTER_STYLER);
					}
				}
				cell.setImage(image);
				cell.setText(text.toString());
				cell.setStyleRanges(text.getStyleRanges());
				
			}
		}
		super.update(cell);
	}
	
	private Image getImage(Display disp, String scol) {
		if(disp == null || scol == null){
			return null;
		}
		
		int height = viewer.getTree().getItemHeight();
		height = height <= 0 ? 8 : height;

		Image image = imgMap.get(scol);
		if(image != null){
			return image;
		}
	   image = new Image(disp, height>>1, height);
	    GC gc = new GC(image);
	    Color col = getRepoColor(scol);
	    if(col == null){
	    	return null;
	    }
	    gc.setBackground(col);
	    gc.fillRectangle(0, 0, height>>1, height);
	    imgMap.put(scol, image);
	    return image;
	}
	
	
	/**
	 * Get the label of the column
	 * 
	 * @param element
	 * @param columnIndex
	 * @return
	 */
	public String getColumnText(GitBlitViewModel element, int columnIndex){
		switch(columnIndex){
			case 0:
				if(element instanceof GroupViewModel){
					return ((GroupViewModel) element).getName();
				}else if(element instanceof ProjectViewModel){
					return ((ProjectViewModel) element).getName();
				}
				break;
			case 1:
				if(element instanceof GroupViewModel){
					if(GitBlitRepository.GROUP_MAIN.equals(((GroupViewModel) element).getName())){
						return "main group of repositories";
					}
				}
				if(element instanceof ProjectViewModel){
					return ((ProjectViewModel) element).getDescription();
				}
				break;
			case 2:
				if(element instanceof ProjectViewModel){
					return ArrayUtils.toString(((ProjectViewModel) element).getOwners());
				}
				break;
			case 3:
				if(element instanceof ProjectViewModel){
					return makeDateAgo((ProjectViewModel) element);
				}
				break;
			case 4:
				if(element instanceof ProjectViewModel){
					if(((ProjectViewModel) element).hasCommits() == true){
						return ((ProjectViewModel) element).getSize();
					}
					return "(empty)";
				}
				break;
			case 5:
				if(element instanceof ProjectViewModel){
					return ((ProjectViewModel) element).getServerURL();
				}
				break;

		}
		return null;
	}

	/**
	 * Calculate time ago as string
	 * 
	 * @param date
	 * @return Relative time string
	 */
	private String makeDateAgo(ProjectViewModel element){
		if(element == null || ((ProjectViewModel) element).getLastChange() == null){
			return null;
		}
		long diff = System.currentTimeMillis() - ((ProjectViewModel) element).getLastChange().getTime();
		TimeAgo ago = TimeAgo.getAgo(diff);

		StringBuilder buff = new StringBuilder();
		diff = ago.getValue(diff);
		switch(ago){
			case Now:
				buff.append("just now");
				break;
			case Mins:
				buff.append(diff);
				buff.append(" mins ago");
				break;
			case Hours:
				buff.append(diff);
				buff.append(" hours ago");
				break;
			case Days:
				buff.append(diff);
				buff.append(" days ago");
				break;
			case Months:
				buff.append(diff);
				buff.append(" month ago");
				break;
			case Years:
				buff.append(diff);
				buff.append(" years ago");
				break;
		}
		return buff.toString();
	}

	/**
	 * Get the forground color of the column
	 * 
	 * @param element
	 * @param columnIndex
	 * @return
	 */
	public Color getForgroundColor(GitBlitViewModel element, int columnIndex){
		if(element == null){
			return null;
		}
		Display disp = viewer.getTree().getDisplay();
		if(element instanceof ErrorViewModel){
			return disp.getSystemColor(SWT.COLOR_RED);
		}
		if(element instanceof ProjectViewModel){
			switch(columnIndex){
				case 3:
					return getTimeColor((ProjectViewModel) element);
				case 4:
					if(((ProjectViewModel) element).hasCommits() == false){
						return disp.getSystemColor(SWT.COLOR_DARK_GREEN);
					}
					return disp.getSystemColor(SWT.COLOR_BLUE);
			}
		}
		return null;
	}
	
	private Color getBackgroundColor(GitBlitViewModel element, int columnIndex){
		return null;
	}

	/**
	 * Get the color of the last change column
	 * 
	 * @param model
	 * @return
	 */
	private Color getTimeColor(ProjectViewModel model){
		Display disp = viewer.getTree().getDisplay();
		if(model == null || model.getLastChange() == null){
			return disp.getSystemColor(SWT.COLOR_BLUE);
		}

		long time = System.currentTimeMillis() - model.getLastChange().getTime();
		TimeAgo ago = TimeAgo.getAgo(time);
		long value = ago.getValue(time);
		switch(ago){
			case Now:
			case Mins:
				return disp.getSystemColor(SWT.COLOR_DARK_GREEN);
			case Hours:
				if(value < 2){
					return disp.getSystemColor(SWT.COLOR_DARK_GREEN);
				}
				return disp.getSystemColor(SWT.COLOR_BLUE);
			case Days:
				if(ago.getValue(time) < 2){
					return disp.getSystemColor(SWT.COLOR_BLUE);
				}
				if(ago.getValue(time) < 7){
					return disp.getSystemColor(SWT.COLOR_DARK_BLUE);
				}
				if(ago.getValue(time) < 30){
					return disp.getSystemColor(SWT.COLOR_DARK_MAGENTA);
				}
			case Months:
			case Years:
		}
		return disp.getSystemColor(SWT.COLOR_BLACK);
	}

	/**
	 * Get the color of the repo, corresponding to gitblit repo view
	 * 
	 * @param model
	 * @return
	 */
	public Color getRepoColor(String scol){
		if(scol == null){
			return null;
		}
		if(scol == null || scol.length() < 7){
			return null;
		}
		Color col = this.colMap.get(scol);
		if(col != null){
			return col;
		}
		try{
			RGB rgb = new RGB(Integer.parseInt(scol.substring(1, 3), 16), Integer.parseInt(scol.substring(3, 5), 16), Integer.parseInt(scol.substring(5, 7), 16));
			col = new Color(this.viewer.getTree().getDisplay(), rgb);
			this.colMap.put(scol, col);
			return col;
		}catch(Exception e){
			Activator.logError("Error creating repository color", e);
		}
		return null;
	}
	
	private void initFonts(){
		this.italicFont 	= createFont(SWT.ITALIC);
		this.boldFont 		= createFont(SWT.BOLD);
		this.boldItalicFont	= createFont(SWT.BOLD | SWT.ITALIC);
	}
	
	private Font createFont(int style){
		FontData[] fontData = this.viewer.getTree().getFont().getFontData();
		for(int i = 0; i < fontData.length; ++i){
			fontData[i].setStyle(style);
		}
		return new Font(this.viewer.getTree().getDisplay(), fontData);
		
	}
	
	private void disposeImages(){
		for(String item : this.imgMap.keySet()){
			Image img = this.imgMap.get(item);
			if(img.isDisposed() == false){
				img.dispose();
			}
		}
		this.imgMap.clear();
	}
	private void disposeColors(){
		for(String item : this.colMap.keySet()){
			Color col = this.colMap.get(item);
			if(col.isDisposed() == false){
				col.dispose();
			}
		}
		this.colMap.clear();
	}

	private void disposeFonts(){
		if(this.italicFont != null && this.italicFont.isDisposed() == false){
			this.italicFont.dispose();
			this.italicFont = null;
		}
		if(this.boldFont != null && this.boldFont.isDisposed() == false){
			this.boldFont.dispose();
			this.boldFont = null;
		}
		if(this.boldItalicFont != null && this.boldItalicFont.isDisposed() == false){
			this.boldItalicFont.dispose();
			this.boldItalicFont = null;
		}
	}
}
