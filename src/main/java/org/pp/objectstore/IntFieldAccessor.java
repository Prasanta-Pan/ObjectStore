package org.pp.objectstore;

import static org.pp.objectstore.Util.extend;
import static org.pp.objectstore.interfaces.Constants.D_TYP_INT;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;

import org.pp.objectstore.interfaces.FieldAccessor;

/**
 * Int field accessor
 * 
 * @author prasantsmac
 *
 */
final class IntFieldAccessor extends AbstractFieldAccessor {
	/**
	 * Backing int value
	 */
	private int val;
	
	/**
	 * 
	 * @param fld
	 */
	protected IntFieldAccessor(Field fld) {
		super(fld);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Extract integer value from byte buffer
	 * @param buf
	 * @return
	 */
	static final int parseInt(ByteBuffer buf) {
		// validate type
		if (buf.get() != D_TYP_INT)
			throw new RuntimeException("Integer data type was expected");
		// extract value
		return buf.getInt();
	}
	/**
	 * Serialise integer value to byte buffer
	 * @param buf
	 * @param val
	 * @return
	 */
	static final ByteBuffer serialise(ByteBuffer buf, int val) {
		// extend buffer if necessary
		buf = extend(buf, 5);
		// set type to buffer
		buf.put(D_TYP_INT);
		// set value to buffer
		buf.putInt(val);
		// return buffer
		return buf;
	}

	@Override
	public ByteBuffer deserialize(ByteBuffer buf, Object target) throws Exception {
		// get integer value
		int val = parseInt(buf);
		// set it to target
		fld.setInt(target, val);
		// return byte buffer
		return buf;
	}

	@Override
	public void deserialize(Object val, Object target) throws Exception {
		// cast value to integer
		int lval = (int) val;
		// set it to target
		fld.setInt(target, lval);		
	}

	@Override
	public Object deserialize(ByteBuffer buf) throws Exception {
		// get integer and return
		return parseInt(buf);
	}

	@Override
	public ByteBuffer serializeField(ByteBuffer buf, Object target) throws Exception {
		// get integer value from target
		int val = fld.getInt(target);
		// serialise and return buffer		
		return serialise(buf, val);
	}

	@Override
	public ByteBuffer serialize(ByteBuffer buf, Object value) throws Exception {
		// cast to integer value
		int val = (int) value;
		// serialise and return buffer		
		return serialise(buf, val);
	}

	@Override
	public void skip(ByteBuffer buf) throws Exception {
		// skip bytes
		buf.position(buf.position() + 5);	
	}
	/**
	 * Common get int value
	 * @return
	 */
	private int getIntValue() {
		// if already parsed
		if (pos < 0)
			return val;
		// set to the correct buffer position
		buf.position(pos);
		// parse int value
		val = parseInt(buf);
		// indicate parsing is over
		pos = -1;
		// return value
		return val;
	}
	
	@Override
	public Object get() throws Exception {
		// TODO Auto-generated method stub
		return getIntValue();
	}
	
	@Override
	public void set(Object target) throws Exception {
		// get int value
		int v = getIntValue();
		// set field value
		fld.set(target, v);		
	}
	
	@Override
	public FieldAccessor newInstance() throws Exception {
		// TODO Auto-generated method stub
		return new IntFieldAccessor(fld);
	}
	
	@Override
	public void set(ByteBuffer buf) throws Exception {
		// save buffer reference
		this.buf = buf;
		// save current buffer position
		this.pos = buf.position();
		// move position pointer
		buf.position(this.pos + 5);
	}

}
