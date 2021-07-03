package org.pp.objectstore;

import static org.pp.objectstore.DataTypes.D_TYP_STR;
import static org.pp.objectstore.Util.byteBufferToString;
import static org.pp.objectstore.Util.invalidType;
import static org.pp.objectstore.Util.stringToByteBuffer;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
/**
 * String Field accessor
 * @author prasantsmac
 *
 */
final class StringFieldAccessor extends AbstractFieldAccessor {
	/**
	 * Backing value could be either byte array or String
	 */
    private Object val;
    /**
     * Only constructor
     * @param fld
     */
	protected StringFieldAccessor(Field fld) {
		super(fld);			
	}
		
	@Override
	public ByteBuffer set(ByteBuffer buf, Object target) throws Exception {
		String str = byteBufferToString(buf, fld);
		fld.set(target, str);
		return buf;
	}
	/**
	 * check if value still byte array if not
	 * @return
	 */
	private Object byteArrayToString() {
		if (val instanceof byte[]) {
			byte[] arrVal = (byte[]) val;
			// extract all characters from byte array
			val = byteBufferToString(ByteBuffer.wrap(arrVal), null);;
		}
		return val;
	}
	
	@Override
	public void set(Object target) throws Exception {
		// set field value now
		fld.set(target, byteArrayToString());
	}

	@Override
	public ByteBuffer get(ByteBuffer buf, Object target) throws Exception {
		String str = (String) fld.get(target);
		return stringToByteBuffer(str == null ? "" : str, buf);
	}

	@Override
	public ByteBuffer toBytes(ByteBuffer buf, Object value) throws Exception {
		String str = (String) value;
		return stringToByteBuffer(str == null ? "" : str, buf);
	}
	
	@Override
	public Object get() throws Exception {
		// TODO Auto-generated method stub
		return byteArrayToString();
	}

	@Override
	public ByteBuffer validateType(ByteBuffer buf, Object target) throws Exception {
		if (!(target instanceof String))
			invalidType(fld);
		String v = (String) target;
		return stringToByteBuffer(v == null ? "" : v, buf);
	}

	@Override
	public FieldAccessor clone(ByteBuffer buf) throws Exception {
		// check data validity
		if (buf.get() != D_TYP_STR)
			invalidType(fld);
		// get length of the string
		int len = buf.getInt();
		// rewind
		buf.position(buf.position() - 5);
		// extract bytes
		byte[] data = new byte[len * 2 + 5];
		buf.get(data);
		// instantiate a new StringAccessor
		StringFieldAccessor strAccessor = new StringFieldAccessor(fld);
		strAccessor.val = data;		
		return strAccessor;
	}	

}
