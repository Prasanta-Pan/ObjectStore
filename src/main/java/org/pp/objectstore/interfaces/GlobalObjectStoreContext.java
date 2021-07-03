package org.pp.objectstore.interfaces;

import org.pp.objectstore.CacheKey;
import org.pp.objectstore.CacheValue;
import org.pp.storagengine.api.KVEngine;

public interface GlobalObjectStoreContext {
    /**
     * Get KV store
     * @return
     */
	public KVEngine getKVEngine();	
	/**
	 * Global cache put.
	 * @param cname - collection name
	 * @param key - Key (String/Integer/Long)
	 * @param value  - Any value
	 */
	public CacheValue put(CacheKey key, CacheValue value);
	/**
	 * Global cache put if absent
	 * @param cname
	 * @param key
	 * @param value
	 * @return
	 */
	public CacheValue putIfAbsent(CacheKey key, CacheValue value);
	
	/**
	 * Global cache get.
	 * @param cname - collection name
	 * @param key - Key (String/Integer/Long)
	 * @return
	 */
	public CacheValue get(CacheKey key);
	/**
	 * Global cache remove
	 * @param cname
	 * @param key
	 * @return
	 */
	public CacheValue remove(CacheKey key);	
	
}
