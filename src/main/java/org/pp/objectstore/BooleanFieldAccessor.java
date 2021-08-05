package org.pp.objectstore;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;

import org.pp.objectstore.interfaces.FieldAccessor;
import static org.pp.objectstore.interfaces.Constants.D_TYP_BOL;
import static org.pp.objectstore.Util.extend;

/**
 * Boolean field accessor
 * @author prasantsmac
 *
 */
final class BooleanFieldAccessor extends  AbstractFieldAccessor {
	/**
	 * Backing boolean value
	 */
	private boolean val;
	
   	@Override
	public ByteBuffer deserialize(ByteBuffer buf, Object target, Field fld) throws Exception {
		// extract boolean value
		boolean val = parseBoolean(buf);
		// set boolean field
		fld.setBoolean(target, val);
		// return buffer
		return buf;
	}

	@Override
	public void deserialize(Object val, Object target, Field fld) throws Exception {
		// TODO Auto-generated method stub
		boolean lVal  = (boolean) val;
		// set boolean field
		fld.setBoolean(target, lVal);
	}

	@Override
	public Object deserialize(ByteBuffer buf) throws Exception {
		// TODO Auto-generated method stub
		return parseBoolean(buf);
	}
	/**
	 * Common serialiser
	 * @param buf
	 * @param val
	 * @return
	 * @throws Exception
	 */
	static final ByteBuffer serialise(ByteBuffer buf, boolean val) throws Exception {
		// serialise
		byte lval = val ? (byte) 1 : 0;
		// extend buffer if necessary
		buf = extend(buf, 2);
		// set to type to buffer
		buf.put(D_TYP_BOL);
		// set value to buffer
		buf.put(lval);
		// return buffer
		return buf;
	}
	/**
	 * Common parsing of boolean value
	 * @param buf
	 * @return
	 * @throws Exception
	 */
	static final boolean parseBoolean(ByteBuffer buf) {
		if (buf.get() != D_TYP_BOL)
			throw new RuntimeException("Value expected to be boolean");
		// convert to boolean value
		boolean val = buf.get() > 0 ? true : false;
		// return value
		return val;
	}

	@Override
	public ByteBuffer serialize(ByteBuffer buf, Object target, Field fld) throws Exception {
		// retrieve boolean value from target
		boolean val = fld.getBoolean(target);
		// serialise boolean
		return serialize(buf, val);
	}

	@Override
	public ByteBuffer serialize(ByteBuffer buf, Object value) throws Exception {
		// TODO Auto-generated method stub
		boolean lVal  = (boolean) value;
		// serialise boolean
		return serialize(buf, lVal);
	}	

	@Override
	public void skip(ByteBuffer buf) {
		// skip bytes
		buf.position(buf.position() + 2);
	}
    /**
     * Common get boolean
     * @return
     */
	private boolean getBoolVal() {
		// if parsed already
		if (pos < 0)
			return val;
		// set buffer position
		buf.position(pos);
		// parse value
		val = parseBoolean(buf);
		// indicate parsing is done
		pos = -1;
		// return boolean value
		return val;
	}
	
	@Override
	public Object get() throws Exception {
		// TODO Auto-generated method stub
		return getBoolVal();
	}

	@Override
	public void set(Object target, Field fld) throws Exception {
		// get boolean value
		boolean v = getBoolVal();
		// set boolean
		fld.setBoolean(target, v);
	}
	
	@Override
	public FieldAccessor newInstance() throws Exception {
		// TODO Auto-generated method stub
		return new BooleanFieldAccessor();
	}
}
