package org.pp.objectstore;

import static org.pp.objectstore.Util.extend;
import static org.pp.objectstore.interfaces.Constants.D_TYP_BYTE;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;

import org.pp.objectstore.interfaces.FieldAccessor;

/**
 * Byte field accessor
 * 
 * @author prasantsmac
 *
 */
final class ByteFieldAccessor extends AbstractFieldAccessor {
	/**
	 * Backing byte value
	 */
	private byte val;
	
	/**
	 * 
	 * @param fld
	 */
	protected ByteFieldAccessor(Field fld) {
		super(fld);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * 	Common parsing of byte value
	 * @param buf
	 * @return
	 */
	static final byte parseByte(ByteBuffer buf) {
		// type check
		if (buf.get() != D_TYP_BYTE)
			throw new RuntimeException("A byte value was expected");
		// return actual value
		return buf.get();
	}	
	/**
	 * Common serialiser for byte value
	 * @param buf
	 * @param val
	 * @return
	 */
	static final ByteBuffer serialise(ByteBuffer buf, byte val) {
		// extend buffer if necessary
		buf = extend(buf, 2);
		// set to type to buffer
		buf.put(D_TYP_BYTE);
		// set value to buffer
		buf.put(val);
		// return buffer
		return buf;
	}

	@Override
	public ByteBuffer deserialize(ByteBuffer buf, Object target) throws Exception {
		// get byte value from target
		byte val = parseByte(buf);
		// set byte value to the target
		fld.setByte(target, val);
		// serialise byte value
		return buf;
	}

	@Override
	public void deserialize(Object val, Object target) throws Exception {
		// cast to byte value
		byte lVal = (byte) val;
		// set it to target
		fld.setByte(target, lVal);		
	}

	@Override
	public Object deserialize(ByteBuffer buf) throws Exception {
		return parseByte(buf);
	}

	@Override
	public ByteBuffer serializeField(ByteBuffer buf, Object target) throws Exception {
		// get byte value from the target
		byte val = fld.getByte(target);
		// serialise and return buffer
		return serialise(buf, val);
	}

	@Override
	public ByteBuffer serialize(ByteBuffer buf, Object value) throws Exception {
		// cast to byte value
		byte val = (byte) value;
		// serialise and return
		return serialise(buf, val);
	}

	@Override
	public void skip(ByteBuffer buf) throws Exception {
		// skip bytes
		buf.position(buf.position() + 2);
	}
	
	/**
	 * Common method to get value
	 * @return
	 */
	private byte getByteVal() {
		// TODO Auto-generated method stub
		if (pos < 0)
			return val;
		// set to the correct buffer position
		buf.position(pos);
		// parse boolean value
		val = parseByte(buf);
		// indicate value is ready
		pos = -1;
		// return value
		return val;
	}
	
	@Override
	public Object get() {
		// TODO Auto-generated method stub
		return getByteVal();
	}
	
	@Override
	public void set(Object target) throws Exception {
		// get value
		byte v = getByteVal();
		// set value
		fld.setByte(target, v);
	}
	
	@Override
	public FieldAccessor newInstance() throws Exception {
		// TODO Auto-generated method stub
		return new ByteFieldAccessor(fld);
	}
	
	@Override
	public void set(ByteBuffer buf) throws Exception {
		// save buffer reference
		this.buf = buf;
		// save current buffer position
		this.pos = buf.position();
		// move position pointer
		buf.position(this.pos + 2);
	}
	
}
