package com.baloise.egitblit.pref;

import java.net.URL;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Dialog for editing a gitblit server entry
 * 
 * @author MicBag
 * 
 */
public class RepositoryDialog extends TitleAreaDialog {

  private Text ctrlUrl;
  private Text ctrlUser;
  private Text ctrlPwd;
  private Combo ctrlProtocol;
  private Button ctrlCloneEnabled;
  private Button ctrlReset;
  private Text ctrlPort;

  public String url;
  public String urlSep;
  public String user;
  public String pwd;
  public CloneSettings cloneSettings;

  public RepositoryDialog(Shell parentShell) {
    super(parentShell);
  }

  @Override
  public void create() {
    super.create();
    setTitle("Gitblit Server Definition");
    setMessage("Please enter or update your Gitblit server settings");
    setHelpAvailable(false);
  }

  @Override
  protected void okPressed() {
    this.url = ctrlUrl.getText();
    this.user = ctrlUser.getText();
    this.pwd = ctrlPwd.getText();

    if(!containsValue(this.url)){
      MessageDialog.open(IStatus.INFO, getShell(), "Invalid Url", "Missing URL.", SWT.NONE);
      return;
    }

    try{
      // Address must be a valid http(s) url
      URL url = new URL(this.url);
      url.toURI();
    }
    catch(Exception e){
      MessageDialog.open(IStatus.INFO, getShell(), "Invalid Url", "The entered URL is invalid.", SWT.NONE);
      return;
    }

    if(containsValue(this.user) && !containsValue(this.pwd)){
      MessageDialog.open(IStatus.INFO, getShell(), "Invalid authorisation data", "Missing password.", SWT.NONE);
      return;
    }
    if(!containsValue(this.user) && containsValue(this.pwd)){
      this.pwd = null;
    }

    CloneProtocol prot = CloneProtocol.getByIndex(this.ctrlProtocol.getSelectionIndex());
    if(prot == null){
      MessageDialog.open(IStatus.INFO, getShell(), "Invalid clone settings", "Internal error: Can't determinate clone protocol.\nClone Settings are restored to its default values.", SWT.NONE);
      this.cloneSettings = new CloneSettings(false);
    }
    else{
      String tmp = ctrlPort.getText();
      Integer port = null;
      if(tmp != null && tmp.trim().length() > 0){
        try{
          port = Integer.valueOf(tmp.trim());
        }
        catch(Exception e){
          MessageDialog.open(IStatus.INFO, getShell(), "Invalid clone settings", "Invalid port.", SWT.NONE);
          return;
        }
      }
      this.cloneSettings = new CloneSettings(prot, port);
      this.cloneSettings.setEnabled(this.ctrlCloneEnabled.getSelection());
    }
    super.okPressed();
  }

  private boolean containsValue(String value) {
    return !(value == null || value.trim().isEmpty());
  }

