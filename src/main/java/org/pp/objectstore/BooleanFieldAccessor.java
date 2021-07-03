package org.pp.objectstore;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import static org.pp.objectstore.Util.extend;
import static org.pp.objectstore.Util.invalidType;
import static org.pp.objectstore.DataTypes.D_TYP_BOL;
/**
 * Boolean field accessor
 * @author prasantsmac
 *
 */
final class BooleanFieldAccessor extends AbstractFieldAccessor {
	/**
	 * Backing boolean field
	 */
	private boolean val = false;
    /**
     * Only constructor
     * @param fld
     */
	protected BooleanFieldAccessor(Field fld) {
		super(fld);			
	}
	// common
	private ByteBuffer put(ByteBuffer buf, boolean val) {
		buf = extend(buf, 2);
		buf.put(D_TYP_BOL);
		buf.put(val ? (byte) 1 : 0);
		return buf;
	}
	
	@Override
	public ByteBuffer set(ByteBuffer buf, Object target) throws Exception {
		if (buf.get() != D_TYP_BOL)
			invalidType(fld);
		fld.setBoolean(target, buf.get() != 0 ? true : false);
		return buf;
	}	

	@Override
	public void set(Object target) throws Exception {
		// TODO Auto-generated method stub
		fld.setBoolean(target, val);
	}

	@Override
	public ByteBuffer get(ByteBuffer buf, Object target) throws Exception {
		boolean val = fld.getBoolean(target);
		return put(buf, val);	
	}	
	
	@Override
	public Object get() throws Exception {
		// TODO Auto-generated method stub
		return val;
	}

	@Override
	public ByteBuffer toBytes(ByteBuffer buf, Object value) throws Exception {
		boolean newVal = (boolean) value;
		return put(buf, newVal);
	}

	@Override
	public ByteBuffer validateType(ByteBuffer buf, Object target) throws Exception {
		invalidType(fld);
	    return null;
	}
		
	@Override
	public FieldAccessor clone(ByteBuffer buf) throws Exception {
		if (buf.get() != D_TYP_BOL)
			invalidType(fld);
		// set boolean field
		BooleanFieldAccessor bfAccessor = new BooleanFieldAccessor(fld);
		bfAccessor.val = buf.get() != 0 ? true : false;
		return bfAccessor;
	}	

}
