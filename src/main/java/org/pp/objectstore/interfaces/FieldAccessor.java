package org.pp.objectstore.interfaces;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;

public interface FieldAccessor {
	/**
	 * Get the value from ByteBuffer set it to target object using the Field
	 * @param buf
	 * @param target
	 * @param fld
	 * @return
	 * @throws Exception
	 */
	public ByteBuffer deserialize(ByteBuffer buf, Object target) throws Exception;
	/**
	 * 
	 * @param val
	 * @param target
	 * @param fld
	 */
	public void deserialize(Object val, Object target) throws Exception ;	
	/**
	 * DE serialise from byte buffer to proper object
	 * @param buf
	 * @return
	 */
	public Object deserialize(ByteBuffer buf) throws Exception ;
	/**
	 * Get value
	 * @return
	 * @throws Exception
	 */
	public Object get() throws Exception;
	
	/**
	 * Get corresponding field object
	 * @return
	 */
	public Field getField();
	/**
	 * Set value to target
	 * @param target
	 * @param fld
	 * @throws Exception
	 */
	public void set(Object target) throws Exception;
	/**
	 * Get the value from target object and set it to byte buffer
	 * @param buf
	 * @param target
	 * @param fld
	 * @return
	 * @throws Exception
	 */
	public ByteBuffer serializeField(ByteBuffer buf, Object target) throws Exception;	
		
	/**
	 * Serialise value to byte buffer
	 * @param buf TODO
	 * @param value TODO
	 * @return
	 * @throws Exception TODO
	 */
	public ByteBuffer serialize(ByteBuffer buf, Object value) throws Exception;
	/**
	 * Skip bytes of type
	 * @param buf
	 */
	public void skip(ByteBuffer buf) throws Exception ;
	/**
	 * 
	 * @param buf
	 * @return
	 */
	public FieldAccessor newInstance() throws Exception;
	/**
	 * Set byte buffer and position
	 * @param buf
	 * @param pos
	 * @throws Exception
	 */
	public void set(ByteBuffer buf) throws Exception;	
	
}
