package org.pp.objectstore;

import static org.pp.objectstore.Util.extend;
import static org.pp.objectstore.interfaces.Constants.D_TYP_STR;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;

import org.pp.objectstore.interfaces.FieldAccessor;
/**
 * String Field accessor
 * @author prasantsmac
 *
 */
final class StringFieldAccessor extends AbstractFieldAccessor {
	/**
	 * Backing string value
	 */
	private String val;
		
	/**
	 * Extract string value from buffer
	 * @param buf
	 * @return
	 */
	static final String parseString(ByteBuffer buf) {
		// validate type
		if (buf.get() != D_TYP_STR)
			throw new RuntimeException("String data type was expected");
		// get string length
		int len = buf.getInt();
		// allocate char arrays of length
		char[] chars = new char[len];
		// get character by character
		for (int i = 0; i < len; i++)
			chars[i] = buf.getChar();
		// decode to string
		return new String(chars);
	}
	/**
	 * Serialise string value to buffer
	 * @param buf
	 * @param val
	 * @return
	 */
	static final ByteBuffer serialise(ByteBuffer buf, String val) {
		// if string value is null 
		val = val == null ? "" : val;
		// get the length of the string
		int len = val.length();
		// extend buffer if necessary
		buf = extend(buf, 5 + len * 2);
		// set string type
		buf.put(D_TYP_STR);
		// set char length
		buf.putInt(len);
		// copy chars
		for (int i = 0; i < len; i++)
			buf.putChar(val.charAt(i));
		// return extended buffer
		return buf;
	}

	@Override
	public ByteBuffer deserialize(ByteBuffer buf, Object target, Field fld) throws Exception {
		// get string from buffer
		String val = parseString(buf);
		// set it to target
		fld.set(target, val);
		// return buffer
		return buf;
	}

	@Override
	public void deserialize(Object val, Object target, Field fld) throws Exception {
		// cast to string
		String lval = (String) val;
		// set it to target
		fld.set(target, lval);		
	}

	@Override
	public Object deserialize(ByteBuffer buf) throws Exception {
		return parseString(buf);
	}

	@Override
	public ByteBuffer serialize(ByteBuffer buf, Object target, Field fld) throws Exception {
		// get field value
		String val = (String) fld.get(target);
		// serialise and return buffer
		return serialise(buf, val);
	}

	@Override
	public ByteBuffer serialize(ByteBuffer buf, Object value) throws Exception {
		// get field value
		String val = (String) value;
		// serialise and return buffer
		return serialise(buf, val);
	}

	@Override
	public void skip(ByteBuffer buf) throws Exception {
		// skip entire string bytes
		buf.get(); // skip type
		int len = buf.getInt(); // get string length
		buf.position(buf.position() + len * 2); // skip entire string		
	}
	/**
	 * 
	 * @return
	 */
	private String getStringValue() {
		// if already parsed
		if (pos < 0)
			return val;
		// set to the correct buffer position
		buf.position(pos);
		// parse string value
		val = parseString(buf);
		// indicate parsing is over
		pos = -1;
		// return value
		return val;
	}
	
	@Override
	public Object get() throws Exception {
		// TODO Auto-generated method stub
		return getStringValue();
	}
	
	@Override
	public void set(Object target, Field fld) throws Exception {
		// get String value
		String v = getStringValue();
		// set String value
		fld.set(target, v);		
	}
	
	@Override
	public FieldAccessor newInstance() throws Exception {
		// TODO Auto-generated method stub
		return new StringFieldAccessor();
	}	
	
}
