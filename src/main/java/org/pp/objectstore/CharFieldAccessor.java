package org.pp.objectstore;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;

import static org.pp.objectstore.interfaces.Constants.D_TYP_CHAR;
import static org.pp.objectstore.Util.extend;

import org.pp.objectstore.interfaces.FieldAccessor;
/**
 * Char Field accessor
 * @author prasantsmac
 *
 */
final class CharFieldAccessor extends AbstractFieldAccessor {
	/**
	 * Backing char value
	 */
	private char val;
	
	/**
	 * Common parse char method	
	 * @param buf
	 * @return
	 */
	static final char parseChar(ByteBuffer buf) {
		if (buf.get() != D_TYP_CHAR)
			throw new RuntimeException("Char data type was expected");
		// get char value
		return buf.getChar();
	}
	/**
	 * Common char serialiser
	 * @param buf
	 * @param val
	 */
	static final ByteBuffer serialise(ByteBuffer buf, char val) {
		// extend buffer if necessary
		buf = extend(buf, 3);
		// set to type to buffer
		buf.put(D_TYP_CHAR);
		// set value to buffer
		buf.putChar(val);
		// return buffer
		return buf;
	}

	@Override
	public ByteBuffer deserialize(ByteBuffer buf, Object target, Field fld) throws Exception {
		// get char value from the buffer
		char val = parseChar(buf);
		// set it to target
		fld.setChar(target, val);
		return buf;
	}

	@Override
	public void deserialize(Object val, Object target, Field fld) throws Exception {
		// cast to char
		char lval = (char) val;
		// set it to target
		fld.setChar(target, lval);		
	}

	@Override
	public Object deserialize(ByteBuffer buf) throws Exception {
		// get char value and return
		return parseChar(buf);
	}

	@Override
	public ByteBuffer serialize(ByteBuffer buf, Object target, Field fld) throws Exception {
		// get value from target
		char val = fld.getChar(target);
		// serialise and return buffer
		return serialise(buf, val);
	}

	@Override
	public ByteBuffer serialize(ByteBuffer buf, Object value) throws Exception {
		// cast to char value
		char val = (char) value;
		// serialise and return buffer
		return serialise(buf, val);
	}

	@Override
	public void skip(ByteBuffer buf) throws Exception {
		// skip bytes
		buf.position(buf.position() + 3);
	}
	
	/**
	 * Common get char value
	 * @return
	 */
	private char getCharVal() {
		// if already parsed
		if (pos < 0)
			return val;
		// set to the correct buffer position
		buf.position(pos);
		// parse boolean value
		val = parseChar(buf);
		// indicate parsing is over
		pos = -1;
		// return value
		return val;
	}
	
	@Override
	public Object get() throws Exception {
		// TODO Auto-generated method stub
		return getCharVal();
	}
	
	@Override
	public void set(Object target, Field fld) throws Exception {
		// get char value
		char v = getCharVal();
		// set char value
		fld.setChar(target, v);
	}
	
	@Override
	public FieldAccessor newInstance() throws Exception {
		// TODO Auto-generated method stub
		return new CharFieldAccessor();
	}		
}
