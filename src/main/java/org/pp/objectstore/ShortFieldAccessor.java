package org.pp.objectstore;

import static org.pp.objectstore.Util.extend;
import static org.pp.objectstore.interfaces.Constants.D_TYP_SHRT;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;

import org.pp.objectstore.interfaces.FieldAccessor;
/**
 * Short Field accessor
 * @author prasantsmac
 *
 */
final class ShortFieldAccessor extends AbstractFieldAccessor {
	/**
	 * Backing short value
	 */
	private short val;
	
	/**
	 * 
	 * @param fld
	 */
	protected ShortFieldAccessor(Field fld) {
		super(fld);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Extract short value from buffer
	 * @param buf
	 * @return
	 */
	static final short parseShort(ByteBuffer buf) {
		// validate type
		if (buf.get() != D_TYP_SHRT)
			throw new RuntimeException("Short data type was expected");
		// extract value
		return buf.getShort();
	}
	/**
	 * Serialise short value to buffer
	 * @param buf
	 * @param val
	 * @return
	 */
	static final ByteBuffer serialise(ByteBuffer buf, short val) {
		// extend buffer if necessary
		buf = extend(buf, 3);
		// set type to buffer
		buf.put(D_TYP_SHRT);
		// set value to buffer
		buf.putShort(val);
		// return buffer
		return buf;				
	}

	@Override
	public ByteBuffer deserialize(ByteBuffer buf, Object target) throws Exception {
		// get short value from buffer
		short val = parseShort(buf);
		// set it to target
		fld.setShort(target, val);
		// return buffer
		return buf;
	}

	@Override
	public void deserialize(Object val, Object target) throws Exception {
		// cast to short
		short lval = (short) val;
		// set it to target
		fld.setShort(target, lval);		
	}

	@Override
	public Object deserialize(ByteBuffer buf) throws Exception {
		// TODO Auto-generated method stub
		return parseShort(buf);
	}

	@Override
	public ByteBuffer serializeField(ByteBuffer buf, Object target) throws Exception {
		// get short value from the target
		short val = fld.getShort(target);
		// serialise and return buffer
		return serialise(buf, val);
	}

	@Override
	public ByteBuffer serialize(ByteBuffer buf, Object value) throws Exception {
		// cast to short
		short val = (short) value;
		// serialise and return buffer
		return serialise(buf, val);
	}

	@Override
	public void skip(ByteBuffer buf) throws Exception {
		// skip bytes
		buf.position(buf.position() + 3);	
		
	}
	/**
	 * Common short value
	 * @return
	 */
	private short getShortValue() {
		// if already parsed
		if (pos < 0)
			return val;
		// set to the correct buffer position
		buf.position(pos);
		// parse short value
		val = parseShort(buf);
		// indicate parsing is over
		pos = -1;
		// return value
		return val;
	}
	
	@Override
	public Object get() throws Exception {
		// TODO Auto-generated method stub
		return getShortValue();
	}
	@Override
	public void set(Object target) throws Exception {
		// get short value
		short v = getShortValue();
		// set short value
		fld.setShort(target, v);		
	}
	
	@Override
	public FieldAccessor newInstance() throws Exception {
		// TODO Auto-generated method stub
		return new ShortFieldAccessor(null);
	}	
	
	@Override
	public void set(ByteBuffer buf) throws Exception {
		// save buffer reference
		this.buf = buf;
		// save current buffer position
		this.pos = buf.position();
		// move position pointer
		buf.position(this.pos + 3);
	}
}
