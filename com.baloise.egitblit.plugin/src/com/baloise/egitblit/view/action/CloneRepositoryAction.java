package com.baloise.egitblit.view.action;

import org.eclipse.jface.viewers.Viewer;

import com.baloise.egitblit.view.model.GitBlitViewModel;
import com.baloise.egitblit.view.model.ProjectViewModel;

public class CloneRepositoryAction extends ViewActionBase{

	public CloneRepositoryAction(Viewer viewer){
		super(viewer, null, "Clone repository");
	}

	@Override
	public void doRun(){
		try{
			GitBlitViewModel model = getSelectedModel();
			if(model instanceof ProjectViewModel){

			}
		}catch(Exception e){
		}
	}

	protected boolean cloneRepository(){
		GitBlitViewModel model = getSelectedModel();
		if(model instanceof ProjectViewModel){
			ProjectViewModel pmodel = (ProjectViewModel)model;

//			URIish uri = new URIish(pmodel.getGitURL());
//			String destDir = UIUtils.getDefaultRepositoryDir();
			
//			UserPasswordCredentials credentials = gitRepositoryInfo.getCredentials();
//			setWindowTitle(NLS.bind(UIText.GitCloneWizard_jobName, uri.toString()));
//			final boolean allSelected;
//			final Collection<Ref> selectedBranches;
//			if(validSource.isSourceRepoEmpty()){
//				// fetch all branches of empty repo
//				allSelected = true;
//				selectedBranches = Collections.emptyList();
//			}else{
//				allSelected = validSource.isAllSelected();
//				selectedBranches = validSource.getSelectedBranches();
//			}
			
//			final File workdir = new File(destDir);
//			final Ref ref = cloneDestination.getInitialBranch();
//			final String remoteName = cloneDestination.getRemote();
//
//			boolean created = workdir.exists();
//			if(!created)
//				created = workdir.mkdirs();
//
//			if(!created || !workdir.isDirectory()){
//				final String errorMessage = NLS.bind(UIText.GitCloneWizard_errorCannotCreate, workdir.getPath());
//				ErrorDialog.openError(getShell(), getWindowTitle(), UIText.GitCloneWizard_failed, new Status(IStatus.ERROR, Activator.getPluginId(), 0, errorMessage, null));
//				// let's give user a chance to fix this minor problem
//				return false;
//			}
//
//			int timeout = Activator.getDefault().getPreferenceStore().getInt(UIPreferences.REMOTE_CONNECTION_TIMEOUT);
//			final CloneOperation op = new CloneOperation(uri, allSelected, selectedBranches, workdir, ref != null ? ref.getName() : null, remoteName, timeout);
//			if(credentials != null)
//				op.setCredentialsProvider(new UsernamePasswordCredentialsProvider(credentials.getUser(), credentials.getPassword()));
//			op.setCloneSubmodules(cloneDestination.isCloneSubmodules());
//
//			configureFetchSpec(op, gitRepositoryInfo, remoteName);
//			configurePush(op, gitRepositoryInfo, remoteName);
//			configureRepositoryConfig(op, gitRepositoryInfo);
//
//			if(cloneDestination.isImportProjects()){
//				final IWorkingSet[] sets = cloneDestination.getWorkingSets();
//				op.addPostCloneTask(new PostCloneTask() {
//					public void execute(Repository repository, IProgressMonitor monitor) throws CoreException{
//						importProjects(repository, sets);
//					}
//				});
//			}
//
//			alreadyClonedInto = workdir.getPath();
//
//			if(!callerRunsCloneOperation)
//				runAsJob(uri, op, gitRepositoryInfo);
//			else
//				cloneOperation = op;
//			return true;
		}
		return false;
	}
}
