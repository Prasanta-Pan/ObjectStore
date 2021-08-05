package org.pp.objectstore;

import static org.pp.objectstore.Util.extend;
import static org.pp.objectstore.interfaces.Constants.D_TYP_FLT;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;

import org.pp.objectstore.interfaces.FieldAccessor;

/**
 * Float field accessor
 * 
 * @author prasantsmac
 *
 */
final class FloatFieldAccessor extends AbstractFieldAccessor {
	/**
	 * Backing float value
	 */
	private float val;
	
    /**
     * 	Extract float value from the byte buffer
     * @param buf
     * @return
     */
	static final float parseFloat(ByteBuffer buf) {
		// validate type
		if (buf.get() != D_TYP_FLT)
			throw new RuntimeException("Float data type was expected");
		// extract value
		return buf.getFloat();	
	}
	/**
	 * Serialise float value to the byte buffer
	 * @param buf
	 * @param val
	 * @return
	 */
	static final ByteBuffer serialise(ByteBuffer buf, float val) {
		// extend buffer if necessary
		buf = extend(buf, 5);
		// set type to buffer
		buf.put(D_TYP_FLT);
		// set value to buffer
		buf.putFloat(val);
		// return buffer
		return buf;
	}
	
	@Override
	public ByteBuffer deserialize(ByteBuffer buf, Object target, Field fld) throws Exception {
		// get float value
		float val = parseFloat(buf);
		// set to target
		fld.setFloat(target, val);
		// return buffer
		return buf;
	}
	
	@Override
	public void deserialize(Object val, Object target, Field fld) throws Exception {
		// cast to float
		float lval = (float) val;
		// set to target
	    fld.setFloat(target, lval);		
	}
	
	@Override
	public Object deserialize(ByteBuffer buf) throws Exception {
		// parse and return
		return parseFloat(buf);
	}
	
	@Override
	public ByteBuffer serialize(ByteBuffer buf, Object target, Field fld) throws Exception {
		// get value from target
		float val = fld.getFloat(target);
		// serialise and return buffer
		return serialise(buf, val);
	}
	
	@Override
	public ByteBuffer serialize(ByteBuffer buf, Object value) throws Exception {
		// cast to float
		float val = (float) value;
		// serialise and return buffer
		return serialise(buf, val);
	}
	
	@Override
	public void skip(ByteBuffer buf) throws Exception {
		// skip bytes
		buf.position(buf.position() + 5);		
	}
	
	/**
     * common get double value
     * @return
     */
	private float getFloatValue() {
		// if already parsed
		if (pos < 0)
			return val;
		// set to the correct buffer position
		buf.position(pos);
		// parse float value
		val = parseFloat(buf);
		// indicate parsing is over
		pos = -1;
		// return value
		return val;
	}
	
	@Override
	public Object get() throws Exception {
		// TODO Auto-generated method stub
		return getFloatValue();
	}
	@Override
	public void set(Object target, Field fld) throws Exception {
		// get float value
		float v = getFloatValue();
		// set float value
		fld.setFloat(target, v);		
	}
	
	@Override
	public FieldAccessor newInstance() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}	

}
