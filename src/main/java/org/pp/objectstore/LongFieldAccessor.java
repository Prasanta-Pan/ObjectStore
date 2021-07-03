package org.pp.objectstore;

import static org.pp.objectstore.DataTypes.D_TYP_LNG;
import static org.pp.objectstore.Util.extend;
import static org.pp.objectstore.Util.invalidType;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;

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
	 * Only constructor
	 * 
	 * @param fld
	 */
	protected LongFieldAccessor(Field fld) {
		super(fld);
	}

	// common
	private ByteBuffer put(ByteBuffer buf, long val) {
		buf = extend(buf, 9);
		buf.put(D_TYP_LNG);
		buf.putLong(val);
		return buf;
	}

	@Override
	public ByteBuffer set(ByteBuffer buf, Object target) throws Exception {
		if (buf.get() != D_TYP_LNG)
			invalidType(fld);
		fld.setLong(target, buf.getLong());
		return buf;
	}

	@Override
	public void set(Object target) throws Exception {
		// TODO Auto-generated method stub
		fld.setLong(target, val);
	}

	@Override
	public ByteBuffer get(ByteBuffer buf, Object target) throws Exception {
		long val = fld.getLong(target);
		return put(buf, val);
	}

	@Override
	public ByteBuffer toBytes(ByteBuffer buf, Object value) throws Exception {
		long newVal = (long) value;
		return put(buf, newVal);
	}

	@Override
	public Object get() throws Exception {
		// TODO Auto-generated method stub
		return val;
	}

	@Override
	public ByteBuffer validateType(ByteBuffer buf, Object target) throws Exception {
		if (!(target instanceof Long))
			invalidType(fld);
		long v = (long) target;
		return put(buf, v);
	}

	@Override
	public FieldAccessor clone(ByteBuffer buf) throws Exception {
		// TODO Auto-generated method stub
		if (buf.get() != D_TYP_LNG)
			invalidType(fld);
		// create a new instance
		LongFieldAccessor lfAccessor = new LongFieldAccessor(fld);
		lfAccessor.val = buf.getLong();
		return lfAccessor;
	}

}
