package org.pp.objectstore.interfaces;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;

public interface FieldAccessor {
	/**
     * Get the value from byte buffers and set it to field
     * @param buf
     * @param target TODO
     * @return TODO
     * @throws Exception TODO
     */
	public ByteBuffer set(ByteBuffer buf, Object target) throws Exception;
	/**
	 * Get the value from field and set to byte buffer
	 * @param buf
	 * @param target TODO
	 * @throws Exception TODO
	 */
	public ByteBuffer get(ByteBuffer buf, Object target) throws Exception;	
	
	/**
	 * Get the value of the field
	 * @param target TODO
	 * @return
	 * @throws Exception TODO
	 */
	public Object get(Object target) throws Exception;	
	/**
	 * Get corresponding field
	 * @return
	 */
	public Field getField();
	/**
	 * Validate provided value
	 * @param buf
	 * @param val TODO
	 * @return TODO
	 */
	public ByteBuffer validateType(ByteBuffer buf, Object val);
	
	
}
