package com.baloise.egitblit.main;

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.statushandlers.StatusManager;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin{

	// The plug-in ID
	public static final String PLUGIN_ID = "com.baloise.egitblit.plugin"; //$NON-NLS-1$

	public final static String MSG_PREFIX = "Gitblit Repository Explorer: ";

	// The shared instance
	private static Activator plugin;

	/**
	 * The constructor
	 */
	public Activator(){
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext context) throws Exception{
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	public void stop(BundleContext context) throws Exception{
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static Activator getDefault(){
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path){
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	@Override
	protected void initializeImageRegistry(ImageRegistry registry){
		super.initializeImageRegistry(registry);
		try{
			Bundle bundle = Platform.getBundle(PLUGIN_ID);
			URL url;
			SharedImages[] items = SharedImages.values();
			for(SharedImages item : items){
				url = FileLocator.find(bundle, new Path(item.getImagePath()), null);
				ImageDescriptor desc = ImageDescriptor.createFromURL(url);
				registry.put(item.ID, desc);
			}
		}catch(Exception e){
			logError("Error initializing image registry of plugin", e);
		}
	}

	
	public final static Image getImage(SharedImages img){
		return getDefault().getImageRegistry().get(img.ID);
	}

	public final static ImageDescriptor getImageDescriptor(SharedImages img){
		return getDefault().getImageRegistry().getDescriptor(img.ID);
	}

	// ------------------------------------------------------------------------
	// General mehtods
	// ------------------------------------------------------------------------
	/**
	 * Handle messages
	 * 
	 * @param status
	 *            @see {@link IStatus}
	 * @param style
	 *            @See {@link StatusManager}
	 * @param msg
	 *            Message
	 * @param e
	 *            Throwable
	 */
	public final static void handleMessage(int status, int style, String msg, Throwable e){
		StatusManager.getManager().handle(new Status(status, Activator.PLUGIN_ID, 0, MSG_PREFIX + msg, e), style);
	}

	// ------------------------------------------------------------------------
	// --- Mesasge logging
	// ------------------------------------------------------------------------
	public final static void logMessage(int status, String msg, Throwable e){
		handleMessage(status, StatusManager.LOG, msg, e);
	}

	public final static void logError(String msg){
		logError(msg, null);
	}

	public final static void logError(String msg, Throwable e){
		logMessage(IStatus.ERROR, msg, e);
	}

	// ------------------------------------------------------------------------
	// --- Message showing
	// ------------------------------------------------------------------------
	public final static void showMessage(int status, String msg, Throwable e){
		handleMessage(status, StatusManager.SHOW, msg, e);
	}

	public final static void showError(String msg, Throwable e){
		showMessage(IStatus.ERROR, msg, e);
	}

	public final static void showInfo(String msg, Throwable e){
		showMessage(IStatus.INFO, msg, e);
	}

	public final static void showInfo(String msg){
		showInfo(msg, null);
	}

	public final static void showWarning(String msg, Throwable e){
		showMessage(IStatus.WARNING, msg, e);
	}

	// ------------------------------------------------------------------------
	// --- Combinations of logging an showing a message
	// ------------------------------------------------------------------------
	public final static void showAndLogMessage(int status, String msg, Throwable e){
		handleMessage(status, StatusManager.SHOW | StatusManager.LOG, msg, e);
	}

	public final static void showAndLogError(String msg, Throwable e){
		handleMessage(IStatus.ERROR, StatusManager.SHOW | StatusManager.LOG, msg, e);
	}

	public final static void showAndLogError(Throwable e){
		handleMessage(IStatus.ERROR, StatusManager.SHOW | StatusManager.LOG, "", e);
	}

}
