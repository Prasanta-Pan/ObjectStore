package org.pp.objectstore.interfaces;

import static org.pp.storagengine.api.imp.Util.MB;

public final class Constants {
    /**
     * Data types supported by Object store
     */
	public static final byte D_TYP_INT 	
	                         	= 0; // int type
	public static final byte D_TYP_LNG 	
								= 1; // long type
	public static final byte D_TYP_STR 	
								= 2; // String type
	public static final byte D_TYP_FLT 	
								= 3; // Float type
	public static final byte D_TYP_DBL 	
								= 4; // Double type
	public static final byte D_TYP_BOL	
								= 5; // Boolean type
	public static final byte D_TYP_SHRT 
								= 6; // short type
	public static final byte D_TYP_BYTE 
								= 7; // byte type
	public static final byte D_TYP_CHAR 
								= 8; // char type
	
	/**
	 * Field modifier type (SortKey, ID, ....)
	 */
	public static final byte FLD_MOD_GEN 		
								= -128; // Field with no modifier
	public static final byte FLD_MOD_SORT_KEY 	
								= -127; // SortKey Field
	public static final byte FLD_MOD_VERSION 	
								= -126; // Version field
	public static final byte FLD_MOD_ID 		
								= -125; // ID field
	
	/**
	 * Constants related to Key Space
	 */
	public static final int MAX_KEY_SIZE 
								= 128; 		// maximum key size
	public static final int MIN_BUF_SIZE
								= 32;		// minimum byte buffer allocation size
	public static final int MAX_SORT_KEY 
								= 5; 		// maximum sort keys allowed
	public static final byte META_SPACE 	
								= -128; 	// meta space 
	public static final byte DATA_SPACE
								= 127; 		// Data space
	public static final byte STORE_NAMES
								= -128; 	// Key space where store names are found [META-SPACE][STORE_NAMES]...
	public static final byte STORE_META
								= -127; 	// key space where store meta info could be found [META-SPACE][STORE_META]...
	public static final int STORE_META_LEN
								= 13; 		// [[META_SPACE][STORE_META][STORE-CODE][FIELD-NAME]=[FIELD-CODE][FIELD-TYPE][MODIFIER]
	public static final int STORE_DATA_LEN
								= 11;		// [DATA_SPACE][STORE-CODE]...
	public static final int STORE_NAMES_META_LEN
								= 4;		// Store names meta length [META-SPACE][STORE_NAMES]...
	
	/**
	 * Constants related to Key Store status
	 */
	
	public static final byte STORE_STATUS_ACTIVE 
								= -128;		// Collection is active and accessible
	public static final byte STORE_STATUS_MIGRATION
								= -127; 	// Collection migration is on progress
	public static final byte STORE_STATUS_RENAME
								= -126;		// Collection fields rename is on progress
	public static final byte STORE_STATUS_PURGE
								= -125;
		
	/**
	 * Default cache size is 32MB
	 */
	public static final long defaultCacheSize 
								= 32 * MB;
		
}
	