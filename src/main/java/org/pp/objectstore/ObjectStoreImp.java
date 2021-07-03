package org.pp.objectstore;

import static java.lang.reflect.Modifier.isFinal;
import static java.lang.reflect.Modifier.isStatic;
import static java.lang.reflect.Modifier.isTransient;
import static org.pp.objectstore.AbstractFieldAccessor.getFieldAccessor;
import static org.pp.objectstore.DataTypes.D_TYP_DBL;
import static org.pp.objectstore.DataTypes.D_TYP_LNG;
import static org.pp.objectstore.DataTypes.D_TYP_STR;
import static org.pp.objectstore.DataTypes.D_TYP_INT;
import static org.pp.objectstore.DataTypes.supportedTypes;
import static org.pp.objectstore.Util.copyBytes;
import static org.pp.objectstore.Util.extract;
import static org.pp.objectstore.Util.genId;
import static org.pp.objectstore.Util.higherValue;
import static org.pp.objectstore.Util.stringToByteBuffer;
import static org.pp.objectstore.Util.byteBufferToString;
import static org.pp.objectstore.Util.stringToBytes;
import static org.xerial.snappy.Snappy.compress;
import static org.xerial.snappy.Snappy.uncompress;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.pp.objectstore.interfaces.GlobalObjectStoreContext;
import org.pp.objectstore.interfaces.Id;
import org.pp.objectstore.interfaces.ObjectStore;
import org.pp.objectstore.interfaces.SortKey;
import org.pp.objectstore.interfaces.StaleObjectException;
import org.pp.objectstore.interfaces.Transient;
import org.pp.objectstore.interfaces.Version;
import org.pp.qry.QueryImp;
import org.pp.qry.interfaces.ObjectIterator;
import org.pp.qry.interfaces.Query;
import org.pp.qry.interfaces.QueryContext;
import org.pp.storagengine.api.KVEntry;
import org.pp.storagengine.api.KVIterator;
import org.pp.storagengine.api.imp.KeyLocker;

class ObjectStoreImp<T> implements ObjectStore<T> {
	/**
	 * Max key length supported
	 */
	static final int MAX_KEY_SZ = 128;
	/**
	 * Max sort key allowed
	 */
	static final int MAX_SORT_KEY = 5;
	/**
     * Global object store context
     */
    final GlobalObjectStoreContext ctx ;
    /**
     * Hold the Key List + ID
     */
    private List<FieldAccessor> keys;
    /**
     * Field name to Field mapping
     */
    private Map<String, FieldAccessor> fMap;
    /**
     * Version field if present
     */
    private FieldAccessor ver;
    /**
     * Reference of class
     */
    private Class<T> clazz;
    /**
     * Collection or class name
     */
    private String cname;
    /**
     * if id generation was required
     */
    private boolean genId = false;
    /**
     * Collection key
     */
    private byte[] collKey;
    /**
     * Collection end key
     */
    private byte[] collEndKey;   
    /**
     * For atomic updates of objects
     */
    private KeyLocker<CacheKey> kLocker = new KeyLocker<>(64);
       
    /**
	 * 
	 * @param clazz
	 */
	ObjectStoreImp(String cname, Class<T> clazz, GlobalObjectStoreContext ctx) {
		this.clazz = clazz;
		this.ctx = ctx;	
		this.cname = cname;
		// retrieve field info 
		init();		
	}	

