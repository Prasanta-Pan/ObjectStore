package org.pp.objectstore;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;

interface FieldAccessor {
	/**
     * Get the value from byte buffers and set it to field
     * @param buf
     * @param target TODO
     * @return TODO
     * @throws Exception TODO
     */
	ByteBuffer set(ByteBuffer buf, Object target) throws Exception;
	/**
	 * Convert backing byte array to object than set it to target
	 * @param target
	 * @throws Exception
	 */
	void set(Object target) throws Exception;
	/**
	 * Get the value from field and set to byte buffer
	 * @param buf
	 * @param target TODO
	 * @throws Exception TODO
	 */
	ByteBuffer get(ByteBuffer buf, Object target) throws Exception;	
	
	/**
	 * Get the value of the field
	 * @param buf TODO
	 * @param value TODO
	 * @return
	 * @throws Exception TODO
	 */
	ByteBuffer toBytes(ByteBuffer buf, Object value) throws Exception;
	/**
	 * Convert backing byte array to object
	 * @return
	 * @throws Exception
	 */
	Object get() throws Exception;
	/**
	 * Get corresponding field
	 * @return
	 */
	Field getField();
	/**
	 * Get Field name
	 * @return
	 */
	String getName();
	/**
	 * Validate provided value
	 * @param buf
	 * @param target TODO
	 * @return TODO
	 * @throws Exception TODO
	 */
	ByteBuffer validateType(ByteBuffer buf, Object target) throws Exception;	
	/**
	 * Get a cloned field accessor with a backing bytes to be converted to object later.
	 * @param buf
	 * @return
	 * @throws Exception
	 */
	
	FieldAccessor clone(ByteBuffer buf) throws Exception;	
}
