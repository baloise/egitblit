package ch.basler.plugin.gitblitexplorer.common;

/**
 * @author MicBag
 *
 */
public class GitBlitBDException extends Exception {

	private static final long serialVersionUID = 1L;

	public GitBlitBDException() {
		super();
	}

	public GitBlitBDException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

	public GitBlitBDException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public GitBlitBDException(String arg0) {
		super(arg0);
	}

	public GitBlitBDException(Throwable arg0) {
		super(arg0);
	}
	
}
