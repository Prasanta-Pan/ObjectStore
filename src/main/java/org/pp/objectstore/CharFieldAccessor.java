package org.pp.objectstore;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import static org.pp.objectstore.Util.invalidType;
import static org.pp.objectstore.Util.extend;
import static org.pp.objectstore.DataTypes.D_TYP_CHAR;
/**
 * Char Field accessor
 * @author prasantsmac
 *
 */
final class CharFieldAccessor extends AbstractFieldAccessor {
	/**
	 * Backing char value
	 */
    private char val;
    /**
     * Only constructor with associated Filed
     * @param fld
     */
	protected CharFieldAccessor(Field fld) {
		super(fld);			
	}
	
	// common
	private ByteBuffer put(ByteBuffer buf, char val) {
		buf = extend(buf, 3);
		buf.put(D_TYP_CHAR);
		buf.putChar(val);
		return buf;
	}
	
	@Override
	public ByteBuffer set(ByteBuffer buf, Object target) throws Exception {
		if (buf.get() != D_TYP_CHAR)
			invalidType(fld);
		fld.setChar(target, buf.getChar());
		return buf;
	}
	
	@Override
	public void set(Object target) throws Exception {
		// TODO Auto-generated method stub
		fld.setChar(target, val);
	}

	@Override
	public ByteBuffer get(ByteBuffer buf, Object target) throws Exception {
		char val = fld.getChar(target);
		return put(buf, val);	
	}

	@Override
	public ByteBuffer toBytes(ByteBuffer buf, Object value) throws Exception {
		char val = (char) value;
		return put(buf, val);	
	}
	
	@Override
	public Object get() throws Exception {
		// TODO Auto-generated method stub
		return val + "";
	}

	@Override
	public ByteBuffer validateType(ByteBuffer buf, Object target) throws Exception {
		invalidType(fld);
	    return null;
	}
	
	@Override
	public FieldAccessor clone(ByteBuffer buf) throws Exception {
		// TODO Auto-generated method stub
		if (buf.get() != D_TYP_CHAR)
			invalidType(fld);
		// create an instance set value and return
		CharFieldAccessor charFieldAccessor = new CharFieldAccessor(fld);
		charFieldAccessor.val = buf.getChar();
		return charFieldAccessor;
	}	

}
