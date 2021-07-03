package org.pp.objectstore;

import static org.pp.objectstore.DataTypes.D_TYP_SHRT;
import static org.pp.objectstore.Util.extend;
import static org.pp.objectstore.Util.invalidType;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
/**
 * Short Field accessor
 * @author prasantsmac
 *
 */
final class ShortFieldAccessor extends AbstractFieldAccessor {
	/**
	 * backing short value
	 */
	private short val;

	/**
	 * Only constructor
	 * 
	 * @param fld
	 */
	protected ShortFieldAccessor(Field fld) {
		super(fld);
	}

	// common
	private ByteBuffer put(ByteBuffer buf, short val) {
		buf = extend(buf, 3);
		buf.put(D_TYP_SHRT);
		buf.putShort(val);
		return buf;
	}

	@Override
	public ByteBuffer set(ByteBuffer buf, Object target) throws Exception {
		if (buf.get() != D_TYP_SHRT)
			invalidType(fld);
		fld.setShort(target, buf.getShort());
		return buf;
	}

	@Override
	public void set(Object target) throws Exception {
		// TODO Auto-generated method stub
		fld.setShort(target, val);
	}

	@Override
	public ByteBuffer get(ByteBuffer buf, Object target) throws Exception {
		short val = fld.getShort(target);
		return put(buf, val);
	}

	@Override
	public ByteBuffer toBytes(ByteBuffer buf, Object value) throws Exception {
		short newVal = (short) value;
		return put(buf, newVal);
	}

	@Override
	public Object get() throws Exception {
		// TODO Auto-generated method stub
		return (int) val;
	}

	@Override
	public ByteBuffer validateType(ByteBuffer buf, Object target) throws Exception {
		invalidType(fld);
		return null;
	}

	@Override
	public FieldAccessor clone(ByteBuffer buf) throws Exception {
		// TODO Auto-generated method stub
		if (buf.get() != D_TYP_SHRT)
			invalidType(fld);
		// create new short field acceesor and return
		ShortFieldAccessor sfAccessor = new ShortFieldAccessor(fld);
		sfAccessor.val = buf.getShort();
		return sfAccessor;
	}

}
