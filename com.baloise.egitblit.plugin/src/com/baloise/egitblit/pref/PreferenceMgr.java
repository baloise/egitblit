package com.baloise.egitblit.pref;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.Preferences;

import com.baloise.egitblit.common.GitBlitServer;
import com.baloise.egitblit.main.EclipseLog;
import com.baloise.egitblit.main.GitBlitExplorerException;

public class PreferenceMgr{

	// --- Root node
	public final static String KEY_GITBLIT_ROOT = "com.baloise.gitblit";

	// --- global settings under root node
	public final static String KEY_GITBLIT_DCLICK = "com.baloise.gitblit.dobuleclick";

	// Node containing the list of servers
	public final static String KEY_GITBLIT_SERVER = "com.baloise.gitblit.server";
	// --- Server entry
	public final static String KEY_GITBLIT_SERVER_URL = "com.baloise.gitblit.server.url";
	public final static String KEY_GITBLIT_SERVER_ACTIVE = "com.baloise.gitblit.server.active";
//	public final static String KEY_GITBLIT_SERVER_URL_SEP = "com.baloise.gitblit.server.url.separator";
	public final static String KEY_GITBLIT_SERVER_USER = "com.baloise.gitblit.server.user";
	public final static String KEY_GITBLIT_SERVER_PASSWORD = "com.baloise.gitblit.server.password";

	public final static String VALUE_GITBLIT_URL_SEPERATOR = "/";


	/**
	 * Reads the configuration, scope INSTANCE
	 * @return ConfigModel
	 * @throws GitBlitExplorerException
	 */
	public static PreferenceModel readConfig() throws GitBlitExplorerException{
		PreferenceModel prefModel = new PreferenceModel();

		IEclipsePreferences pref = InstanceScope.INSTANCE.getNode(KEY_GITBLIT_ROOT);

		if(pref == null){
			final String msg = "Can´t access preferences for gitblit explorer. Continue with new (empty) settings.";
			EclipseLog.error(msg);
			return prefModel;
		}

		// --- Read global settings
		int val = pref.getInt(KEY_GITBLIT_DCLICK, PreferenceModel.DoubleClickBehaviour.OpenGitBlit.value);
		prefModel.setDoubleClick(PreferenceModel.DoubleClickBehaviour.getValue(val));

		// ---- Read list of servers
		try{
			// Root node
			Preferences serverNode = pref.node(KEY_GITBLIT_SERVER);
			// An entry
			Preferences entryNode;
			String url, user, pwd, urlSep;
			boolean active;

			String[] names = serverNode.childrenNames();
			for(String item : names){
				entryNode = serverNode.node(item);
				url = entryNode.get(KEY_GITBLIT_SERVER_URL, null);
//				urlSep = entryNode.get(KEY_GITBLIT_SERVER_URL_SEP, VALUE_GITBLIT_URL_SEPERATOR);
				active = entryNode.getBoolean(KEY_GITBLIT_SERVER_ACTIVE, true);
				user = entryNode.get(KEY_GITBLIT_SERVER_USER, null);
				pwd = entryNode.get(KEY_GITBLIT_SERVER_PASSWORD, null);
				prefModel.addRepository(url, active, user, pwd);
			}
		}catch(Exception e){
			EclipseLog.error("Error reading preferences. Continue with configuration settings which have been read so far.", e);
		}
		return prefModel;
	}

	public static void saveConfig(PreferenceModel prefModel) throws GitBlitExplorerException{
		if(prefModel == null){
			final String msg = "Error saving GitBlit Explorer settings: No preference model avail (internal error). Configuration will not be saved.";
			EclipseLog.error(msg);
			throw new GitBlitExplorerException(msg);
		}
		IEclipsePreferences pref = InstanceScope.INSTANCE.getNode(KEY_GITBLIT_ROOT);

		if(pref == null){
			final String msg = "Can´t access preferences for gitblit explorer. Confriguration will not be saved.";
			EclipseLog.error(msg);
			throw new GitBlitExplorerException(msg);
		}

		// --- Saving global settings
		pref.putInt(KEY_GITBLIT_DCLICK, prefModel.getDoubleClick().value);

		Preferences serverNode = pref.node(KEY_GITBLIT_SERVER);
		Preferences entryNode;

		// --- Saving server settings
		try{
			// First, erase old settings
			serverNode.removeNode();
			pref.flush();

			// --- Write new settings
			int count = 0;
			serverNode = pref.node(KEY_GITBLIT_SERVER);
			for(GitBlitServer item : prefModel.getServerList()){
				entryNode = serverNode.node(KEY_GITBLIT_SERVER_URL + "_" + count++);
				entryNode.put(KEY_GITBLIT_SERVER_URL, trim(item.url));
//				entryNode.put(KEY_GITBLIT_SERVER_URL_SEP, item.urlSeparator);
				entryNode.putBoolean(KEY_GITBLIT_SERVER_ACTIVE, item.active);
				entryNode.put(KEY_GITBLIT_SERVER_USER, trim(item.user));
				entryNode.put(KEY_GITBLIT_SERVER_PASSWORD, item.password);
			}
			pref.flush();
		}catch(Exception e){
			EclipseLog.error("Error saving gitblit preference settings: Can´t remove older server settings.", e);
		}
	}
	
	private final static String trim(String value){
		if(value == null){
			return null;
		}
		return value.trim();
	}
	
	public final static boolean isValidConfig(){
		return true;
//		private void checkPreferences(){
//		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
//		String value = preferenceStore.getString(GitBlitExplorerPrefPage.KEY_GITBLIT_URL);
//		try{
//			new URL(value);
//		}
//		catch(Exception e){
//			EclipseLog.error("Error while checking preferences",e);
//			showMessage(IStatus.ERROR,"GitBlit Explorer: Configuration Error", "GitBlit URL is not specified or invalid");
//		}
//		value = preferenceStore.getString(GitBlitExplorerPrefPage.KEY_GITBLIT_USER);
//		if(value == null || value.trim().isEmpty()){
//			String msg = "Missing configuration parameter: User";
//			EclipseLog.error(value);
//			showMessage(IStatus.ERROR,"GitBlit Explorer: Configuration Error", msg);
//		}
//
//		value = preferenceStore.getString(GitBlitExplorerPrefPage.KEY_GITBLIT_PWD);
//		if(value == null || value.trim().isEmpty()){
//			String msg = "Missing configuration parameter: Password";
//			EclipseLog.error(value);
//			showMessage(IStatus.ERROR,"GitBlit Explorer: Configuration Error", msg);
//		}
//	}
		
	}
}
