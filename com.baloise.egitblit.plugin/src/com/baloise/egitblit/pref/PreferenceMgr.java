package com.baloise.egitblit.pref;

import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;

import com.baloise.egitblit.common.GitBlitExplorerException;
import com.baloise.egitblit.gitblit.GitBlitServer;
import com.baloise.egitblit.main.Activator;

/**
 * Handling preference settings
 * 
 * @author MicBag
 * 
 */
public class PreferenceMgr{

	// --- Root node
	public final static String KEY_GITBLIT_ROOT = "com.baloise.gitblit";

	// --- global settings under root node
	public final static String KEY_GITBLIT_DCLICK = "com.baloise.gitblit.dobuleclick";
	public final static String KEY_GITBLIT_DECORATRE_VIEW = "com.baloise.gitblit.general.viewer.coloring";
	

	// Node containing the list of servers
	public final static String KEY_GITBLIT_SERVER_NODE = "com.baloise.gitblit.server";
	// --- Server entry
	public final static String KEY_GITBLIT_SERVER_URL = "com.baloise.gitblit.server.url";
	public final static String KEY_GITBLIT_SERVER_ACTIVE = "com.baloise.gitblit.server.active";

	// public final static String KEY_GITBLIT_SERVER_URL_SEP =
	// "com.baloise.gitblit.server.url.separator";
	public final static String KEY_GITBLIT_SERVER_USER = "com.baloise.gitblit.server.user";
	public final static String KEY_GITBLIT_SERVER_PASSWORD = "com.baloise.gitblit.server.password";

	public final static String KEY_GITBLIT_SERVER_CLONE_PROTOCOL = "com.baloise.gitblit.server.clone.protocol";
	public final static String KEY_GITBLIT_SERVER_CLONE_PORT     = "com.baloise.gitblit.server.clone.port";
  public final static String KEY_GITBLIT_SERVER_CLONE_ENABLED  = "com.baloise.gitblit.server.clone.enabled";
	
	public final static String KEY_GITBLIT_WS_GROUPNAME = "com.baloise.gitblit.workingset.groupname";

	
	public final static String VALUE_GITBLIT_URL_SEPERATOR = "/";

	/**
	 * Reads the configuration, scope INSTANCE
	 * 
	 * @return ConfigModel
	 * @throws GitBlitExplorerException
	 */
	public static PreferenceModel readConfig() throws GitBlitExplorerException{
		PreferenceModel prefModel = new PreferenceModel();
		try{
			ISecurePreferences pref = SecurePreferencesFactory.getDefault().node(KEY_GITBLIT_ROOT);
			if(pref == null){
				final String msg = "Can't access preferences for Gitblit explorer. Continue with new (empty) settings.";
				Activator.logError(msg);
				return prefModel;
			}

			// --- Read global settings
			prefModel.setDoubleClick(PreferenceModel.DoubleClickBehaviour.getValue(pref.getInt(KEY_GITBLIT_DCLICK, PreferenceModel.DoubleClickBehaviour.OpenGitBlit.value)));
			prefModel.setDecorateView(pref.getBoolean(KEY_GITBLIT_DECORATRE_VIEW, false));
			
			prefModel.setWSGroupNameEnabled(pref.getBoolean(KEY_GITBLIT_WS_GROUPNAME, true));
			
			readServerList(prefModel);
		}catch(Exception e){
			Activator.logError("Error reading preferences. Continue with configuration settings which have been read so far.", e);
		}
		return prefModel;
	}
	
