package org.pp.objectstore;

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
	 * 
	 */
	protected AbstractFieldAccessor() { }
	
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
	public void set(ByteBuffer buf) throws Exception {
		// TODO Auto-generated method stub
		pos = buf.position();
		this.buf = buf;
	}	
}
