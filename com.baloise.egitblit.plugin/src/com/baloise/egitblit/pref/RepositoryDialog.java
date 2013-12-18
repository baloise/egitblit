package com.baloise.egitblit.pref;

import java.net.URL;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Dialog for editing a gitblit server entry
 * @author MicBag
 *
 */
public class RepositoryDialog extends TitleAreaDialog{

	private Text ctrlUrl;
//	private Text ctrlUrlSep;
	private Text ctrlUser;
	private Text ctrlPwd;
	
	public String url;
	public String urlSep;
	public String user;
	public String pwd;

	public RepositoryDialog(Shell parentShell){
		super(parentShell);
	}

	@Override
	public void create(){
		super.create();
		setTitle("Gitblit Server Definition");
		setMessage("Please enter or update your Gitblit Repository settings");
		setHelpAvailable(false);
	}

	@Override
	protected void okPressed(){
		this.url = ctrlUrl.getText();
//		this.urlSep = ctrlUrlSep.getText();
		this.user = ctrlUser.getText();
		this.pwd = ctrlPwd.getText();

		if(!containsValue(this.url)){
			MessageDialog.open(IStatus.INFO,getShell(),"Invalid Url","Missing URL.",SWT.NONE);
			return;
		}
		try{
			URL url = new URL(this.url);
			url.toURI();
		}catch(Exception e){
			MessageDialog.open(IStatus.INFO,getShell(),"Invalid Url","The entered URL is invalid.",SWT.NONE);
			return;
		}
		
		if(containsValue(this.user) && !containsValue(this.pwd)){
			MessageDialog.open(IStatus.INFO,getShell(),"Invalid authorisation data","Missing password.",SWT.NONE);
			return;
		}
		if(!containsValue(this.user) && containsValue(this.pwd)){
			this.pwd = null;
		}
		super.okPressed();
	}
	
	private boolean containsValue(String value){
		return !(value == null || value.trim().isEmpty());
	}

	
	
	@Override
	protected Control createDialogArea(Composite parent){
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout layout = new GridLayout(2, false);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		container.setLayout(layout);

		GridData gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = GridData.FILL;

		new Label(container, SWT.NONE).setText("URL:");
		ctrlUrl = new Text(container, SWT.BORDER);
		ctrlUrl.setLayoutData(gd);

//		new Label(container, SWT.NONE).setText("URL separator:");
//		ctrlUrlSep = new Text(container, SWT.BORDER);
//		ctrlUrlSep.setLayoutData(gd);

		new Label(container, SWT.NONE).setText("User:");		
		ctrlUser = new Text(container, SWT.BORDER);
		ctrlUser.setLayoutData(gd);
		
		new Label(container, SWT.NONE).setText("Password:");		
		ctrlPwd = new Text(container, SWT.BORDER |  SWT.PASSWORD);
		ctrlPwd.setLayoutData(gd);
		
		if(url == null || url.trim().isEmpty()){
			url = "https://";
		}
		if(user == null || user.trim().isEmpty()){
			user = System.getProperty("user.name");
			pwd = "";;
		}
//		if(urlSep == null || urlSep.trim().isEmpty()){
//			urlSep = GitBlitServer.DEF_URL_SEPARATOR;
//		}
		
		ctrlUrl.setText(url);
//		ctrlUrlSep.setText(urlSep);
		ctrlUser.setText(user);
		ctrlPwd.setText(pwd);

		return area;
	}
}
