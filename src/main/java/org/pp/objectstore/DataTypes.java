package org.pp.objectstore;

import java.util.HashMap;
import java.util.Map;

/**
 * Define data types and some important constants supported by object store
 * @author prasantsmac 
 */
final class DataTypes {
    /**
     * Should remain as singleton
     */
	private DataTypes() {}	
	/**
	 * Data type integer
	 */
	static final byte D_TYP_INT = 0;
	/**
	 * Data type long
	 */
	static final byte D_TYP_LNG = 1;
	/**
	 * Data type string
	 */
	static final byte D_TYP_STR = 2;
	/**
	 * Data type float
	 */
	static final byte D_TYP_FLT = 3;
	/**
	 * Data type double
	 */
	static final byte D_TYP_DBL = 4;
	/**
	 * Data type boolean
	 */
	static final byte D_TYP_BOL = 5;
	/**
	 * Data type short
	 */
	static final byte D_TYP_SHRT = 6;
	/**
	 * Data type byte
	 */
	static final byte D_TYP_BYTE = 7;
	/**
	 * Data type character
	 */
	static final byte D_TYP_CHAR = 8;
	/**
	 * Max key length supported
	 */
	static final int MAX_KEY_SZ = 128;
	/**
	 * Max sort key allowed
	 */
	static final int MAX_SORT_KEY = 5;	
	
	/**
	 * Field Accessor
	 */
	static final Map<String, Byte> supportedTypes = new HashMap<>();	
	/**
	 * Initialise supported types
	 */
	static {
		//
		supportedTypes.put("int", D_TYP_INT);
		supportedTypes.put("long", D_TYP_LNG);
		supportedTypes.put("float", D_TYP_FLT);
		supportedTypes.put("double", D_TYP_DBL);
		supportedTypes.put("short", D_TYP_SHRT);
		supportedTypes.put("byte", D_TYP_BYTE);
		supportedTypes.put("String", D_TYP_STR);
		supportedTypes.put("char", D_TYP_CHAR);
		supportedTypes.put("boolean", D_TYP_BOL);
		// some other non primitive types can be added in future
	}
	
}
