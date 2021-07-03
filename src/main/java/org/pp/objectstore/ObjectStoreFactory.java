package org.pp.objectstore;

import java.net.URI;

import org.pp.objectstore.interfaces.ObjectStore;
import static org.pp.storagengine.api.imp.Util.MB;

public abstract class ObjectStoreFactory {
	/**
	 * Singleton Object factory
	 */
	private static ObjectStoreFactory osFactory = null;
	/**
	 * Default cache size is 32MB
	 */
	static final long defaultCacheSize = 32 * MB;

	/**
	 * Open Object store of class T
	 * 
	 * @param <T>
	 * @param clazz
	 * @return
	 */
	public abstract <T> ObjectStore<T> openStore(Class<T> clazz);
   	/**
	 * Close Object store factory
	 * @throws Exception
	 */
	public abstract void close() throws Exception;
	/**
	 * Open ObjectStore factory if not open yet with support for object store caching
	 * @param uri
	 * @param enableCache
	 * @return
	 * @throws Exception
	 */
	public static final synchronized ObjectStoreFactory open(URI uri) throws Exception {
		return open(uri, defaultCacheSize);
	}
	
	/**
	 * Open ObjectStore factory if not open yet with support for object store caching
	 * @param uri
	 * @param cacheSize
	 * @return
	 * @throws Exception
	 */
	public static final synchronized ObjectStoreFactory open(URI uri, long cacheSize) throws Exception {
		if (osFactory == null)
			osFactory = new ObjectStoreFactoryImp(uri, cacheSize);
		//
		return osFactory;
	}

	/**
	 * Return current object factory
	 * 
	 * @return
	 */
	public static final ObjectStoreFactory current() {
		return osFactory;
	}

}
