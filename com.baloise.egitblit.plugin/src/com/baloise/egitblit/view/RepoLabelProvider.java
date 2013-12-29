package com.baloise.egitblit.view;

import com.baloise.egitblit.view.model.GitBlitViewModel;

public interface RepoLabelProvider{

	public String getColumnText(GitBlitViewModel element, int columnIndex);
}
