package ch.basler.plugin.gitblitexplorer.view;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;


/**
 * @author MicBag
 *
 */
public class RepoLabelProvider extends LabelProvider {

	@Override
	  public String getText(Object element) {
		if(element instanceof RepoViewModel){
			RepoViewModel model = (RepoViewModel)element; 
			if(model.getChilds().size() != 0){
				return model.repo;
			}
			return model.project;
		}
		return "(Error)";
	  }

	  @Override
	  public Image getImage(Object element) {
		if(element instanceof RepoViewModel){
			RepoViewModel model = (RepoViewModel)element; 
			if(model.getChilds().size() != 0){
				return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
			}
			//return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_PROJECT);
		}
		return super.getImage(element);
	  }

}
