package org.pp.objectstore.interfaces;
import static org.pp.objectstore.interfaces.Constants.FLD_MOD_SORT_KEY;

/**
 * This class contains Field meta info
 * @author prasantsmac
 *
 */
public final class FieldMetaInfo implements Cloneable {
	/**
	 * Field to be used to get/set values from Object
	 */
	private FieldAccessor fa;
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
	public FieldMetaInfo(FieldAccessor fa, String fldName, long fldCode, byte fldType, byte fldModifier) {
		this();
		this.fa = fa;
		this.fldCode = fldCode;
		this.fldName = fldName;
		this.fldType = fldType;
		this.fldModifier = fldModifier;
		this.active = true;
	}

	public FieldAccessor getFieldAccessor() {
		return fa;
	}

	public String getFldName() {
		return fldName != null ? fldName : fa.getField().getName();
	}
	
    public FieldMetaInfo setFieldName(String fName) {
    	this.fldName = fName;
    	return this;
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
		
	public FieldMetaInfo setFldCode(long fldCode) {
		this.fldCode = fldCode;
		return this;
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
	
	public boolean isKey() {
		return fldModifier >= FLD_MOD_SORT_KEY ? true : false;
	}
	
	@Override
	public FieldMetaInfo clone() {
		try {
			return new FieldMetaInfo(fa != null ? this.fa.newInstance() : null, 
									 getFldName(), this.fldCode, this.fldType, this.fldModifier);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new RuntimeException(e);
		}
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
		return "FieldName=" + getFldName() + "," +
	           "FieldCode=" + fldCode + "," +
			   "FieldType=" + fldType + "," +
	           "FieldModifier=" + fldModifier + "," +
			   "Active=" + active ;
			   
	}
}