  @Override
  protected Control createDialogArea(Composite parent) {
    Composite area = (Composite)super.createDialogArea(parent);

    GridData gd;

    // Composite grid data
    GridData cgd = GridDataFactory.swtDefaults().create();
    cgd.horizontalAlignment = SWT.FILL;
    cgd.verticalAlignment = SWT.FILL;
    cgd.grabExcessHorizontalSpace = true;
    cgd.grabExcessVerticalSpace = true;

    GridData fgd = GridDataFactory.swtDefaults().create();
    fgd.horizontalAlignment = SWT.FILL;
    fgd.verticalAlignment = SWT.FILL;
    fgd.horizontalSpan = 2;
    fgd.grabExcessHorizontalSpace = true;
    fgd.grabExcessVerticalSpace = true;

    Composite root = new Composite(area, SWT.FILL);
    root.setLayout(new GridLayout(3, false));
    root.setLayoutData(cgd);
    Label l;

    ctrlUrl = addTextField(root, "URL:");
    ctrlUrl.setLayoutData(fgd);
    
    ctrlUrl.addFocusListener(new FocusListener() {
      @Override
      public void focusLost(FocusEvent e) {
        syncPortFromURL();
      }
      
      @Override
      public void focusGained(FocusEvent e) {
      }
    });
    
    ctrlUser = addTextField(root, "User:");
    ctrlUser.setLayoutData(fgd);
    ctrlPwd = addTextField(root, "Password:", SWT.PASSWORD);
    ctrlPwd.setLayoutData(fgd);

    if(url == null || url.trim().isEmpty()){
      url = CloneProtocol.HTTPS.schema + "://";
    }
    if(user == null || user.trim().isEmpty()){
      user = System.getProperty("user.name");
      pwd = "";
    }

    l = new Label(root, SWT.SEPARATOR | SWT.HORIZONTAL);
    gd = GridDataFactory.copyData(cgd);
    gd.horizontalSpan = 3;
    l.setLayoutData(gd);

    l = addLabel(root, "Cloning:");
    gd = GridDataFactory.copyData(fgd);
    gd.verticalAlignment = SWT.LEFT;
    gd.grabExcessHorizontalSpace = false;
    gd.horizontalSpan = 0;
    l.setLayoutData(gd);

    ctrlCloneEnabled = new Button(root, SWT.CHECK);
    ctrlCloneEnabled.setText("Override Clone Protocol");
    ctrlCloneEnabled.addSelectionListener(new SelectionListener() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        enableCloningFields();
      }

      @Override
      public void widgetDefaultSelected(SelectionEvent e) {}
    });

    gd = GridDataFactory.copyData(fgd);
    gd.horizontalAlignment = SWT.LEFT;
    gd.grabExcessHorizontalSpace = false;
    gd.horizontalSpan = 2;
    ctrlCloneEnabled.setLayoutData(gd);

    addLabel(root, null);
    Composite pcomp = new Composite(root, SWT.NONE);
    gd = GridDataFactory.copyData(cgd);
    gd.horizontalAlignment = SWT.LEFT;
    gd.grabExcessHorizontalSpace = true;
    gd.horizontalSpan = 2;
    pcomp.setLayoutData(gd);
    pcomp.setLayout(new GridLayout(6, false));

    addLabel(pcomp, "Protocol:");

    ctrlProtocol = new Combo(pcomp, SWT.READ_ONLY);
    ctrlProtocol.setItems(CloneProtocol.getDisplayValues());

    ctrlProtocol.addSelectionListener(new SelectionListener() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        int val = ctrlProtocol.getSelectionIndex();
        CloneProtocol cp = CloneProtocol.getByIndex(val);
        String pval = "";
        if(cp != null){
          pval = cp.defaultPort != null ? "" + cp.defaultPort : "";
        }
        ctrlPort.setText(pval);
        ctrlPort.setEnabled(pval.length() > 0);
        syncPortFromURL();
      }

      @Override
      public void widgetDefaultSelected(SelectionEvent e) {}
    });

    ctrlPort = addTextField(pcomp, "Port:");
    gd = GridDataFactory.copyData(fgd);
    gd.verticalAlignment = SWT.LEFT;
    gd.widthHint = 40;
    gd.grabExcessHorizontalSpace = false;
    gd.horizontalSpan = 0;
    ctrlPort.setLayoutData(gd);

    ctrlPort.addVerifyListener(new VerifyListener() {
      @Override
      public void verifyText(VerifyEvent e) {
        if(e.keyCode == SWT.ARROW_LEFT || e.keyCode == SWT.ARROW_RIGHT || e.keyCode == SWT.BS || e.keyCode == SWT.DEL || e.keyCode == SWT.MODIFIER_MASK || e.character == 0){
          return;
        }
        e.doit = ("" + e.character).matches("\\d*\\.?\\d+");
      }
    });

    ctrlReset = new Button(pcomp, SWT.PUSH);
    ctrlReset.setText("Reset");
    ctrlReset.addSelectionListener(new SelectionListener() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        CloneProtocol prot = CloneProtocol.getByIndex(ctrlProtocol.getSelectionIndex());
        if(prot != null){
          String val = prot.defaultPort != null ? prot.defaultPort.toString() : "";
          ctrlPort.setText(val);
        }
      }

      @Override
      public void widgetDefaultSelected(SelectionEvent e) {}
    });
    
    // --- Init with data
    ctrlUrl.setText(url);
    ctrlUser.setText(user);
    ctrlPwd.setText(pwd);

    if(this.cloneSettings == null){
      this.cloneSettings = new CloneSettings(false); // Create with defaults
    }

    CloneProtocol cp = this.cloneSettings.getCloneProtocol();
    this.ctrlProtocol.select(cp.index);
    Integer port = this.cloneSettings.getPort();
    this.ctrlPort.setEnabled(cp.defaultPort != null);
    if(port != CloneSettings.NO_PORT){
      this.ctrlPort.setText("" + port);
    }
    else{
      this.ctrlPort.setText("");
    }
    ctrlCloneEnabled.setSelection((this.cloneSettings.isEnabled()));
    enableCloningFields();
    return area;
  }

  private Text addTextField(Composite root, String label, int fieldStyle) {
    addLabel(root, label);
    Text t = new Text(root, SWT.BORDER | fieldStyle);
    return t;
  }

  private Text addTextField(Composite root, String label) {
    return addTextField(root, label, SWT.NONE);
  }

  private Label addLabel(Composite root, String text) {
    Label l = new Label(root, SWT.NONE);
    if(text != null){
      l.setText(text);
    }
    return l;
  }

  private void syncPortFromURL(){
    try{
      CloneProtocol cp = CloneProtocol.getByIndex(this.ctrlProtocol.getSelectionIndex());
      if(cp == CloneProtocol.HTTP || cp == CloneProtocol.HTTPS){
        int port = -1;
        URL u = new URL(ctrlUrl.getText());
        port = u.getPort();
        CloneProtocol ucp = CloneProtocol.getValue(u.getProtocol());
        String sport = "";
        if(cp == ucp){
          if(port > 0){
            sport += port;
          }
          ctrlPort.setText(sport);
        }
      }
    }
    catch(Exception e){
    }
  }
  
  private void enableCloningFields() {
    boolean enable = ctrlCloneEnabled.getSelection();

    this.ctrlPort.setEnabled(enable);
    this.ctrlProtocol.setEnabled(enable);
    this.ctrlReset.setEnabled(enable);

    CloneProtocol cp = CloneProtocol.getByIndex(this.ctrlProtocol.getSelectionIndex());
    if(cp != null){
      this.ctrlPort.setEnabled(cp.defaultPort != null && enable);
    }
    else{
      this.ctrlPort.setEnabled(enable);
    }
  }

}
