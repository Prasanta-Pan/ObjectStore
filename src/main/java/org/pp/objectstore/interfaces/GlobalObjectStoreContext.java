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
	/**
	 * Lock a key
	 * @param key
	 */
	public void lock(Object key) throws Exception;
	/**
	 * Unlock the same. Don't forget to unlock
	 * @param key
	 */
	public void unlock(Object key);
	/**
	 * Build key
	 * @param objs
	 * @return
	 */
	public byte[] buildKey(Object...objs) throws Exception;
	/**
	 * Compressed bytes 
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public byte[] compress(byte[] data) throws Exception;
	/**
	 * Uncompressed the compressed bytes
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public byte[] uncompress(byte[] data) throws Exception; 
	/**
	 * Get Field accessor byte type code
	 * @param typeCode
	 * @return
	 */
	public FieldAccessor getFieldAccessor(byte typeCode);
	/**
	 * Get field accessor by instance type
	 * @param val
	 * @return
	 */
	public FieldAccessor getFieldAccessor(Object val);
	/**
	 * If type name is supported
	 * @param typeName
	 * @return
	 */
	public boolean isSupportedType(String typeName);
	/**
	 * Get type code 
	 * @param typeName
	 * @return
	 */
	public byte getTypeCode(String typeName);
	/**
	 * Validate class name
	 * @param clazz
	 * @return
	 */
	public String validateClassName(Class<?> clazz);
	/**
	 * Generate unique long id
	 * @return
	 */
	public long genId();
}
