package org.pp.objectstore;

import static org.pp.objectstore.ByteFieldAccessor.parseByte;
import static org.pp.objectstore.ByteFieldAccessor.serialise;
import static org.pp.objectstore.LongFieldAccessor.parseLong;
import static org.pp.objectstore.LongFieldAccessor.serialise;
import static org.pp.objectstore.StringFieldAccessor.parseString;
import static org.pp.objectstore.StringFieldAccessor.serialise;
import static org.pp.objectstore.Util.extract;
import static org.pp.objectstore.interfaces.Constants.D_TYP_BOL;
import static org.pp.objectstore.interfaces.Constants.D_TYP_BYTE;
import static org.pp.objectstore.interfaces.Constants.D_TYP_CHAR;
import static org.pp.objectstore.interfaces.Constants.D_TYP_DBL;
import static org.pp.objectstore.interfaces.Constants.D_TYP_FLT;
import static org.pp.objectstore.interfaces.Constants.D_TYP_INT;
import static org.pp.objectstore.interfaces.Constants.D_TYP_LNG;
import static org.pp.objectstore.interfaces.Constants.D_TYP_SHRT;
import static org.pp.objectstore.interfaces.Constants.D_TYP_STR;
import static org.pp.objectstore.interfaces.Constants.MAX_KEY_SIZE;
import static org.pp.objectstore.interfaces.Constants.META_SPACE;
import static org.pp.objectstore.interfaces.Constants.MIN_BUF_SIZE;
import static org.pp.objectstore.interfaces.Constants.STORE_NAMES;
import static org.pp.objectstore.interfaces.Constants.STORE_NAMES_META_LEN;
import static org.pp.objectstore.interfaces.Constants.STORE_STATUS_ACTIVE;
import static org.pp.objectstore.interfaces.Constants.STORE_STATUS_MIGRATION;
import static org.pp.objectstore.interfaces.Constants.STORE_STATUS_PURGE;
import static org.pp.objectstore.interfaces.Constants.STORE_STATUS_RENAME;
import static org.pp.objectstore.interfaces.Constants.defaultCacheSize;
import static org.pp.storagengine.api.imp.Util.MB;

import java.lang.ref.SoftReference;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.pp.objectstore.interfaces.Collection;
import org.pp.objectstore.interfaces.FieldAccessor;
import org.pp.objectstore.interfaces.FieldRenameHandler;
import org.pp.objectstore.interfaces.GlobalObjectStoreContext;
import org.pp.objectstore.interfaces.ObjectStore;
import org.pp.objectstore.interfaces.PurgeStoreHandler;
import org.pp.objectstore.interfaces.StoreDataHandler;
import org.pp.objectstore.interfaces.StoreMetaInfo;
import org.pp.storagengine.api.KVEngine;
import org.pp.storagengine.api.KVEntry;
import org.pp.storagengine.api.imp.KVEngineImp;
import org.pp.storagengine.api.imp.KeyLocker;
import org.pp.storagengine.api.imp.LRUCache;
import org.xerial.snappy.Snappy;

class ObjectStoreFactoryImp extends ObjectStoreFactory implements GlobalObjectStoreContext {
	/**
	 * Key value storage
	 */
	private KVEngine kvStore = null;
	/**
	 * For atomic updates of objects
	 */
	private KeyLocker<Object> kLocker = new KeyLocker<>(64);
	/**
	 * LRU cache for object caching
	 */
	private static LRUCache<CacheKey, CacheValue> objCache;
	/**
	 * Object store cache
	 */
	private static ConcurrentMap<String, SoftReference<ObjectStoreImp<?>>> cache = new ConcurrentHashMap<>();
	/**
	 * current sequence number
	 */
	private static int SEQ = -1;
	/**
	 * Max sequence number supported (12 bit)
	 */
	private static final int MAX_SEQ = 4095;
	/**
	 * Current time in milliseconds
	 */
	private static long curTime = System.currentTimeMillis();
	/**
	 * Supported types
	 */
	static final Map<String, Byte> supportedTypes = new HashMap<>();
	/**
	 * Field accessors
	 */
	static final FieldAccessor[] accessors = new FieldAccessor[9];
	/**
	 * Initialise supported types
	 */
	static {
		// some other non primitive types can be added in future
		supportedTypes.put("int", D_TYP_INT);
		supportedTypes.put("long", D_TYP_LNG);
		supportedTypes.put("float", D_TYP_FLT);
		supportedTypes.put("double", D_TYP_DBL);
		supportedTypes.put("short", D_TYP_SHRT);
		supportedTypes.put("byte", D_TYP_BYTE);
		supportedTypes.put("String", D_TYP_STR);
		supportedTypes.put("char", D_TYP_CHAR);
		supportedTypes.put("boolean", D_TYP_BOL);

		// initialise field accessors
		accessors[D_TYP_INT] = new IntFieldAccessor();
		accessors[D_TYP_LNG] = new LongFieldAccessor();
		accessors[D_TYP_FLT] = new FloatFieldAccessor();
		accessors[D_TYP_DBL] = new DoubleFieldAccessor();
		accessors[D_TYP_SHRT] = new ShortFieldAccessor();
		accessors[D_TYP_BYTE] = new ByteFieldAccessor();
		accessors[D_TYP_STR] = new ShortFieldAccessor();
		accessors[D_TYP_CHAR] = new CharFieldAccessor();
		accessors[D_TYP_BOL] = new BooleanFieldAccessor();
	}

