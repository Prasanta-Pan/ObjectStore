package org.pp.objectstore;

import static org.pp.objectstore.DataTypes.D_TYP_FLT;
import static org.pp.objectstore.Util.extend;
import static org.pp.objectstore.Util.invalidType;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;

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
	 * Only constructor
	 * 
	 * @param fld
	 */
	protected FloatFieldAccessor(Field fld) {
		super(fld);
	}

	// common
	private ByteBuffer put(ByteBuffer buf, float val) {
		buf = extend(buf, 5);
		buf.put(D_TYP_FLT);
		buf.putFloat(val);
		return buf;
	}

	@Override
	public ByteBuffer set(ByteBuffer buf, Object target) throws Exception {
		if (buf.get() != D_TYP_FLT)
			invalidType(fld);
		fld.setFloat(target, buf.getFloat());
		return buf;
	}

	@Override
	public void set(Object target) throws Exception {
		// TODO Auto-generated method stub
		fld.setFloat(target, val);
	}

	@Override
	public ByteBuffer get(ByteBuffer buf, Object target) throws Exception {
		float val = fld.getFloat(target);
		return put(buf, val);
	}

	@Override
	public ByteBuffer toBytes(ByteBuffer buf, Object value) throws Exception {
		float newVal = (float) value;
		return put(buf, newVal);
	}

	@Override
	public Object get() throws Exception {
		// TODO Auto-generated method stub
		return val;
	}

	@Override
	public ByteBuffer validateType(ByteBuffer buf, Object target) throws Exception {
		if (!(target instanceof Float))
			invalidType(fld);
		float v = (float) target;
		return put(buf, v);
	}

	@Override
	public FieldAccessor clone(ByteBuffer buf) throws Exception {
		// TODO Auto-generated method stub
		if (buf.get() != D_TYP_FLT)
			invalidType(fld);
		// create a new instance
		FloatFieldAccessor ffAccessor = new FloatFieldAccessor(fld);
		ffAccessor.val = buf.getFloat();
		return ffAccessor;
	}

}
