package com.baloise.egitblit.view;

import java.util.Date;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;

import com.baloise.egitblit.gitblit.GitBlitRepository;
import com.baloise.egitblit.view.model.ErrorViewModel;
import com.baloise.egitblit.view.model.GroupViewModel;
import com.baloise.egitblit.view.model.ProjectViewModel;
import com.gitblit.utils.ArrayUtils;

/**
 * @see CellLabelProvider
 * @author MicBag extends CellLabelProvider
 **/

public class RepoLabelProvider implements ITableLabelProvider, ITableColorProvider, ITableFontProvider{

	private TreeViewer viewer;
	private Font fontBold;
	private boolean decorateLabels = false;

	public RepoLabelProvider(TreeViewer viewer){
		this.viewer = viewer;
		Font font = this.viewer.getTree().getFont();
		FontData[] fontData = font.getFontData();
		for(FontData item : fontData){
			item.setStyle(SWT.BOLD);
		}
		
		fontBold = new Font(viewer.getTree().getDisplay(), fontData);
		viewer.getTree().addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e){
				disposeFont(fontBold);
			}
		});
	}

	private void disposeFont(Font font){
		if(font == null || font.isDisposed() == true){
			return;
		}
		font.dispose();
	}
	
	@Override
	public Image getColumnImage(Object element, int columnIndex){
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex){
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
					return makeDateAgo(((ProjectViewModel) element).getLastChange());
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
	private String makeDateAgo(Date date){
		if(date == null){
			return "";
		}
		long diff = System.currentTimeMillis() - date.getTime();

		long minAgo = diff / 60000;
		long hourAgo = minAgo / 60;
		long daysAgo = hourAgo / 24;
		long monthAgo = daysAgo / 30;

		StringBuilder buff = new StringBuilder();
		if(diff < 0){
			buff.append("In ");
			if(monthAgo > 1){
				buff.append(monthAgo);
				buff.append(" months");
			}
			if(daysAgo > 1){
				buff.append(daysAgo);
				buff.append(" days");
			}else if(hourAgo > 1){
				buff.append(hourAgo);
				buff.append(" hours");
			}else if(minAgo > 1){
				buff.append(minAgo);
				buff.append(" mins");
			}
			return buff.toString();
		}
		if(monthAgo > 1){
			buff.append(monthAgo);
			buff.append(" months");
		}
		if(daysAgo > 1){
			buff.append(daysAgo);
			buff.append(" days ago");
		}else if(hourAgo > 1){
			buff.append(hourAgo);
			buff.append(" hours ago");
		}else if(minAgo > 1){
			buff.append(minAgo);
			buff.append(" mins ago");
		}else{
			buff.append("just now");
		}
		return buff.toString();
	}

	private boolean isRecentlyUpdated(ProjectViewModel model){
		String str = makeDateAgo(model.getLastChange());
		return str.contains("mins") || str.contains("now");
	}
	
	public void setDecorateLabels(boolean yesNo){
		this.decorateLabels = yesNo;
	}
	
	@Override
	public Color getForeground(Object element, int columnIndex){
		if(this.decorateLabels == false){
			return null;
		}

		if(element != null && (element instanceof ErrorViewModel)){
			return viewer.getTree().getDisplay().getSystemColor(SWT.COLOR_RED);
		}
//		if(element != null && (element instanceof GroupViewModel)){
//			return viewer.getTree().getDisplay().getSystemColor(SWT.COLOR_DARK_BLUE);
//		}
		
		if(element != null && (element instanceof ProjectViewModel) && columnIndex == 4){
			if(((ProjectViewModel)element).hasCommits() == false){
				return viewer.getTree().getDisplay().getSystemColor(SWT.COLOR_DARK_GREEN);
			}
			else{
				return viewer.getTree().getDisplay().getSystemColor(SWT.COLOR_BLUE);
			}
		}
		
		if(element != null && (element instanceof ProjectViewModel) && columnIndex == 3){
			if(isRecentlyUpdated((ProjectViewModel)element) == true){
				return viewer.getTree().getDisplay().getSystemColor(SWT.COLOR_DARK_GREEN);
			}
			else{
				return viewer.getTree().getDisplay().getSystemColor(SWT.COLOR_BLUE);
			}
		}
		return null;
	}

	
	@Override
	public void addListener(ILabelProviderListener listener){
	}

	@Override
	public void dispose(){
	}

	@Override
	public boolean isLabelProperty(Object element, String property){
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener){
	}

	@Override
	public Font getFont(Object element, int columnIndex){

		if(this.decorateLabels == true){
			if(element != null && (element instanceof ErrorViewModel) == true){
				this.viewer.getTree().getFont();
			}
//			if(element != null && (element instanceof GroupViewModel) && columnIndex == 0){
//				return fontBold;
//			}
		}
		return this.viewer.getTree().getFont();
	}

	@Override
	public Color getBackground(Object element, int columnIndex){
		return null;
	}


}