	/**
	 * Create singleton ObjectStoreFactory
	 * 
	 * @param uri
	 * @throws Exception
	 */
	ObjectStoreFactoryImp(URI uri, long cacheSize) throws Exception {
		if (uri == null)
			throw new NullPointerException("URI can not be null");
		// check valid cache size
		if (cacheSize < defaultCacheSize)
			throw new RuntimeException("Minimum cache size should be " + (defaultCacheSize / MB) + "MB");
		// check protocol
		switch (uri.getScheme()) {
			case "file":
				// Database Folder
				String DB_ROOT = uri.getPath();
				// Get system properties
				Properties props = System.getProperties();
				// Create or open Database
				kvStore = new KVEngineImp(DB_ROOT, props, new ObjectStoreComparator());
				break;
			case "tcp":
				throw new RuntimeException("TCP scheme/protocol currently not supported");
			default:
				throw new RuntimeException("Unsupported scheme/protocole : " + uri.getScheme());
		}
		// initialise cache
		objCache = new LRUCache<>(MB, cacheSize);
	}
    /**
     * Migrate store 
     */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public <T> void migrateStore(Class<T> clazz, StoreDataHandler<T> hdlr) throws Exception {
		// validate class and get collection name
		String name = validateClassName(clazz);
		// store handler must not be null
		if (hdlr == null)
			throw new NullPointerException("Store Handler can not be null");
		// lock the store
		lock(name);
		try {
			// check if object store instance available in cache or not
			SoftReference<ObjectStoreImp<?>> ref = cache.get(name);
			// get reference
			ObjectStoreImp osCx = (ref != null) ? ref.get() : null;
			//
			if (osCx != null)
				throw new RuntimeException("Can not redefine sore when store already in use");
			// build store key
			byte[] storeKey = buildKeyFindStore(name);
			// load store meta info
			StoreMetaInfo smInfo = loadStoreMeta(storeKey, clazz);
			// if store don't exist
			if (smInfo == null)
				throw new RuntimeException("Object store '" + name + "' doesn't exist");
			// check if store status is valid
			if (smInfo.getStoreStatus() != STORE_STATUS_ACTIVE && smInfo.getStoreStatus() != STORE_STATUS_MIGRATION)
				checkStatus(smInfo);
			// set store status to migration
			if (smInfo.getStoreStatus() == STORE_STATUS_ACTIVE) {
				// create a new store code
				smInfo.setNewStoreCode(genId());
				// set status migration in progress
				smInfo.setStoreStatus(STORE_STATUS_MIGRATION);
				// persist state
				createOrUpdateStore(storeKey, smInfo);
			}
			// make clone of new store meta
			StoreMetaInfo newSmInfo = smInfo.clone();
			// set new store code to store code
			newSmInfo.setStoreCode(smInfo.getNewStoreCode());
			// create a new store
			ObjectStoreImp newStore = new ObjectStoreImp(newSmInfo, this, null);
			// now instantiate old store context
			ObjectStoreImp oldStore = new ObjectStoreImp(smInfo, this);
			// migrate store
			ObjectStoreImp.migrate(newStore, oldStore, hdlr);
			// purge old store
			ObjectStoreImp.purgeStore(oldStore, null);
			// set new store code
			smInfo.setStoreCode(smInfo.getNewStoreCode());
			// set store status to active
			smInfo.setStoreStatus(STORE_STATUS_ACTIVE);
			// persist status
			createOrUpdateStore(storeKey, smInfo);
		} finally {
			unlock(name);
		}
	}
    /**
     * To rename store fields
     */
	@Override
	public <T> void renameStoreFields(Class<T> clazz, FieldRenameHandler fh) throws Exception {
		// validate class and get collection name
		String name = validateClassName(clazz);
		// validate handler, handler can not be null
		if (fh == null)
			throw new NullPointerException("FieldRenameHandler can not be null");
		// lock the store
		lock(name);
		try {
			// get object store reference
			SoftReference<ObjectStoreImp<?>> ref = cache.get(name);
			// get reference
			ObjectStoreImp<?> osCtx = (ref != null) ? ref.get() : null;
			//
			if (osCtx != null)
				throw new RuntimeException("Can not rename fields when store is in use");
			// create store key
			byte[] storeKey = buildKeyFindStore(name);
			// load store meta info
			StoreMetaInfo smInfo = loadStoreMeta(storeKey, clazz);
			// if store don't exist
			if (smInfo == null)
				throw new RuntimeException("Object store '" + name + "' doesn't exist");
			// check if store status is valid
			if (smInfo.getStoreStatus() != STORE_STATUS_ACTIVE && smInfo.getStoreStatus() != STORE_STATUS_RENAME)
				checkStatus(smInfo);
			// set store status to field rename
			if (smInfo.getStoreStatus() == STORE_STATUS_ACTIVE) {
				// field rename status
				smInfo.setStoreStatus(STORE_STATUS_RENAME);
				// persist state
				createOrUpdateStore(storeKey, smInfo);
			}
			// instantiate store and rename fields
			new ObjectStoreImp<>(smInfo, this, fh);
			// make store active
			createOrUpdateStore(storeKey, smInfo.setStoreStatus(STORE_STATUS_ACTIVE));
		} finally {
			unlock(name);
		}
	}