	@Override
	public void addField(String name, Object defaultVal) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void renameField(String exsistingName, String newName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeField(String exsistingName) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public T store(T t) throws Exception {
		return store(t, true);
	}
	
	@Override
	public T store(T t, boolean cache) throws Exception {
		// TODO Auto-generated method stub
		return save(t, null, false, cache, false);
	}
	
	// add + update
	private T save(T t, byte[] key, boolean upd, boolean cache, boolean putIfAbsent) throws StaleObjectException, Exception {
		// do null check first
		if (t == null)
			throw new NullPointerException("Object to persist can't be null");
		/**
		 * If any of the sort key was updated we need to delete the
		 * the old entry after the new entry was persisted.
		 * No that these two updates are currently not atomic
		 */
		byte[] oldKey = null;
		// if its new entry to be persisted
		if (genId) {
			// generate a new id
			FieldAccessor fa = keys.get(keys.size() - 1);
			Field fld = fa.getField();
			// only generate if value is zero
			if (fld.getLong(t) == 0)
			   fld.setLong(t, genId());			
		} 
		// to compare with new key
		if (key != null) {
			oldKey = key;
		}
		// build new key	
		key = buildKey(t);
		// create a cache key
		CacheKey k = new CacheKey(key);
		// lock key
		kLocker.lock(k);
		try {
			// if update request and @Version was present
			if (upd && ver != null) {
				// load raw value
				byte[] val = load(key, cache);
				// if value found
				if (val != null) {
					// create a temporary instance
					T tmp = clazz.newInstance();
					// get the version property from raw bytes
					tmp = deSerializeVersion(tmp, null, ByteBuffer.wrap(val));
					// check existing version number
					int exVer = ver.getField().getInt(tmp);
					// check new version number
					int newVer = ver.getField().getInt(t);
					// if version doen't match throw stale object exception
					if (exVer != newVer)
						throw new StaleObjectException(clazz);
					// if everything is all right, increment version field
					ver.getField().setInt(t, ++newVer);
				}
			} 
			// if version field is present, increment it
			else if (ver != null) {
			   int curVerNum = ver.getField().getInt(t);
			   ver.getField().setInt(t, ++curVerNum);				
			}
			// serialise object
			byte[] value = serialize(t);
			// compress value
			byte[] cValue = compress(value);
			// persist new entry
			ctx.getKVEngine().put(key, cValue);
			// if old key different than new key
			if (oldKey != null && !Arrays.equals(oldKey, key)) {
				// than remove the old mapping
				ctx.getKVEngine().delete(oldKey);
				// evict old entry from cache if present
				ctx.remove(new CacheKey(oldKey));			
			}
			// update cache if required
			cachePut(key, value, cache, putIfAbsent);			
		} finally {
			kLocker.unlock(k);
		}		
		return t;
	}
		
	/**
	 * Put it in cache
	 * @param t
	 * @param key
	 * @param cache
	 * @param putIfAbsent
	 * @return
	 */
	private void cachePut(byte[] key, byte[] val, boolean cache, boolean putIfAbsent) {
		if (cache) {
			CacheKey k = new CacheKey(key);
			CacheValue v = new CacheValue(val);
			if (!putIfAbsent)
				ctx.put(k, v);
			else 
				ctx.putIfAbsent(k, v);				
		} 		
	}
	
	/**
	 * Check cache entry
	 * @param cache
	 * @param key
	 * @return
	 */
	private byte[] checkCache(boolean cache, byte[] key) {
		CacheValue v = null;
		if (cache) {
			CacheKey k = new CacheKey(key);
			v = ctx.get(k);			
		}
		return v != null ? v.getValue() : null;
	}

	@Override
	public T load(T t) throws Exception {
		return load(t, true);
	}
	
	@Override
	public T load(T t, boolean cache) throws Exception {
		// do null check first
		if (t == null)
			throw new NullPointerException("Object to load can't be null");
		// build key first
		byte[] key = buildKey(t);
		// load raw value
		byte[] value = load(key, cache);
		// de serialise and return
		return value != null ? deSerialize(value, t) : null;
	}
	
	/**
	 * Common raw load method
	 * @param key
	 * @param cache
	 * @return
	 * @throws Exception
	 */
	private byte[] load(byte[] key, boolean cache) throws Exception {
		byte[] value = checkCache(cache, key);
		// if not found in cache
		if (value == null) {
			// load it from store
			KVEntry kvEntry = ctx.getKVEngine().get(key);
			// if found
			if (kvEntry != null) {
				// get compress bytes
				value = kvEntry.getValue();
				// uncompress value
				value = uncompress(value);
				// cache it if required. cache update for load is always putIfAbsent
				cachePut(key, value, cache, true);
			}
		}
		// return raw uncompress value
		return value;
	}

	@Override
	public T remove(T t) throws Exception {
		// do null check first
		if (t == null)
			throw new NullPointerException("Object to be removed can't be null");
		return delete(t, null);		
	}
	
	@Override
	public T update(T t, boolean cache) throws StaleObjectException, Exception {
		return save(t, null, true, cache, false);
	}

	@Override
	public T update(T t) throws StaleObjectException, Exception {
		return update(t, true);
	}
	
	/**
	 * Delete object
	 * @param t
	 * @return
	 * @throws Exception
	 */
	private T delete(T t, byte[] key) throws Exception {
		// build key first
		if (key == null)
		   key = buildKey(t);
		// remove from the KV store
		ctx.getKVEngine().delete(key);
		// evict corresponding cache entry if present
		ctx.remove(new CacheKey(key));
		return t;
	}
	
	@Override
	public ObjectIterator<T> iterator() throws Exception {
		// return iterator
		return new ObjectIteratorImp(new QueryContextImp(), false);
	}
	
	@Override
	public ObjectIterator<T> revIterator() throws Exception {
		// return reverse iterator
		return new ObjectIteratorImp(new QueryContextImp(), true);
	}

	@Override
	public Query<T> createQuery(String qry) {
		// null query is not allowed
		if (qry == null)
			throw new NullPointerException();
		// return query implementation
		return new QueryImp<>(qry, new QueryContextImp());
	}
	
	/**
	 *  Initialise store
	 */
	private void init() {		
		// Sort Key
		SortKey sk = null;
		// SortKey count
		int skCount = 0;
		// default regular field
		FieldAccessor id = null;
		// Sort keys + 1 ID field
		FieldAccessor[] keys = new FieldAccessor[MAX_SORT_KEY];
		// Hash map to hold all fields
		Map<String, FieldAccessor> m = new HashMap<>();
		// get all declared fields of a class
		Field[] flds = clazz.getDeclaredFields();
		// iterate over the field and retrieve necessary informations
		for (Field fld : flds) {
			// skip constant and transient field
			if (fld.isEnumConstant() || fld.isAnnotationPresent(Transient.class))
				continue;
			// get modifiers
			int mod = fld.getModifiers();
			// skip static, final and transient field
			if (isStatic(mod) || isFinal(mod) || isTransient(mod))
				continue;
			// check if field type supported persistence or not
			String tNm = fld.getType().getSimpleName();
			// skip if field type is not supported
			if (!supportedTypes.containsKey(tNm))
				continue;
			// make the field accessible
			fld.setAccessible(true);
			// get the field type
			byte type = supportedTypes.get(tNm);
			// Create field accessor
			FieldAccessor fa = getFieldAccessor(type, fld);
			// add field accessor to map
			m.put(fld.getName(), fa);
			// Check @SortKey
			sk = fld.getAnnotation(SortKey.class);
			if (sk != null) {
				int index = sk.value();
				// if order already exist
				if (keys[index - 1] != null)
					throw new RuntimeException("A SortKey already exist with the order number " + index);
				// check max sort key limit already crossed
				if (index > MAX_SORT_KEY)
					throw new RuntimeException("Maximum number SortKeys allowed is " + MAX_SORT_KEY);
				// check field type
				if (type > D_TYP_DBL)
					throw new RuntimeException("Allowed field types for SortKeys are int/long/float/Double/String");
				// add field Accessor to key list
				keys[--index] = fa;
				// increment sort key count
				skCount++;
				continue;
			}
			// @Version check
			if (fld.isAnnotationPresent(Version.class)) {
				// @Version field must be of type INT
				if (type != D_TYP_INT)
					throw new RuntimeException("@Version can only apllied to int field");
				// assign to version property
				ver = fa;
				/**
				 * Remove version field from the map.
				 * Version field to be the first to be serialise or deserialise
				 */
				m.remove(fld.getName());
				continue;
			}
			// @Id check
			if (fld.isAnnotationPresent(Id.class)) {
				if (id != null)
					throw new RuntimeException("More than one @Id is not allowed");
				// check id type
				if (type > D_TYP_STR)
					throw new RuntimeException("Unsupported ID type, allowed type: int/long/String");
				// verify ID generation type
				boolean gen = fld.getAnnotation(Id.class).gen();
				// only long type is supported for ID generation
				if (gen && type != D_TYP_LNG)
					throw new RuntimeException("Id generation is not supported on type other than long");
				// if gen required
				genId = gen ? true : false;
				id = fa;				
			}
		}
		// check if ID was provided
		if (id == null)
			throw new RuntimeException("No @Id was provided");
		// check order of sort keys
		List<FieldAccessor> nKeys = new ArrayList<>(MAX_SORT_KEY + 1);
		for (int i = 0; i < skCount; i++) {
			if (keys[i] == null)
				throw new RuntimeException("SortKey order is not well formed");
			// add to list to be returned
			nKeys.add(keys[i]);			
		}
		// add id field as well
		nKeys.add(id);
		// build collection key
		this.collKey = stringToBytes(cname);
		// build collection end key
		this.collEndKey = stringToBytes(higherValue(cname));
		// assign key list
		this.keys = nKeys;
		// assign field map
		this.fMap = m;
	}
	
	/**
	 * To build key for store/load methods
	 * 
	 * @param cName
	 * @param keys
	 * @return
	 */
	private final byte[] buildKey(Object target) throws Exception {
		// allocate enough memory
		ByteBuffer buf = ByteBuffer.allocate(MAX_KEY_SZ);
		// serialise collection name
		buf.put(copyBytes(collKey));
		// build key
		for (FieldAccessor fld : keys)
			buf = fld.get(buf, target);
		// check if key size exceeded
		if (buf.position() > MAX_KEY_SZ)
			keySizeIssue();
		// extract key
		return extract(buf);
	}	

	/**
	 * To build key for query (seek method) when start key was provided
	 * 
	 * @param cname
	 * @param sk
	 * @param val
	 * @return
	 * @throws Exception
	 */
	private final byte[] buildKeyUsingSortKey(Object val) throws Exception {
		// allocate enough memory
		ByteBuffer buf = ByteBuffer.allocate(MAX_KEY_SZ);
		// serialise collection name
		buf.put(copyBytes(collKey));
		// validate and get sort value bytes
		buf = keys.get(0).toBytes(buf, val);
		// check if key size exceeded
		if (buf.position() > MAX_KEY_SZ)
			keySizeIssue();
		// extract key
		return extract(buf);
	}
	
	/**
	 * Serialise object
	 * 
	 * @param cName
	 * @param keys
	 * @param target
	 * @return
	 * @throws Exception
	 */
	private final byte[] serialize(Object target) throws Exception {
		// allocate enough memory
		ByteBuffer buf = ByteBuffer.allocate(MAX_KEY_SZ);
		// serialise version property first
		buf = serialiseVersion(buf, target);
		// serialise rest of the fields
		for (Map.Entry<String, FieldAccessor> e : fMap.entrySet()) {
			buf = stringToByteBuffer(e.getKey(), buf);
			// now value
			buf = e.getValue().get(buf, target);
		}
		// return serialise data
		return extract(buf);
	}
	
	/**
	 * Serialise version property
	 * @param buf
	 * @return
	 * @throws Exception
	 */
	private final ByteBuffer serialiseVersion(ByteBuffer buf, Object target) throws Exception {
		if (ver != null) {
			buf = stringToByteBuffer(ver.getName(), buf);
			buf = ver.get(buf, target);
		}
		return buf;
	}
	
	/**
	 * De serialise version property
	 * @param t
	 * @param buf
	 * @return
	 * @throws Exception
	 */
	private final T deSerializeVersion(T t, Map<String, FieldAccessor> m, ByteBuffer buf) throws Exception {
		if (ver != null) {
			// get version property name
			String fNm = byteBufferToString(buf);
			// check if both version name matching or not
			if (!fNm.equals(ver.getName()))
				throw new RuntimeException("@Version name missmatch");
			// set version field value
			if (t != null)
				ver.set(buf, t);
			else 
				m.put(fNm, ver.clone(buf));
		}
		return t;
	}
	
    /**
	 * When key size exceed limit
	 */
	static final void keySizeIssue() {
		throw new RuntimeException("Key size can not be more than " + MAX_KEY_SZ);
	}	
	
	/**
	 * De-serialise object
	 * 
	 * @param value
	 * @param keys
	 * @param m
	 * @param target
	 * @throws Exception
	 */
	private final T deSerialize(byte[] value, T target) throws Exception {
		// ensure value is not first
		if (value == null)
			return null;
		// get a byte buffer
		ByteBuffer buf = ByteBuffer.wrap(value);
		// de serialise version property first
		deSerializeVersion(target, null, buf);
		// de serialise object
		while (buf.hasRemaining()) {
			// decode to string
			String fNm = byteBufferToString(buf);
			// get corresponding field accessor
			FieldAccessor fa = fMap.get(fNm);
			if (fa == null)
				throw new RuntimeException("Field name '" + fNm + "' is missing in class " + cname);
			// set field
			fa.set(buf, target);
		}
		return target;
	}
		
	/**
	 * Key bytes holds sort keys and ID. Desrialise the key to map
	 * 
	 * @param key
	 * @param km
	 * @return
	 * @throws Exception
	 */
	private final void keyToMap(byte[] key, Map<String, FieldAccessor>  m) throws Exception {
		// get a byte buffer
		ByteBuffer buf = ByteBuffer.wrap(key);
		// skip collection name
		buf.get();
		buf.position(buf.getInt() * 2 + 5);
		// iterate over sort keys and ID field accessor
		String name;		
		for (FieldAccessor fa : keys) {
			// get field name
			name = fa.getName();
			// put this into result map
			m.put(name, fa.clone(buf));
		}		
	}
	
	/**
	 * De serialise object to map
	 * @param val
	 * @return
	 * @throws Exception
	 */
	private final void valueToMap(byte[] val, Map<String, FieldAccessor>  m) throws Exception {
		// get a byte buffer
		ByteBuffer buf = ByteBuffer.wrap(val);
		// de serialise version property first
		deSerializeVersion(null, m, buf);
		// de serialise object		
		while (buf.hasRemaining()) {
			// decode to string
			String fNm = byteBufferToString(buf);
			// get corresponding field accessor
			FieldAccessor fa = fMap.get(fNm);
			if (fa == null)
				throw new RuntimeException("Field name '" + fNm + "' is missing in class " + cname);
			// set field
			m.putIfAbsent(fNm, fa.clone(buf));	
		}
	}
	
	/**
	 * Object iterator implementation
	 * @author prasantsmac
	 *
	 */
	private final class ObjectIteratorImp implements ObjectIterator<T> {
		/**
         * Query Context
         */
        final private QueryContextImp ctx;
        /**
         * Iterator close indicator
         */
        private boolean close = false;
       
        /**
         * Only constructor
         * @param ctx
         * @param rev
         */
        ObjectIteratorImp (QueryContextImp ctx, boolean rev) {
        	this.ctx = ctx;
        	// indicate this context going to support iterator
        	this.ctx.oitr = true;
        	// seek from the beginning
        	this.ctx.seek(null, rev);
        }
        
		@Override
		public boolean hasNext() {
			// check iterator status
			status();
			// if next record doesn't exist
			if (!ctx.nextRecord()) {
				close();
				return !close;
			}
			// if record exist
			return true;
		}

		@Override
		public T next() {
			return status() ? ctx.currentRecord() : null;
		}

		@Override
		public T update() {
			return status() ? ctx.updateCurrent() : null;
		}

		@Override
		public T remove() {
			return status() ? ctx.deleteCurrent() : null;
		}

		@Override
		public void close() {
			if (!close) {
				ctx.reset();
				close = true;				
			}			
		}
		
		/**
		 * Check iterator status
		 * 
		 * @return
		 */
		private boolean status() {
			if (close)
				throw new RuntimeException("Iterator closed already");
			return true;
		}
		
	}

	/**
	 * Query context implementation. txnIterator 
	 * @author prasantsmac
	 *
	 */
	private final class QueryContextImp implements QueryContext<T> {
		/**
         * KV Iterator
         */
        KVIterator itr;
        /**
         * Current selected record
         */
        T t;  
        /**
         * Count number of iteration
         */
        int count;
        /**
         * Reset indicator
         */
		boolean reset = false;
		/**
		 * If this context supporting object iterator
		 */
		boolean oitr = false;
		/**
		 * Current loaded value
		 */
		byte[] value;
		/**
		 * Currently loaded key
		 */
		byte[] key;
		 /**
         * Result map
         */
        Map<String, FieldAccessor> res = new HashMap<>();
		
				
		@Override
		public boolean isSortKey(String fname) {
			// get sort key field now			
			return keys.get(0).getField().getName().equals(fname) ? true : false;			
		}

		@Override
		public void seek(Object from, boolean rev) {
			try {
				// start key
				byte[] start = null, end = null;
				// if not reverse iterator
				if (!rev) {
					// common end point
					end = copyBytes(collEndKey);
					// if seek key was provided
					if (from != null) 
						start = buildKeyUsingSortKey(from);
					else 
						start = copyBytes(collKey);					
				} 
				// otherwise
				else {
					// common end point
					end = copyBytes(collKey);
					// if seek key was provided
					if (from != null) 
						start = buildKeyUsingSortKey(from);
					else 
						start = copyBytes(collEndKey);				
				}
				// get KV iterator now
				itr = ctx.getKVEngine().iterator(start, end, rev);				
			} catch (Exception e) {
				throw new RuntimeException(e);				
			}			
		}
		
		@Override
		public boolean nextRecord() {
			try {
				// if no next record
				if (itr.hasNext()) {
					// get the entry
					KVEntry entry = itr.next();
					// get the key
					key = entry.getKey();
					// get the value
					value = entry.getValue();
					// if this context supporting object iterator
					if (oitr) {
						// create a new instance
						t = clazz.newInstance();
						// uncompress and deserialise
						deSerialize(uncompress(value), t);
					} else {
						// clear result map
						res.clear();
						// set current object to null
						t = null;
					}
					// count number of iteration
					count++;
					// 
					return true;
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			//
			return false;
		}
		
		@Override
		public Object getFieldValue(String fname) {
			try {
				// if field name not exist, throw exception
				if (!fMap.containsKey(fname))
					throw new RuntimeException("Field '" + fname + "' not valid for class " + clazz);
				// load keys if not loaded yet
				if (res.size() == 0) 
					keyToMap(key, res);
				// check if the value is present in map
				FieldAccessor val = res.get(fname);
				/**
				 * If no mapping is found and value is not loaded yet
				 */
				if (val == null && res.size() < fMap.size()) {
					// uncompress value
					value = uncompress(value);
					// convert value to map now
					valueToMap(value, res);
					// try again
					val = res.get(fname);
				}
				// return value
				return val.get();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		} 
		
		@Override
		public T currentRecord() { 
			// if map to object transformation not yet happened
			if (t == null && res.size() > 0) {
				try {
					// create a new instance
					t = clazz.newInstance();
					// check if if whole map was loaded or not
					if (res.size() < fMap.size()) {
						// uncompress value
						value = uncompress(value);
						// de serialise
						deSerialize(value, t);
					} else {
						// get map values
						Collection<FieldAccessor> values = res.values();
						// convert map to object
						for (FieldAccessor fld : values) {
							fld.set(t);
						}
					}
					// put the serialise value in cache
					cachePut(key, value, true, true);
					
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
			// if record was not loaded yet
			else if (t == null)
				throw new NoSuchElementException();
			
			return t;
		}

		@Override
		public T deleteCurrent() {
			if (t == null)
				throw new NoSuchElementException();
			// delete
			try { delete(t, key); } catch (Exception e) {
				throw new RuntimeException(e);
			}
			return t;
		}

		@Override
		public T updateCurrent() {
			if (t == null)
				throw new NoSuchElementException();
			// update
			try { save(t, key, true, true, false); } catch (Exception e) {
				throw new RuntimeException(e);
			}
			return t;
		}
		
		@Override
		public void reset() {
			if (!reset) {
				reset = true;
				itr.close();
				t = null;
				System.out.println(count);
			}
		}		
	}	
}
