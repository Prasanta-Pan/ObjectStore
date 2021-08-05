package org.pp.objectstore.interfaces;

public final class StoreMetaInfo implements Cloneable {
	/**
	 * Store name
	 */
	private String storeName;
	/**
	 * Assigned store code
	 */
	private long storeCode;
	/**
	 * New store code if store migration was in progress
	 */
	private long newStoreCode;
	/**
	 * Store status
	 */
	private byte storeStatus;
	/**
	 * Associated class
	 */
    private Class<?> clazz;
    
    
	private StoreMetaInfo() {
	}

	/**
	 * Only constructor
	 * 
	 * @param storeName
	 * @param fullyQualifiedClassName
	 * @param storeCode
	 * @param newStoreCode
	 * @param storeStatus
	 */
	public StoreMetaInfo(String storeName, Class<?> clazz, 
			long storeCode, long newStoreCode, byte storeStatus) {
		this.newStoreCode = newStoreCode;
		this.storeCode = storeCode;
		this.storeName = storeName;
		this.storeStatus = storeStatus;
		this.clazz = clazz;
	}

	public String getStoreName() {
		return storeName;
	}

	public String getClassName() {
		return clazz.getName();
	}

	public long getStoreCode() {
		return storeCode;
	}

	public long getNewStoreCode() {
		return newStoreCode;
	}

	public byte getStoreStatus() {
		return storeStatus;
	}

	public StoreMetaInfo setNewStoreCode(long newStoreCode) {
		this.newStoreCode = newStoreCode;
		return this;
	}

	public StoreMetaInfo setStoreStatus(byte storeStatus) {
		this.storeStatus = storeStatus;
		return this;
	}
	
	public void setStoreCode(long storeCode) {
		this.storeCode = storeCode;
	}	

	public Class<?> getClazz() {
		return clazz;
	}

	@Override
	public StoreMetaInfo clone() {
		StoreMetaInfo cloned = new StoreMetaInfo();
		cloned.newStoreCode = newStoreCode;
		cloned.storeCode = storeCode;
		cloned.storeName = storeName;
		cloned.storeStatus = storeStatus;
		cloned.clazz = clazz;
		return cloned;
	}

	@Override
	public String toString() {
		return "StoreName=" + storeName + "," + "ClassName=" + clazz.getName() + ","
				+ "StoreCode=" + storeCode + "," + "NewStoreCode=" + newStoreCode + "," + "StoreStatus=" + storeStatus;
	}

}
