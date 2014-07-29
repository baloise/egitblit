package com.baloise.egitblit.view.action;

import static org.eclipse.ui.PlatformUI.getWorkbench;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.ISharedImages;

import com.baloise.egitblit.gitblit.GitBlitServer;
import com.baloise.egitblit.main.Activator;
import com.baloise.egitblit.pref.CloneSettings;
import com.baloise.egitblit.pref.PreferenceModel;
import com.baloise.egitblit.view.model.GitBlitViewModel;
import com.baloise.egitblit.view.model.ProjectViewModel;

/**
 * Guess what: Copy Git url to clipboard
 * 
 * @see Action
 * @author MicBag
 * 
 */
public class CopyAction extends ViewActionBase {
  public final static String ID = "com.baloise.egitblit.cmd.copy";

  public CopyAction(Viewer viewer) {
    super(ID, viewer, "Copy");
    ImageDescriptor img = getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_COPY);
    setImageDescriptor(img);
    ImageDescriptor imgDisabled = getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_COPY_DISABLED);
    setDisabledImageDescriptor(imgDisabled);
  }

  @Override
  public void doRun() {
    if(handleErrorModel() == true){
      return; // Error state: Can't perform action
    }
    GitBlitViewModel model = getSelectedModel();
    if(model != null){
      if(model instanceof ProjectViewModel){
        Clipboard clipboard = new Clipboard(getDisplay());
        clipboard.setContents(new Object[] {makeCopyUrl((ProjectViewModel)model)}, new Transfer[] {TextTransfer.getInstance()});
      }
    }
  }
  
  public String makeCopyUrl(final ProjectViewModel model) {

	String gurl = model.getGitUrl();

	// Get the host server of the model
	GitBlitServer server = null;
    PreferenceModel prefModel = getPrefModel();
    for(GitBlitServer gitBlitServer : prefModel.getServerList()){
      if(gitBlitServer.url.equalsIgnoreCase(model.getServerUrl())){
          server = gitBlitServer;
          break;
      }
    }
    
    if(server == null){
      Activator.logError("Internal error: Can't find server in config to modify clone action url. Using default git url instead.");
      return gurl;
    }
    
    CloneSettings cls = server.getCloneSettings();
    if(cls == null){
      Activator.logError("Internal error: Can't find clone settings in config to modify clone action url. Using default git url instead.");
      return gurl;
    }
    
    if(cls.isEnabled() == false){
      // Override clone settings is disabled
      return gurl;
    }
    
    try{
      String tgurl = server.getCloneURL(gurl);
      if(tgurl == null){
        Activator.logError("Internal error: Error parsing copy clone url. Using default git url instead.");
        return gurl;
      }
      return tgurl;
    }
    catch(Exception e){
      Activator.logError("Internal error: Error parsing git url. Using default git url instead.");
      return gurl;
    }
  }
}
