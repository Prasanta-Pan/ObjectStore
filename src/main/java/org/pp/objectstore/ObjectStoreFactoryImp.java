package org.pp.objectstore;

import static org.pp.objectstore.Util.comp;
import static org.pp.objectstore.Util.validateClassName;

import java.lang.ref.SoftReference;
import java.net.URI;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.pp.objectstore.interfaces.GlobalObjectStoreContext;
import org.pp.objectstore.interfaces.ObjectStore;
import org.pp.storagengine.api.KVEngine;
import org.pp.storagengine.api.imp.KVEngineImp;

class ObjectStoreFactoryImp extends ObjectStoreFactory implements GlobalObjectStoreContext {
	/**
	 * Close indicator
	 */
	private boolean close = false;
	/**
	 * Key value storage
	 */
	private KVEngine kvStore = null;
	/**
	 * Object store cache
	 */
	private static ConcurrentMap<String, SoftReference<ObjectStore<?>>> cache = new ConcurrentHashMap<>();
		
	/**
	 * Create singleton ObjectStoreFactory
	 * @param uri
	 * @throws Exception 
	 */
	ObjectStoreFactoryImp(URI uri) throws Exception {
		if (uri == null)
			throw new NullPointerException("URI can not be null");
		// check protocol
		switch (uri.getScheme()) {
		case "file":
			// Database Folder
			String DB_ROOT = uri.getPath();
			// Get system properties
			Properties props = System.getProperties();
			// Create or open Database
			kvStore = new KVEngineImp(DB_ROOT, props, comp);
			break;
		case "tcp":
			throw new RuntimeException("TCP scheme/protocol currently not supported");
		default:
			throw new RuntimeException("Unsupported scheme/protocole : " + uri.getScheme());
		}	
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> ObjectStore<T> openStore(Class<T> clazz) {
		// validate class and get collection name
		String name = validateClassName(clazz);
		// reference to be return
		ObjectStore<T> os;
		// get reference 
		SoftReference<ObjectStore<?>> ref = cache.get(name);
		os = (ref != null) ? (ObjectStore<T>) ref.get() : null;
		// new instance
		if (os == null) {
			os = new ObjectStoreImp<>(name, clazz, this);
			for (;;) {
				// remove empty reference
				if (ref != null) 
                   cache.remove(name, ref);
                // put store in cache
                ref = cache.putIfAbsent(name, new SoftReference<>(os));
                if (ref == null) 
                    break;
                // check if we late
                ObjectStore<T> tmp = (ObjectStore<T>) ref.get();
                if (tmp != null) {
                	os = tmp;
                    break;
                }
			}
		}
		// return 
		return os;
	}

	@Override
	public void close() throws Exception {
		if (!close) {
			close = true;
			kvStore.close();
			cache.clear();
			cache = null;
		}		
	}

	@Override
	public KVEngine getKVEngine() {
		// TODO Auto-generated method stub
		return kvStore;
	}

	@Override
	public Object put(CacheKey key, Object value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object putIfAbsent(CacheKey key, Object value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object get(CacheKey key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object remove(CacheKey key) {
		// TODO Auto-generated method stub
		return null;
	}
}
