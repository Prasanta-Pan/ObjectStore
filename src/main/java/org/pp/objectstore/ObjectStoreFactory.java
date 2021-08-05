package org.pp.objectstore;

import static org.pp.objectstore.interfaces.Constants.defaultCacheSize;

import java.net.URI;

import org.pp.objectstore.interfaces.FieldRenameHandler;
import org.pp.objectstore.interfaces.ObjectStore;
import org.pp.objectstore.interfaces.PurgeStoreHandler;
import org.pp.objectstore.interfaces.StoreDataHandler;

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
	public abstract <T> ObjectStore<T> openStore(Class<T> clazz) throws Exception;
	/**
	 * Migrate Store. Potentially expensive
	 * @param <T>
	 * @param clazz
	 * @param hdlr
	 */
	public abstract <T> void migrateStore(Class<T> clazz, StoreDataHandler<T> hdlr) throws Exception ;
	/**
	 * Rename field of existing store
	 * @param <T>
	 * @param clazz
	 * @param exField
	 * @param newField
	 */
	public abstract <T> void renameStoreFields(Class<T> clazz, FieldRenameHandler fh) throws Exception ;	
	/**
	 * Purge store
	 * @param <T>
	 * @param clazz
	 * @param hdlr
	 * @throws Exception
	 */
	public abstract <T> void purgeStore(Class<T> clazz, PurgeStoreHandler hdlr) throws Exception;	
   	/**
	 * Close Object store factory
	 * @throws Exception
	 */
	public abstract void close() throws Exception;	
	/**
	 * Return true if factory closed already false otherwise
	 * @return
	 */
	public abstract boolean isClosed();	
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
		if (osFactory == null || osFactory.isClosed())
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
		return (osFactory != null && !osFactory.isClosed()) ? osFactory : null;
	}

}
