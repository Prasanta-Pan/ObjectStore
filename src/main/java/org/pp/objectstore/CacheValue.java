package org.pp.objectstore;

import org.pp.storagengine.api.CacheEntry;
/**
 * Cache value
 * @author prasantsmac
 *
 */
public class CacheValue implements CacheEntry {
    /**
	 * Actual object data bytes
	 */
	private byte[] value;
	/**
	 * No default constructor
	 */
	private CacheValue() {
		
	}
	
	/**
	 * Value along with its size
	 * @param val
	 * @param sz
	 */
	public CacheValue(byte[] val) {
		this();
		if (val == null)
			throw new NullPointerException("Value can not be null");
		this.value = val;
	}
	
	@Override
	public int entrySize() {
		// TODO Auto-generated method stub
		return value.length;
	}
	
	/**
	 * Return containing value
	 * @return
	 */
	public byte[] getValue() {
		return value;
	}	

}
