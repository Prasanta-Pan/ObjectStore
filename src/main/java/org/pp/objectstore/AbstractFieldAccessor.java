package org.pp.objectstore;

import static org.pp.objectstore.DataTypes.D_TYP_BOL;
import static org.pp.objectstore.DataTypes.D_TYP_BYTE;
import static org.pp.objectstore.DataTypes.D_TYP_CHAR;
import static org.pp.objectstore.DataTypes.D_TYP_DBL;
import static org.pp.objectstore.DataTypes.D_TYP_FLT;
import static org.pp.objectstore.DataTypes.D_TYP_INT;
import static org.pp.objectstore.DataTypes.D_TYP_LNG;
import static org.pp.objectstore.DataTypes.D_TYP_SHRT;
import static org.pp.objectstore.DataTypes.D_TYP_STR;

import java.lang.reflect.Field;

/**
 * @email pan.prasanta@gmail.com
 * @author prasantsmac
 *
 */
abstract class AbstractFieldAccessor implements FieldAccessor {
	/**
	 * Field to be used for set/get of value
	 */
	protected Field fld;
	/**
     * Common constructor with associated field
     * @param data
     * @param fld
     */
	protected AbstractFieldAccessor(Field fld) {
		this.fld = fld;		
	}
	/**
	 * Return the backing field
	 */
	@Override
	public Field getField() {
		return fld;
	}
	
	@Override
	public String getName() {
		return fld.getName();
	}
	
	/**
	 * Instantiate proper field accessor of type
	 * 
	 * @param type
	 * @param fld
	 * @return
	 */
	static final FieldAccessor getFieldAccessor(byte type, Field fld) {
		switch (type) {
			case D_TYP_INT:
				return new IntFieldAccessor(fld);
			case D_TYP_LNG:
				return new LongFieldAccessor(fld);
			case D_TYP_FLT:
				return new FloatFieldAccessor(fld);
			case D_TYP_DBL:
				return new DoubleFieldAccessor(fld);
			case D_TYP_STR:
				return new StringFieldAccessor(fld);
			case D_TYP_BOL:
				return new BooleanFieldAccessor(fld);
			case D_TYP_SHRT:
				return new ShortFieldAccessor(fld);
			case D_TYP_BYTE:
				return new ByteFieldAccessor(fld);
			case D_TYP_CHAR:
				return new CharFieldAccessor(fld);
			default:
				throw new RuntimeException("Unknown type");
		}
	}

}
