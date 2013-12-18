package com.baloise.egitblit.main;

/**
 * Exception which will be used by all GitBlit Explorer classes (this is just a wrapper)
 * @author MicBag
 *
 */
public class GitBlitExplorerException extends Exception {

	private static final long serialVersionUID = 1L;

	public GitBlitExplorerException() {
		super();
	}

	public GitBlitExplorerException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public GitBlitExplorerException(String arg0) {
		super(arg0);
	}

	public GitBlitExplorerException(Throwable arg0) {
		super(arg0);
	}
}
