package org.pp.objectstore.interfaces;

public class StaleObjectException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8763492480392604439L;
	
	public StaleObjectException(String msg)  {
		super("Version doesn't match for the class " + msg);
	}
	
	public StaleObjectException(Class<?> claz) {
		super("Version doesn't match for the class " + claz.getName());
	}

}
