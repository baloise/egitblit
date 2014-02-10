package com.baloise.egitblit.view.wizard;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.egit.core.op.ConnectProviderOperation;
import org.eclipse.egit.ui.Activator;
import org.eclipse.egit.ui.internal.UIText;
import org.eclipse.egit.ui.internal.clone.GitCreateGeneralProjectPage;
import org.eclipse.egit.ui.internal.clone.GitImportWizard;
import org.eclipse.egit.ui.internal.clone.GitProjectsImportPage;
import org.eclipse.egit.ui.internal.clone.GitSelectWizardPage;
import org.eclipse.egit.ui.internal.clone.ProjectRecord;
import org.eclipse.egit.ui.internal.clone.ProjectUtils;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
// stolen from org.eclipse.egit.ui.internal.clone.GitCreateProjectViaWizardWizard ;-)
public class GitCreateStandardProjectWizard extends Wizard {
	private final Repository myRepository;

	private final String myGitDir;

	private GitCreateGeneralProjectPage myCreateGeneralProjectPage;

	/**
	 * @param repository
	 * @param path
	 */
	public GitCreateStandardProjectWizard(Repository repository, String path) {
		super();
		myRepository = repository;
		myGitDir = path;
		setNeedsProgressMonitor(true);
		setWindowTitle(NLS.bind(UIText.GitCreateProjectViaWizardWizard_WizardTitle, myRepository.getDirectory().getPath()));
	}

	@Override
	public void addPages() {
		myCreateGeneralProjectPage = new GitCreateGeneralProjectPage(myGitDir) {
			@Override
			public void setVisible(boolean visible) {
				setPath(myGitDir);
				super.setVisible(visible);
			}
		};
		addPage(myCreateGeneralProjectPage);

	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		if (page == myCreateGeneralProjectPage) {
			return null;
		}
		return super.getNextPage(page);
	}

	@Override
	public boolean canFinish() {
		return myCreateGeneralProjectPage.isPageComplete();
	}

	@Override
	public boolean performFinish() {
		try {
			getContainer().run(true, true, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					importProjects(monitor);
				}
			});
		} catch (InvocationTargetException e) {
			Activator.handleError(e.getCause().getMessage(), e.getCause(), true);
			return false;
		} catch (InterruptedException e) {
			Activator.handleError(UIText.GitCreateProjectViaWizardWizard_AbortedMessage, e, true);
			return false;
		}
		return true;
	}

	private void importProjects(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

		final String[] projectName = new String[1];
		final boolean[] defaultLocation = new boolean[1];
		// get the data from the page in the UI thread
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			public void run() {
				projectName[0] = myCreateGeneralProjectPage.getProjectName();
				defaultLocation[0] = myCreateGeneralProjectPage.isDefaultLocation();
			}
		});

		try {
			IWorkspaceRunnable wsr = new IWorkspaceRunnable() {
				public void run(IProgressMonitor actMonitor) throws CoreException {
					final IProjectDescription desc = ResourcesPlugin.getWorkspace().newProjectDescription(projectName[0]);
					if (!defaultLocation[0])
						desc.setLocation(new Path(myGitDir));

					IProject prj = ResourcesPlugin.getWorkspace().getRoot().getProject(desc.getName());
					prj.create(desc, actMonitor);
					prj.open(actMonitor);

					ResourcesPlugin.getWorkspace().getRoot().refreshLocal(IResource.DEPTH_ONE, actMonitor);

					File repoDir = myRepository.getDirectory();
					ConnectProviderOperation cpo = new ConnectProviderOperation(prj, repoDir);
					cpo.execute(new NullProgressMonitor());
				}
			};
			ResourcesPlugin.getWorkspace().run(wsr, monitor);

		} catch (CoreException e) {
			throw new InvocationTargetException(e);
		}
	}
}
