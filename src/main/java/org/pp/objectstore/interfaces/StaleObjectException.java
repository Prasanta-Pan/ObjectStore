package org.pp.objectstore.interfaces;

public class StaleObjectException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8763492480392604439L;
	
	public StaleObjectException(String msg)  {
		super(msg);
	}
	
	public StaleObjectException(Class<?> claz) {
		super("Version doesn't match for the class " + claz.getName());
	}

}
