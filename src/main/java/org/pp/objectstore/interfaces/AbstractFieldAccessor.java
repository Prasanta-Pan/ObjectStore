package org.pp.objectstore.interfaces;

import java.lang.reflect.Field;

/**
 * 
 * @author prasantsmac
 *
 */
public abstract class AbstractFieldAccessor implements FieldAccessor {
	/**
	 * Field to be used for set/get of value
	 */
	protected Field fld;
    /**
     * 
     * @param data
     * @param fld
     */
	protected AbstractFieldAccessor(Field fld) {
		this.fld = fld;		
	}
	
	@Override
	public Field getField() {
		return fld;
	}

}
