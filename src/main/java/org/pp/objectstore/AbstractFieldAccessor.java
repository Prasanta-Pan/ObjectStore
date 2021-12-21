package org.pp.objectstore;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;

import org.pp.objectstore.interfaces.FieldAccessor;

abstract class AbstractFieldAccessor implements FieldAccessor {
	/**
	 * Backing byte array to point to
	 */
	protected ByteBuffer buf;
	/**
	 * Position in the byte array
	 */
	protected int pos;
	/**
	 * Corresponding field object
	 */
	protected Field fld;
	
	/**
	 * 
	 */
	protected AbstractFieldAccessor(Field fld) { 
		this.fld = fld;
	}
	
	/**
	 * Set both variables
	 * @param val
	 * @param pos
	 */
	protected AbstractFieldAccessor(ByteBuffer buf, int pos) {
		this.pos = pos;
		this.buf = buf;
	}
	
	@Override
	public Field getField() {
		return fld;
	}
		
}
