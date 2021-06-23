package org.pp.objectstore;

import java.net.URI;

import org.pp.objectstore.interfaces.ObjectStore;

public abstract class ObjectStoreFactory {
	/**
	 * Singleton Object factory
	 */
	private static ObjectStoreFactory osFactory = null;

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
		if (osFactory == null)
			osFactory = new ObjectStoreFactoryImp(uri);
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
