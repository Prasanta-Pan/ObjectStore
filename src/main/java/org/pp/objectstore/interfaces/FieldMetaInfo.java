package org.pp.objectstore.interfaces;

import java.lang.reflect.Field;
/**
 * This class contains Field meta info
 * @author prasantsmac
 *
 */
public final class FieldMetaInfo {
	/**
	 * Field to be used to get/set values from Object
	 */
	private Field fld;
	/**
	 * Field name
	 */
	private String fldName;
	/**
	 * Field assigned code
	 */
	private long fldCode;
	/**
	 * Field data type
	 */
	private byte fldType;
	/**
	 * Field modifier (SortKey,ID, version...)
	 */
	private byte fldModifier;
	/**
	 * If this field removed already
	 */
    private boolean active;
    
	private FieldMetaInfo() {
	}

	/**
	 * Only constructor to create FieldMetaObject
	 * 
	 * @param fld
	 * @param fldName
	 * @param fldType
	 * @param fldModifier
	 */
	public FieldMetaInfo(Field fld, String fldName, long fldCode, byte fldType, byte fldModifier) {
		this();
		this.fld = fld;
		this.fldCode = fldCode;
		this.fldName = fldName;
		this.fldType = fldType;
		this.fldModifier = fldModifier;
		this.active = true;
	}

	public Field getField() {
		return fld;
	}

	public String getFldName() {
		return fld != null ? fld.getName() : fldName;
	}

	public long getFldCode() {
		return fldCode;
	}

	public byte getFldType() {
		return fldType;
	}

	public byte getFldModifier() {
		return fldModifier;
	}
		
	public void setFldCode(long fldCode) {
		this.fldCode = fldCode;
	}

	public void setFldModifier(byte fldModifier) {
		this.fldModifier = fldModifier;
	}	
	
	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	@Override
	public boolean equals(Object othr) {
		if (othr instanceof FieldMetaInfo) {
			FieldMetaInfo oMInfo = (FieldMetaInfo) othr;
			if (getFldName().equals(oMInfo.getFldName()) && 
				fldCode == oMInfo.fldCode && fldType == oMInfo.fldType && 
				fldModifier == oMInfo.fldModifier) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return "FieldName=" + fldName + "," +
	           "FieldCode=" + fldCode + "," +
			   "FieldType=" + fldType + "," +
	           "FieldModifier=" + fldModifier + "," +
			   "Active=" + active ;
			   
	}
}
