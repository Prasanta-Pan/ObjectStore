package org.pp.objectstore;

import java.util.Arrays;

import org.pp.storagengine.api.CacheEntry;

/**
 * To be used for cache key
 * 
 * @author prasantsmac
 *
 */
public class CacheKey implements CacheEntry {
	/**
	 * Cache Key
	 */
	private byte[] key;
	/**
     * No default cache key instance
     */
	private CacheKey() {
		
	}
	/**
	 * Construct a cache key instance
	 * @param key
	 */
	public CacheKey(byte[] key) {
		this();
		if (key == null)
			throw new NullPointerException("Cache key can not be null");
		this.key = key;	
	}

	/**
	 * Return the key
	 * @return
	 */
	public byte[] getKey() {
		return key;
	}
	
	@Override
	public int entrySize() {
		// TODO Auto-generated method stub
		return key.length;
	}

	@Override
	public int hashCode() {
		return key.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof CacheKey) {
			CacheKey anthr = (CacheKey) o;
			if (Arrays.equals(key, anthr.key))
				return true;
		}
		return false;
	}	
}
