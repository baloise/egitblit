package com.baloise.egitblit.view.action;

import static com.baloise.egitblit.pref.PreferenceModel.CloneProtocol;
import static org.eclipse.ui.PlatformUI.getWorkbench;

import com.baloise.egitblit.gitblit.GitBlitServer;
import com.baloise.egitblit.pref.PreferenceMgr;
import org.eclipse.core.internal.resources.Project;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.ISharedImages;

import com.baloise.egitblit.pref.PreferenceModel;
import com.baloise.egitblit.view.model.GitBlitViewModel;
import com.baloise.egitblit.view.model.ProjectViewModel;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Guess what: Copy Git url to clipboard
 * @see Action 
 * @author MicBag
 *
 */
public class CopyAction extends ViewActionBase{
	public final static String ID = "com.baloise.egitblit.cmd.copy";

	public CopyAction(Viewer viewer){
		super(ID,viewer, "Copy");
		ImageDescriptor img = getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_COPY);
		setImageDescriptor(img);
		ImageDescriptor imgDisabled = getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_COPY_DISABLED);
		setDisabledImageDescriptor(imgDisabled);
	}

	@Override
	public void doRun(){
        if(handleErrorModel() == true){
			return; // Error state: Can't perform action
		}
		GitBlitViewModel model = getSelectedModel();
		if(model != null){
			if(model instanceof ProjectViewModel){
                CloneProtocol cloneProtocol = getPrefModel().getCloneProtocol();
                String url;
                switch(cloneProtocol) {
                    case SSH:
                        url = getSshUrl((ProjectViewModel) model);
                        break;
                    default:
                        url = ((ProjectViewModel) model).getGitUrl();
                        break;
                }

                Clipboard clipboard = new Clipboard(getDisplay());
                clipboard.setContents(new Object[] { url }, new Transfer[] { TextTransfer.getInstance() });
            }
		}
	}

    private String getSshUrl(ProjectViewModel model) {
        PreferenceModel prefModel = getPrefModel();

        for (GitBlitServer gitBlitServer : prefModel.getServerList()) {
            if (gitBlitServer.url.equalsIgnoreCase(model.getServerUrl())) {
                return model.getSshUrl(gitBlitServer.sshPort);
            }
        }

        return null;
    }
}
