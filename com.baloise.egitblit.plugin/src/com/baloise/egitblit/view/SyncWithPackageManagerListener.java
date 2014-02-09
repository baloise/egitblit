package com.baloise.egitblit.view;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

public class SyncWithPackageManagerListener implements ISelectionListener {
	private static final String PACKAGE_EXPLORER_ID = "org.eclipse.jdt.ui.PackageExplorer";
	private final GitBlitUrlSelectionListener listener;
	IProject lastSelectedProject;
	private boolean enabled;
	private ISelectionService selectionService;

	public static interface GitBlitUrlSelectionListener {
		void select(Set<String> gitBlitUrls);
	}

	public SyncWithPackageManagerListener(GitBlitUrlSelectionListener listener,
			IWorkbenchWindow workbenchWindow) {
		this.listener = listener;
		selectionService = workbenchWindow.getSelectionService();
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (!enabled)
			return;
		IStructuredSelection sel = (IStructuredSelection) selection;
		Set<IProject> projects = getProjects(sel);
		if (projects.size() != 1)
			return;
		IProject project = projects.iterator().next();
		if (lastSelectedProject != null && lastSelectedProject.equals(project))
			return;
		lastSelectedProject = project;
		try {
			IFile gitconf = project.getFile(".git/config");
			if (gitconf.exists()) {
				InputStream in = gitconf.getContents();
				String config = new Scanner(in, "UTF-8").useDelimiter("\\A").next();
				in.close();
				final Set<String> urls = getUrls(config);
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						listener.select(urls);
					}
				});
			}
		} catch (Exception e) {

		}
	}

	private Set<String> getUrls(String config) {
		Set<String> ret = new HashSet<String>();
		String regex = "url\\s*=\\s*(\\S+)\\s";
		Matcher matcher = Pattern.compile(regex).matcher(config);
		while (matcher.find()) {
			ret.add(matcher.group(1));
		}
		return ret;
	}

	private Set<IProject> getProjects(IStructuredSelection sel) {
		if(sel == null) return Collections.emptySet();
		Iterator iterator = sel.iterator();
		Set<IProject> ret = new HashSet<IProject>();
		while (iterator.hasNext()) {
			Object item = iterator.next();
			if (item instanceof IAdaptable) {
				IResource adapter = (IResource) ((IAdaptable) item)
						.getAdapter(IResource.class);
				if (adapter != null) {
					ret.add(adapter.getProject());
				}
			}
		}
		return ret;
	}

	public void setEnabled(boolean enabled) {
		if (enabled == this.enabled)
			return;
		this.enabled = enabled;
		if (enabled) {
			selectionService
					.addPostSelectionListener(PACKAGE_EXPLORER_ID, this);
			ISelection selection = selectionService
					.getSelection(PACKAGE_EXPLORER_ID);
			selectionChanged(null, selection);
		} else {
			lastSelectedProject = null;
			selectionService.removePostSelectionListener(PACKAGE_EXPLORER_ID,
					this);
		}
	}
}