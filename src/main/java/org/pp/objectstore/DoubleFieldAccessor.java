package org.pp.objectstore;

import static org.pp.objectstore.Util.extend;
import static org.pp.objectstore.interfaces.Constants.D_TYP_DBL;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;

import org.pp.objectstore.interfaces.FieldAccessor;

/**
 * Double field accessor
 * 
 * @author prasantsmac
 *
 */
final class DoubleFieldAccessor extends AbstractFieldAccessor {
	/**
	 * Backing double value
	 */
	private double val;
	
	/**
	 * 
	 * @param fld
	 */
	protected DoubleFieldAccessor(Field fld) {
		super(fld);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Extract double value from byte buffer
	 * @param buf
	 * @return
	 */
	static final double parseDouble(ByteBuffer buf) {
		// validate type
		if (buf.get() != D_TYP_DBL)
			throw new RuntimeException("Double data type was expected");
		// extract value
		return buf.getDouble();		
	}
	
	/**
	 * Serialise double value
	 * @param buf
	 * @param val
	 * @return
	 */
	static final ByteBuffer serialise(ByteBuffer buf, double val) {
		// extend buffer if necessary
		buf = extend(buf, 9);
		// set type to buffer
		buf.put(D_TYP_DBL);
		// set value to buffer
		buf.putDouble(val);
		// return buffer
		return buf;
	}

	@Override
	public ByteBuffer deserialize(ByteBuffer buf, Object target) throws Exception {
		// extract value from buffer
		double val = parseDouble(buf);
		// set value to target
		fld.setDouble(target, val);
		// return buffer
		return buf;
	}

	@Override
	public void deserialize(Object val, Object target) throws Exception {
		// cast to double
		double lval = (double) val;
		// set value to target
		fld.setDouble(target, lval);		
	}

	@Override
	public Object deserialize(ByteBuffer buf) throws Exception {
		// extract double value and return
		return parseDouble(buf);
	}

	@Override
	public ByteBuffer serializeField(ByteBuffer buf, Object target) throws Exception {
		// get double value from the target
		double val = fld.getDouble(target);
		// serialise and return buffer
		return serialise(buf, val);
	}

	@Override
	public ByteBuffer serialize(ByteBuffer buf, Object value) throws Exception {
		// cast to double
		double val = (double) value;
		// serialise and return buffer
		return serialise(buf, val);
	}

	@Override
	public void skip(ByteBuffer buf) throws Exception {
		// skip bytes
		buf.position(buf.position() + 9);
	}
    /**
     * common get double value
     * @return
     */
	private double getDoubleValue() {
		// if already parsed
		if (pos < 0)
			return val;
		// set to the correct buffer position
		buf.position(pos);
		// parse double value
		val = parseDouble(buf);
		// indicate parsing is over
		pos = -1;
		// return value
		return val;
	}
	
	@Override
	public Object get() throws Exception {
		// TODO Auto-generated method stub
		return getDoubleValue();
	}

	@Override
	public void set(Object target) throws Exception {
		// get double value
		double v = getDoubleValue();
		// set double value
		fld.setDouble(target, v);
	}

	@Override
	public FieldAccessor newInstance() throws Exception {
		// TODO Auto-generated method stub
		return new DoubleFieldAccessor(fld);
	}
	
	@Override
	public void set(ByteBuffer buf) throws Exception {
		// save buffer reference
		this.buf = buf;
		// save current buffer position
		this.pos = buf.position();
		// move position pointer
		buf.position(this.pos + 9);
	}
}
