package com.baloise.egitblit.pref;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * @see IStructuredContentProvider
 * @author MicBag
 *
 */
public class PreferenceModelProvider implements IStructuredContentProvider{

	@Override
	public void dispose(){
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput){
	}

	@Override
	public Object[] getElements(Object inputElement){
		if(inputElement instanceof PreferenceModel){
			return ((PreferenceModel)inputElement).getServerList().toArray();
		}
		return null;
	}

}
