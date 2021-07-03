package org.pp.objectstore;

import static org.pp.objectstore.DataTypes.D_TYP_BYTE;
import static org.pp.objectstore.Util.extend;
import static org.pp.objectstore.Util.invalidType;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;

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
	 * Only constructor
	 * 
	 * @param fld
	 */
	protected ByteFieldAccessor(Field fld) {
		super(fld);
	}

	// common
	private ByteBuffer put(ByteBuffer buf, byte val) {
		buf = extend(buf, 2);
		buf.put(D_TYP_BYTE);
		buf.put(val);
		return buf;
	}

	@Override
	public ByteBuffer set(ByteBuffer buf, Object target) throws Exception {
		if (buf.get() != D_TYP_BYTE)
			invalidType(fld);
		fld.setByte(target, buf.get());
		return buf;
	}

	@Override
	public void set(Object target) throws Exception {
		// TODO Auto-generated method stub
		fld.setByte(target, val);
	}

	@Override
	public ByteBuffer get(ByteBuffer buf, Object target) throws Exception {
		byte val = fld.getByte(target);
		return put(buf, val);
	}

	@Override
	public ByteBuffer toBytes(ByteBuffer buf, Object value) throws Exception {
		byte newVal = (byte) value;
		return put(buf, newVal);
	}

	@Override
	public ByteBuffer validateType(ByteBuffer buf, Object target) throws Exception {
		invalidType(fld);
		return null;
	}

	@Override
	public Object get() throws Exception {
		// TODO Auto-generated method stub
		return (int) val;
	}

	@Override
	public FieldAccessor clone(ByteBuffer buf) throws Exception {
		// TODO Auto-generated method stub
		if (buf.get() != D_TYP_BYTE)
			invalidType(fld);
		// create a new byte field accessor instance
		ByteFieldAccessor bfAccessor = new ByteFieldAccessor(fld);
		bfAccessor.val = buf.get();
		return bfAccessor;
	}

}
