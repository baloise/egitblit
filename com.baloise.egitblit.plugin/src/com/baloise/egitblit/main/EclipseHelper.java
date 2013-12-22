package com.baloise.egitblit.main;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * Common Tool methods to get in touch with the eclipse/workbench environment / framework
 * @author MicBag
 *
 */
public class EclipseHelper{

	public final static String MSG_PREFIX = "Gitblit Repository Explorer: ";

	// ------------------------------------------------------------------------
	// General mehtods
	// ------------------------------------------------------------------------
	/** 
	 * Handle messages 
	 * @param status @see {@link IStatus}
	 * @param style @See {@link StatusManager}
	 * @param msg Message
	 * @param e Throwable
	 */
	public final static void handleMessage(int status, int style, String msg, Throwable e){
		StatusManager.getManager().handle(new Status(status,Activator.PLUGIN_ID, 0, MSG_PREFIX + msg, e),style);
	}

	// ------------------------------------------------------------------------
	// --- Mesasge logging
	// ------------------------------------------------------------------------
	public final static void logMessage(int status, String msg, Throwable e){
		handleMessage(status,StatusManager.LOG,msg,e);
	}

	public final static void logError(String msg){
		logError(msg,null);
	}

	public final static void logError(String msg, Throwable e){
		logMessage(IStatus.ERROR,msg,e);
	}
	
	// ------------------------------------------------------------------------
	// --- Message showing
	// ------------------------------------------------------------------------
	public final static void showMessage(int status, String msg, Throwable e){
		handleMessage(status, StatusManager.SHOW, msg, e);
	}

	public final static void showError(String msg, Throwable e){
		showMessage(IStatus.ERROR,msg,e);
	}

	public final static void showInfo(String msg, Throwable e){
		showMessage(IStatus.INFO,msg,e);
	}

	public final static void showInfo(String msg){
		showInfo(msg,null);
	}

	public final static void showWarning(String msg, Throwable e){
		showMessage(IStatus.WARNING,msg,e);
	}
	
	// ------------------------------------------------------------------------
	// --- Combinations of logging an showing a message
	// ------------------------------------------------------------------------
	public final static void showAndLogMessage(int status, String msg, Throwable e){
		handleMessage(status, StatusManager.SHOW | StatusManager.LOG,msg, e);
	}
	
	public final static void showAndLogError(String msg, Throwable e){
		handleMessage(IStatus.ERROR, StatusManager.SHOW | StatusManager.LOG,msg, e);
	}
	
	public final static void showAndLogError(Throwable e){
		handleMessage(IStatus.ERROR, StatusManager.SHOW | StatusManager.LOG, "",e);
	}

}
