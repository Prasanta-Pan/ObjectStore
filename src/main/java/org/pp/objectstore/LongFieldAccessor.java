package org.pp.objectstore;

import static org.pp.objectstore.Util.extend;
import static org.pp.objectstore.interfaces.Constants.D_TYP_LNG;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;

import org.pp.objectstore.interfaces.FieldAccessor;

/**
 * Long Field accessor
 * 
 * @author prasantsmac
 *
 */
final class LongFieldAccessor extends AbstractFieldAccessor {
	/**
	 * Backing long value
	 */
	private long val;
	
	/**
	 * Extract long value from buffer
	 * @param buf
	 * @return
	 */
	static final long parseLong(ByteBuffer buf) {
		// validate type
		if (buf.get() != D_TYP_LNG)
			throw new RuntimeException("Long data type was expected");
		// extract value
		return buf.getLong();
	}
	/**
	 * Serialise long value to byte buffer
	 * @param buf
	 * @param val
	 * @return
	 */
	static final ByteBuffer serialise(ByteBuffer buf, long val) {
		// extend buffer if necessary
		buf = extend(buf, 9);
		// set type to buffer
		buf.put(D_TYP_LNG);
		// set value to buffer
		buf.putLong(val);
		// return buffer
		return buf;		
	}
	
	@Override
	public ByteBuffer deserialize(ByteBuffer buf, Object target, Field fld) throws Exception {
		// extract long value from the buffer
		long val = parseLong(buf);
		// set it to target
		fld.setLong(target, val);
		// return buffer
		return buf;
	}
	
	@Override
	public void deserialize(Object val, Object target, Field fld) throws Exception {
		// cast to long
		long lval = (long) val;
		// set it to target
		fld.setLong(target, lval);		
	}
	
	@Override
	public Object deserialize(ByteBuffer buf) throws Exception {
		// parse long and return
		return parseLong(buf);
	}
	
	@Override
	public ByteBuffer serialize(ByteBuffer buf, Object target, Field fld) throws Exception {
		// get long value from target
		long val = fld.getLong(target);
		// serialise and return buffer
		return serialise(buf, val);
	}
	
	@Override
	public ByteBuffer serialize(ByteBuffer buf, Object value) throws Exception {
		// cast to long
		long val = (long) value;
		// serialise and return buffer
		return serialise(buf, val);
	}
	
	@Override
	public void skip(ByteBuffer buf) throws Exception {
		// skip bytes
		buf.position(buf.position() + 9);		
	}		
	/**
	 * Common get long value
	 * @return
	 */
	private long getLongValue() {
		// if already parsed
		if (pos < 0)
			return val;
		// set to the correct buffer position
		buf.position(pos);
		// parse long value
		val = parseLong(buf);
		// indicate parsing is over
		pos = -1;
		// return value
		return val;
	}
	
	@Override
	public Object get() throws Exception {
		// TODO Auto-generated method stub
		return getLongValue();
	}
	
	@Override
	public void set(Object target, Field fld) throws Exception {
		// get long value
		long v = getLongValue();
		// set long value
		fld.setLong(target, v);		
	}
	
	@Override
	public FieldAccessor newInstance() throws Exception {
		// TODO Auto-generated method stub
		return new LongFieldAccessor();
	}

}
