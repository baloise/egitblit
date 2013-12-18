package com.baloise.egitblit.main;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Common Tool methods to get in touch with the eclipse/workbench environment / framework
 * @author MicBag
 *
 */
public class EclipseLog{

	public static void error(String msg){
		IStatus s = new Status(IStatus.ERROR, Activator.PLUGIN_ID, msg);
		Activator.getDefault().getLog().log(s);
	}

	public static void error(String msg, Throwable e){
		IStatus s = new Status(IStatus.ERROR, Activator.PLUGIN_ID, msg, e);
		Activator.getDefault().getLog().log(s);
	}

}