	/**
	 * Purge existing source
	 */
	public <T> void purgeStore(Class<T> clazz, PurgeStoreHandler hdlr) throws Exception {
		// validate class and get collection name
		String name = validateClassName(clazz);
		// lock the store
		lock(name);
		try {
			// get object store reference
			SoftReference<ObjectStoreImp<?>> ref = cache.get(name);
			// get reference
			ObjectStoreImp<?> osCtx = (ref != null) ? ref.get() : null;
			//
			if (osCtx != null)
				throw new RuntimeException("Can not purge a store when the store in use");
			// create store key
			byte[] storeKey = buildKeyFindStore(name);
			// load store meta info
			StoreMetaInfo smInfo = loadStoreMeta(storeKey, clazz);
			// if store don't exist
			if (smInfo == null)
				throw new RuntimeException("Object store '" + name + "' doesn't exist");
			// check if store status is valid
			if (smInfo.getStoreStatus() != STORE_STATUS_ACTIVE && smInfo.getStoreStatus() != STORE_STATUS_PURGE)
				checkStatus(smInfo);
			// update store status to purging in progress
			if (smInfo.getStoreStatus() == STORE_STATUS_ACTIVE) {
				// set status purge in progress
				smInfo.setStoreStatus(STORE_STATUS_PURGE);
				// persist state
				createOrUpdateStore(storeKey, smInfo);
			}
			// instantiate store context
			osCtx = new ObjectStoreImp<>(smInfo, this);
			// purge store now
			ObjectStoreImp.purgeStore(osCtx, hdlr);
			// delete store
			getKVEngine().delete(storeKey);
			// make final sync
			getKVEngine().sync();
		} finally {
			unlock(name);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> ObjectStore<T> openStore(Class<T> clazz) throws Exception {
		// validate class and get collection name
		String name = validateClassName(clazz);
		// object store context
		ObjectStoreImp<?> osCtx;
		// lock store name
		lock(name);
		try {
			// get object store reference
			SoftReference<ObjectStoreImp<?>> ref = cache.get(name);
			osCtx = (ref != null) ? ref.get() : null;
			// create new instance
			if (osCtx == null) {
				// build find store key
				byte[] osKey = buildKeyFindStore(name);
				// create or load store
				StoreMetaInfo smInfo = loadStoreMeta(osKey, clazz);
				// check if the store is accessible
				checkStatus(smInfo);
				// store doesn't exist, create a new one
				if (smInfo == null) {
					// generate store code
					long storeCode = genId();
					// create new store meta info object
					smInfo = new StoreMetaInfo(name, clazz, storeCode, 0, STORE_STATUS_ACTIVE);
					// persist store info
					createOrUpdateStore(osKey, smInfo);
				}
				// create new Object Store context
				osCtx = new ObjectStoreImp<>(smInfo, this, null);
				// remove empty reference
				if (ref != null)
					cache.remove(name, ref);
				// put store in cache
				cache.put(name, new SoftReference<>(osCtx));
			}

		} finally {
			unlock(name);
		}
		//
		return (ObjectStore<T>) osCtx;
	}

	/**
	 * Validate store name length and build key
	 * [META_SPACE][STORE_NAMES][STORE-NAME]=[long store code][Fully Qualified
	 * ClassName][status][New Store Code]?
	 * 
	 * @param cname
	 * @return
	 */
	private byte[] buildKeyFindStore(String cname) throws Exception {
		// validate store name length
		int mLen = (MAX_KEY_SIZE - (STORE_NAMES_META_LEN + 5)) / 2;
		if (cname.length() > mLen)
			throw new RuntimeException("Max character allowed for store name is " + mLen);
		// create key
		return buildKey(META_SPACE, STORE_NAMES, cname);
	}
	/**
	 * Check store status
	 * 
	 * @param smInfo
	 */
	private void checkStatus(StoreMetaInfo smInfo) {
		if (smInfo != null && smInfo.getStoreStatus() != STORE_STATUS_ACTIVE) {
			// if migration was on progress
			if (smInfo.getStoreStatus() == STORE_STATUS_MIGRATION)
				throw new RuntimeException("Store migration was in progress, please complete store migration");
			else if (smInfo.getStoreStatus() == STORE_STATUS_RENAME)
				throw new RuntimeException("Store fields rename was in progress, please complete field renaming");
			else if (smInfo.getStoreStatus() == STORE_STATUS_PURGE)
				throw new RuntimeException("Store purging was in progress, please complete purging");
		}
	}

	/**
	 * Find Object store code if exist [META_SPACE][STORE_NAMES][STORE-NAME]=[long
	 * store code][Fully Qualified ClassName][status][New Store Code]?
	 * 
	 * @param key
	 * @param cname
	 * @param clazz
	 * @return
	 * @throws Exception
	 */
	private StoreMetaInfo loadStoreMeta(byte[] key, Class<?> clazz) throws Exception {
		// store Meta info
		StoreMetaInfo storeMeta = null;
		// Fetch Object store meta info now
		KVEntry entry = kvStore.get(key);
		// if record exist
		if (entry != null) {
			// Get byte buffer from the key
			ByteBuffer buf = ByteBuffer.wrap(entry.getKey());
			// extract Store name from the key
			String storeName = parseString(buf);
			// remaining fields from the value
			buf = ByteBuffer.wrap(entry.getValue());
			// get store code
			long storeCode = parseLong(buf);
			// get fully qualified class name
			String className = parseString(buf);
			// compare class names
			if (!clazz.getName().equals(className))
				throw new RuntimeException("Class name doesn't match with store");
			// extract store status
			byte status = parseByte(buf);
			// new store code if any
			long newStoreCode = 0;
			// if store migration status
			if (status == STORE_STATUS_MIGRATION) {
				// than extract new assigned store code
				newStoreCode = parseLong(buf);
			}
			// create a store meta object now
			storeMeta = new StoreMetaInfo(storeName, clazz, storeCode, newStoreCode, status);
		}
		// return Object reference
		return storeMeta;
	}

	/**
	 * Create a new brand new store [META_SPACE][STORE_NAMES][STORE-NAME]=[long
	 * store code][Fully Qualified ClassName][Status][new store code]?
	 * 
	 * @param storeKey
	 * @param clazz
	 * @throws Exception
	 */
	private StoreMetaInfo createOrUpdateStore(byte[] key, StoreMetaInfo smInfo) throws Exception {
		// Get a byte buffer of max key size
		ByteBuffer buf = ByteBuffer.allocate(MAX_KEY_SIZE);
		// serialise store code
		buf = serialise(buf, smInfo.getStoreCode());
		// serialise fully qualified class name
		buf = serialise(buf, smInfo.getClassName());
		// serialise status code
		buf = serialise(buf, smInfo.getStoreStatus());
		// check if new store code needs to be persisted or not
		if (smInfo.getStoreStatus() == STORE_STATUS_MIGRATION) {
			// persist new store code
			buf = serialise(buf, smInfo.getNewStoreCode());
		}
		// persist store info now
		kvStore.put(key, extract(buf));
		// make a sync
		getKVEngine().sync();
		// return store code
		return smInfo;
	}

	@Override
	public KVEngine getKVEngine() {
		// TODO Auto-generated method stub
		return kvStore;
	}

	@Override
	public CacheValue put(CacheKey key, CacheValue value) {
		// TODO Auto-generated method stub
		return objCache.put(key, value);
	}

	@Override
	public CacheValue putIfAbsent(CacheKey key, CacheValue value) {
		// TODO Auto-generated method stub
		return objCache.putIfAbsent(key, value);
	}

	@Override
	public CacheValue get(CacheKey key) {
		// TODO Auto-generated method stub
		return objCache.get(key);
	}

	@Override
	public CacheValue remove(CacheKey key) {
		// TODO Auto-generated method stub
		return objCache.remove(key);
	}

	@Override
	public void lock(Object key) throws Exception {
		// TODO Auto-generated method stub
		kLocker.lock(key);
	}

	@Override
	public void unlock(Object key) {
		// TODO Auto-generated method stub
		kLocker.unlock(key);
	}

	@Override
	public byte[] compress(byte[] data) throws Exception {
		// compress data using snappy
		return Snappy.compress(data);
	}

	@Override
	public byte[] uncompress(byte[] data) throws Exception {
		// uncompress data using snappy
		return Snappy.uncompress(data);
	}

	@Override
	public FieldAccessor getFieldAccessor(byte typeCode) {
		// TODO Auto-generated method stub
		return accessors[typeCode];
	}

	@Override
	public FieldAccessor getFieldAccessor(Object obj) {
		// TODO Auto-generated method stub
		if (obj == null)
			throw new NullPointerException("Key element can not be null");
		// if key element is Byte instance
		if (obj instanceof Byte)
			return accessors[D_TYP_BYTE];
		// if long type
		else if (obj instanceof Long)
			return accessors[D_TYP_LNG];
		// if integer instance
		else if (obj instanceof Integer)
			return accessors[D_TYP_INT];
		// if String type
		else if (obj instanceof String)
			return accessors[D_TYP_STR];
		// if float type
		else if (obj instanceof Double)
			return accessors[D_TYP_DBL];
		// if short type
		else if (obj instanceof Short)
			return accessors[D_TYP_SHRT];
		// if float type
		else if (obj instanceof Float)
			return accessors[D_TYP_FLT];
		// if boolean type
		else if (obj instanceof Boolean)
			return accessors[D_TYP_BOL];
		// if char type
		else if (obj instanceof Character)
			return accessors[D_TYP_CHAR];
		// throw exception
		else
			throw new RuntimeException("Unsupported data type: " + obj);
	}

	@Override
	public String validateClassName(Class<?> clazz) {
		// TODO Auto-generated method stub
		String cname = null;
		Collection coll = clazz.getAnnotation(Collection.class);
		// Must be having Collection annotation
		if (coll == null)
			throw new RuntimeException("@Collection not present in class " + clazz);
		// if collection name was not provided...
		cname = coll.value();
		if (cname == null || "".equals(cname = cname.trim()))
			cname = clazz.getSimpleName();
		// return class name
		return cname;
	}

	@Override
	@SuppressWarnings("static-access")
	public synchronized long genId() {
		// if exceed MAX sequence reset to 0
		if (++SEQ > MAX_SEQ) {
			long tmp = curTime;
			// in case number is running too fast
			while (tmp == (curTime = System.currentTimeMillis())) {
				try {
					// sleep 1 millisecond
					Thread.currentThread().sleep(1);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
			SEQ = 0;
		}
		// generate and return ID
		return curTime << 12 | 0x7fffffffffffffffL & SEQ;
	}

	@Override
	public boolean isSupportedType(String typeName) {
		// TODO Auto-generated method stub
		return supportedTypes.containsKey(typeName);
	}

	@Override
	public byte getTypeCode(String typeName) {
		// TODO Auto-generated method stub
		return supportedTypes.get(typeName);
	}

	@Override
	public byte[] buildKey(Object... objs) throws Exception {
		// allocate buffer of minimum size
		ByteBuffer buf = ByteBuffer.allocate(MIN_BUF_SIZE);
		// iterate throw objects and build key
		for (Object obj : objs) {
			// get corresponding field accessor
			FieldAccessor fa = getFieldAccessor(obj);
			// serialise
			fa.serialize(buf, obj);
		}
		// check key size
		if (buf.position() > MAX_KEY_SIZE)
			throw new RuntimeException("Key size exceed max key size limit");
		// extract key bytes
		return extract(buf);
	}
	
	@Override
	public synchronized void close() throws Exception {
		// clean up resources
		if (kvStore != null) {
			kvStore.close();
			kvStore = null;
			cache.clear();
			cache = null;
			objCache = null;
		}
	}
	
	@Override
	public synchronized boolean isClosed() {
		// TODO Auto-generated method stub
		return kvStore != null ? false : true;
	}
}
