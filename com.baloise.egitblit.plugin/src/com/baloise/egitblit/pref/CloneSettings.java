package com.baloise.egitblit.pref;


/**
 * @author MicBag
 */
public class CloneSettings{

	private boolean enabled;
	private CloneProtocol cloneProtocol;
	private Integer port;
	public final static Integer NO_PORT = -1;

	public CloneSettings(){
		this(false);
	}

	public CloneSettings(CloneProtocol pro, Integer port){
		setCloneProtocol(pro);
		setPort(port);
		setEnabled(true);
	}

	public CloneSettings(boolean enabled){
		this(CloneProtocol.HTTPS, CloneProtocol.HTTPS.defaultPort);
		setEnabled(enabled);
	}

	public boolean isEnabled(){
		return this.enabled;
	}

	public void setEnabled(boolean enabled){
		this.enabled = enabled;
	}

	public CloneProtocol getCloneProtocol(){
		return cloneProtocol;
	}

	public void setCloneProtocol(CloneProtocol cloneProtocol){
		this.cloneProtocol = cloneProtocol;
	}

	public Integer getPort(){
		return port;
	}

	public void setPort(Integer port){
		this.port = port;
		if(this.port == null || this.port < 0){
			this.port = NO_PORT;
		}
	}
}
