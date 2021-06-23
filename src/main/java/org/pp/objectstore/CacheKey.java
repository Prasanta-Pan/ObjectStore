package org.pp.objectstore;

/**
 * To be used for cache key
 * 
 * @author prasantsmac
 *
 */
public class CacheKey implements org.pp.storagengine.api.CacheEntry {
	/**
	 * Collection name
	 */
	private String cname;
	/**
	 * record id
	 */
	private Object id;

	/**
	 * Create a cache key
	 * 
	 * @param cname
	 * @param id
	 */
	public CacheKey(String cname, Object id) {
		if (cname == null || id == null)
			throw new NullPointerException("collection name and id can not be null");
		this.cname = cname;
		this.id = id;
	}

	/**
	 * Get collection name
	 * 
	 * @return
	 */
	public String getCName() {
		return cname;
	}

	/**
	 * Get object id
	 */
	public Object getId() {
		return id;
	}

	@Override
	public int hashCode() {
		int hc = cname.hashCode();
		return 31 * hc + id.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof CacheKey) {
			CacheKey anthr = (CacheKey) o;
			if (cname.equals(anthr.cname) && id.equals(anthr.id))
				return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return "CollectionName=" + cname + ",id=" + id;
	}

	@Override
	public int entrySize() {
		// TODO Auto-generated method stub
		return 0;
	}
}
