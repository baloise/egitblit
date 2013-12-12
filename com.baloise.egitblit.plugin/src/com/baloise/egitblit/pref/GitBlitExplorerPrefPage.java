package com.baloise.egitblit.pref;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.baloise.egitblit.main.Activator;

/**
 * Preference Page
 * @author MicBag
 *
 */
public class GitBlitExplorerPrefPage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	// Keys to store / access data at preference store
	public final static String KEY_GITBLIT_URL = "gitblit.url";
	public final static String KEY_GITBLIT_USER = "gitblit.user";
	public final static String KEY_GITBLIT_PWD = "gitblit.password";
	public final static String KEY_GITBLIT_DCLICK = "gitblit.dobuleclick";
	public final static String VALUE_GITBLIT_DCLICK_GIT = "git";
	public final static String VALUE_GITBLIT_DCLICK_GITBLIT = "gitblit";
	
	public GitBlitExplorerPrefPage() {
		super(GRID);
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Configure GitBlit Explorer");
	}

	@Override
	protected void createFieldEditors() {
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		String surl  = preferenceStore.getString(KEY_GITBLIT_URL);
		
		StringFieldEditor url = new StringFieldEditor(KEY_GITBLIT_URL, 	"GitBlit server url",getFieldEditorParent());
		if(surl == null || surl.trim() == ""){
			surl = "";
		}
		addField(url);
		
		 addField(new StringFieldEditor(KEY_GITBLIT_USER, "GitBlit user",getFieldEditorParent()));
		 StringFieldEditor pwd = new StringFieldEditor(KEY_GITBLIT_PWD, "GitBlit password",getFieldEditorParent()){
			 @Override
		     protected void doFillIntoGrid(Composite parent, int numColumns) {
		         super.doFillIntoGrid(parent, numColumns);
		         getTextControl().setEchoChar('*');
		     }

			@Override
			public boolean isEmptyStringAllowed() {
				return false;
			}
			 
		 };
		 addField(pwd);
		 
		 addField(new RadioGroupFieldEditor(KEY_GITBLIT_DCLICK,
				    "Double click behaviour", 1,
				    new String[][] { { "Copy Git Link to clipboard", VALUE_GITBLIT_DCLICK_GIT},
				            { "Open GitBlit", VALUE_GITBLIT_DCLICK_GITBLIT } }, getFieldEditorParent()));
	}
}