	public static void readServerList(PreferenceModel prefModel){
		if(prefModel == null){
			Activator.logError("Error reading preferences. Missing call parameter PreferenceModel.");
			return;
		}
		try{
			ISecurePreferences pref = SecurePreferencesFactory.getDefault().node(KEY_GITBLIT_ROOT);
			if(pref == null){
				final String msg = "Can�t access preferences for Gitblit explorer. Continue with new (empty) settings.";
				Activator.logError(msg);
				return;
			}

			// --- Column settings
			ISecurePreferences entryNode;
			ISecurePreferences serverNode;
			
			prefModel.clearServerList();
			// ---- Read list of servers
			// Root node
			serverNode = pref.node(KEY_GITBLIT_SERVER_NODE);
			if(serverNode != null){
				// An entry
				String url, user, pwd,protocol;
				Integer port;
				boolean active,enabled;
				CloneSettings set = null;
	
				String[] names = serverNode.childrenNames();
				for(String item : names){
					entryNode = serverNode.node(item);
					url = entryNode.get(KEY_GITBLIT_SERVER_URL, null);
					active = entryNode.getBoolean(KEY_GITBLIT_SERVER_ACTIVE, true);
					user = entryNode.get(KEY_GITBLIT_SERVER_USER, null);
					pwd = entryNode.get(KEY_GITBLIT_SERVER_PASSWORD, null);
					
					protocol = entryNode.get(KEY_GITBLIT_SERVER_CLONE_PROTOCOL, null); 
					port     = entryNode.getInt(KEY_GITBLIT_SERVER_CLONE_PORT, -1);
					enabled  = entryNode.getBoolean(KEY_GITBLIT_SERVER_CLONE_ENABLED, false);
					
					CloneProtocol prot = CloneProtocol.getValue(protocol);
					if(prot == null){
					  set = new CloneSettings(false);
					  Activator.logInfo("Clone Settings for server " + url + " can't be read. Using default clone settings.");
					}
					else{
					  set = new CloneSettings(prot,port);
					  set.setEnabled(enabled);
					}
					prefModel.addRepository(url, active, user, pwd,set);
				}
			}
			else{
				Activator.logError("Error reading preferences. Missing configuration node of servers");	
			}
		}catch(Exception e){
			Activator.logError("Error reading preferences. Continue with configuration settings which have been read so far.", e);
		}
	}

	public static void saveConfig(PreferenceModel prefModel) throws GitBlitExplorerException{
		if(prefModel == null){
			final String msg = "Error saving Gitblit Explorer settings: No preference model avail (internal error). Configuration will not be saved.";
			Activator.logError(msg);
			throw new GitBlitExplorerException(msg);
		}
		ISecurePreferences pref = SecurePreferencesFactory.getDefault().node(KEY_GITBLIT_ROOT);

		if(pref == null){
			final String msg = "Can't access preferences for Gitblit Explorer. Confriguration will not be saved.";
			Activator.logError(msg);
			throw new GitBlitExplorerException(msg);
		}

		try{
			// --- Saving global settings
			pref.putInt(KEY_GITBLIT_DCLICK, prefModel.getDoubleClick().value, false);
			pref.putBoolean(KEY_GITBLIT_DECORATRE_VIEW, prefModel.isDecorateView(), false);

			pref.putBoolean(KEY_GITBLIT_WS_GROUPNAME, prefModel.isWSGroupNameEnabled(), true);
			
			ISecurePreferences entryNode;
			ISecurePreferences serverNode;
			
			// --- Write new settings
			int count = 0;
			serverNode = pref.node(KEY_GITBLIT_SERVER_NODE);
			serverNode.removeNode();
			pref.flush();
			serverNode = pref.node(KEY_GITBLIT_SERVER_NODE);
			
			CloneSettings set; 
			for(GitBlitServer item : prefModel.getServerList()){
				entryNode = serverNode.node(KEY_GITBLIT_SERVER_URL + "_" + count++);
				entryNode.put(KEY_GITBLIT_SERVER_URL, trim(item.url), false);
				entryNode.putBoolean(KEY_GITBLIT_SERVER_ACTIVE, item.active, false);
				entryNode.put(KEY_GITBLIT_SERVER_USER, trim(item.user), false);
				entryNode.put(KEY_GITBLIT_SERVER_PASSWORD, item.password, true);
				set = item.cloneSettings;
				if(set == null){
				  set = new CloneSettings();
				}
				entryNode.put(KEY_GITBLIT_SERVER_CLONE_PROTOCOL,set.getCloneProtocol().schema,false);
				entryNode.putInt(KEY_GITBLIT_SERVER_CLONE_PORT,set.getPort(),false);
				entryNode.putBoolean(KEY_GITBLIT_SERVER_CLONE_ENABLED,set.isEnabled(),false);
			}
			pref.flush();
		}catch(Exception e){
			Activator.logError("Error saving Gitblit preference settings: Can�t remove older server settings.", e);
		}
	}

	private final static String trim(String value){
		if(value == null){
			return null;
		}
		return value.trim();
	}

}
