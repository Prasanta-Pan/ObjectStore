package org.pp.objectstore;

import static org.pp.objectstore.DataTypes.D_TYP_INT;
import static org.pp.objectstore.Util.extend;
import static org.pp.objectstore.Util.invalidType;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;

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
	 * Only constructor
	 * 
	 * @param fld
	 */
	protected IntFieldAccessor(Field fld) {
		super(fld);
	}

	// common
	private ByteBuffer put(ByteBuffer buf, int val) {
		buf = extend(buf, 5);
		buf.put(D_TYP_INT);
		buf.putInt(val);
		return buf;
	}

	@Override
	public ByteBuffer set(ByteBuffer buf, Object target) throws Exception {
		if (buf.get() != D_TYP_INT)
			invalidType(fld);
		fld.setInt(target, buf.getInt());
		return buf;
	}

	@Override
	public void set(Object target) throws Exception {
		// TODO Auto-generated method stub
		fld.setInt(target, val);
	}

	@Override
	public ByteBuffer get(ByteBuffer buf, Object target) throws Exception {
		int val = fld.getInt(target);
		return put(buf, val);
	}

	@Override
	public ByteBuffer toBytes(ByteBuffer buf, Object value) throws Exception {
		int newVal = (int) value;
		return put(buf, newVal);
	}

	@Override
	public Object get() throws Exception {
		// TODO Auto-generated method stub
		return val;
	}

	@Override
	public ByteBuffer validateType(ByteBuffer buf, Object target) throws Exception {
		if (!(target instanceof Integer))
			invalidType(fld);
		int v = (int) target;
		return put(buf, v);
	}

	@Override
	public FieldAccessor clone(ByteBuffer buf) throws Exception {
		// TODO Auto-generated method stub
		if (buf.get() != D_TYP_INT)
			invalidType(fld);
		// create a new instance
		IntFieldAccessor ifAccessor = new IntFieldAccessor(fld);
		ifAccessor.val = buf.getInt();
		return ifAccessor;
	}

}
