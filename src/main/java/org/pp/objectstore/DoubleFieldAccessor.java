package org.pp.objectstore;

import static org.pp.objectstore.DataTypes.D_TYP_DBL;
import static org.pp.objectstore.Util.extend;
import static org.pp.objectstore.Util.invalidType;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;

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
	 * Only constructor
	 * 
	 * @param fld
	 */
	protected DoubleFieldAccessor(Field fld) {
		super(fld);
	}

	// common
	private ByteBuffer put(ByteBuffer buf, double val) {
		buf = extend(buf, 9);
		buf.put(D_TYP_DBL);
		buf.putDouble(val);
		return buf;
	}

	@Override
	public ByteBuffer set(ByteBuffer buf, Object target) throws Exception {
		if (buf.get() != D_TYP_DBL)
			invalidType(fld);
		fld.setDouble(target, buf.getDouble());
		return buf;
	}

	@Override
	public void set(Object target) throws Exception {
		// TODO Auto-generated method stub
		fld.setDouble(target, val);
	}

	@Override
	public ByteBuffer get(ByteBuffer buf, Object target) throws Exception {
		double val = fld.getDouble(target);
		return put(buf, val);
	}

	@Override
	public Object get() throws Exception {
		// TODO Auto-generated method stub
		return val;
	}

	@Override
	public ByteBuffer toBytes(ByteBuffer buf, Object value) throws Exception {
		double newVal = (double) value;
		return put(buf, newVal);
	}

	@Override
	public ByteBuffer validateType(ByteBuffer buf, Object target) throws Exception {
		if (!(target instanceof Double))
			invalidType(fld);
		double v = (double) target;
		return put(buf, v);
	}

	@Override
	public FieldAccessor clone(ByteBuffer buf) throws Exception {
		// TODO Auto-generated method stub
		if (buf.get() != D_TYP_DBL)
			invalidType(fld);
		// create a new instance
		DoubleFieldAccessor dfAccessor = new DoubleFieldAccessor(fld);
		dfAccessor.val = buf.getDouble();
		return dfAccessor;
	}

}
