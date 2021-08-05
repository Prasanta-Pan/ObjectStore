package org.pp.objectstore;

import static java.lang.reflect.Modifier.isFinal;
import static java.lang.reflect.Modifier.isStatic;
import static java.lang.reflect.Modifier.isTransient;
import static org.pp.objectstore.ByteFieldAccessor.parseByte;
import static org.pp.objectstore.ByteFieldAccessor.serialise;
import static org.pp.objectstore.LongFieldAccessor.parseLong;
import static org.pp.objectstore.LongFieldAccessor.serialise;
import static org.pp.objectstore.StringFieldAccessor.serialise;
import static org.pp.objectstore.StringFieldAccessor.parseString;
import static org.pp.objectstore.Util.extract;
import static org.pp.objectstore.interfaces.Constants.DATA_SPACE;
import static org.pp.objectstore.interfaces.Constants.D_TYP_DBL;
import static org.pp.objectstore.interfaces.Constants.D_TYP_INT;
import static org.pp.objectstore.interfaces.Constants.D_TYP_LNG;
import static org.pp.objectstore.interfaces.Constants.D_TYP_STR;
import static org.pp.objectstore.interfaces.Constants.FLD_MOD_GEN;
import static org.pp.objectstore.interfaces.Constants.FLD_MOD_ID;
import static org.pp.objectstore.interfaces.Constants.FLD_MOD_SORT_KEY;
import static org.pp.objectstore.interfaces.Constants.FLD_MOD_VERSION;
import static org.pp.objectstore.interfaces.Constants.MAX_KEY_SIZE;
import static org.pp.objectstore.interfaces.Constants.MAX_SORT_KEY;
import static org.pp.objectstore.interfaces.Constants.META_SPACE;
import static org.pp.objectstore.interfaces.Constants.STORE_DATA_LEN;
import static org.pp.objectstore.interfaces.Constants.STORE_META;
import static org.pp.objectstore.interfaces.Constants.STORE_META_LEN;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.pp.objectstore.interfaces.FieldAccessor;
import org.pp.objectstore.interfaces.FieldMetaInfo;
import org.pp.objectstore.interfaces.FieldRenameHandler;
import org.pp.objectstore.interfaces.GlobalObjectStoreContext;
import org.pp.objectstore.interfaces.Id;
import org.pp.objectstore.interfaces.LongWrapper;
import org.pp.objectstore.interfaces.ObjectMapIterator;
import org.pp.objectstore.interfaces.ObjectStore;
import org.pp.objectstore.interfaces.PurgeStoreHandler;
import org.pp.objectstore.interfaces.SortKey;
import org.pp.objectstore.interfaces.StaleObjectException;
import org.pp.objectstore.interfaces.StoreDataHandler;
import org.pp.objectstore.interfaces.StoreMetaInfo;
import org.pp.objectstore.interfaces.Transient;
import org.pp.objectstore.interfaces.Version;
import org.pp.qry.QueryImp;
import org.pp.qry.interfaces.ObjectIterator;
import org.pp.qry.interfaces.Query;
import org.pp.qry.interfaces.QueryContext;
import org.pp.storagengine.api.KVEntry;
import org.pp.storagengine.api.KVIterator;
/**
 * Object store implementation
 * @author prasantsmac
 *
 * @param <T>
 */
final class ObjectStoreImp<T> implements ObjectStore<T> {
	/**
	 * Stored meta info
	 */
	final StoreMetaInfo smInfo;
	/**
	 * Global store context
	 */
	final GlobalObjectStoreContext ctx;	
	/**
	 * Key list 
	 */
	private List<FieldMetaInfo> keys;
	/**
	 * Filed map (Field name ==> FieldMeta)
	 */
	private Map<String,FieldMetaInfo> fMap;
	/**
	 * Code map (Field code ==> FieldMeta)
	 */
	private Map<LongWrapper, FieldMetaInfo> cMap;
	/**
	 * if versioning required for the class
	 */
	private FieldMetaInfo ver;
	/**
	 * If Id generation required
	 */
	private boolean isGen;
       
    /**
     * Create or open store and introspect class and validate meta info
     * @param smInfo
     * @param ctx
     * @param fh
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
	ObjectStoreImp(StoreMetaInfo smInfo,
		 	 		GlobalObjectStoreContext ctx, 
		 	 		FieldRenameHandler fh) throws Exception 
    {
    	this.ctx = ctx;
		this.smInfo = smInfo;
		// Introspect class
		classIntrospect();
		// load store field info
		Object[] fields = loadFieldMetaInfo();
		// verify sort keys
		Collection<FieldMetaInfo> coll = verifySortKeys((List<FieldMetaInfo>) fields[0]);
		// persist sort keys if the store being created first time
		addFieldsToStore(coll);
		// verify version
		verifyVersion((FieldMetaInfo) fields[1]);
		// verify rest of the fields
		coll = verifyGeneralFields((Map<String, FieldMetaInfo>) fields[2], fh);
		// add version field if need to persist first time
		if (fields[1] == null && ver != null)
			coll.add(ver);
		// persist rest of the fields now
		addFieldsToStore(coll);
		// remove fields
		removeFieldsFromStore(coll);
		// get field code map
		cMap = getCodeMap();
		// make a sync
		ctx.getKVEngine().sync(); 	
    }  
    /**
     * Only open store if exist
     * @param smInfo
     * @param ctx
     * @throws Exception
     */
	@SuppressWarnings("unchecked")
	ObjectStoreImp(StoreMetaInfo smInfo, GlobalObjectStoreContext ctx) throws Exception {
		this.ctx = ctx;
		this.smInfo = smInfo;
		// load store field info
		Object[] fields = loadFieldMetaInfo();
		// assign key list
		keys = (List<FieldMetaInfo>) fields[0];
		// assign version
		ver = (FieldMetaInfo) fields[1];
		// assign rest
		fMap = (Map<String, FieldMetaInfo>) fields[2];
		// get code map
		cMap = getCodeMap();
	}
	
	/**
	 *  Start introspection class and retrieve field information
	 */
	private void classIntrospect() {		
		// Sort Key
		SortKey sk = null;
		// SortKey count
		int skCount = 0;
		// default ID field
		FieldMetaInfo id = null;
		// Sort keys + 1 ID field
		FieldMetaInfo[] keys = new FieldMetaInfo[MAX_SORT_KEY];
		// Hash map to hold all fields
		Map<String, FieldMetaInfo> m = new HashMap<>();
		// get all declared fields of a class
		Field[] flds = smInfo.getClazz().getDeclaredFields();
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
			if (!ctx.isSupportedType(tNm))
				continue;
			// make the field accessible
			fld.setAccessible(true);
			// get the field type
			byte type = ctx.getTypeCode(tNm);
			// Create a Field Meta object
			FieldMetaInfo fm = new FieldMetaInfo(fld, null, -1L, type, FLD_MOD_GEN);
			// Check @SortKey
			sk = fld.getAnnotation(SortKey.class);
			if (sk != null) {
				int index = sk.value();
				// if order already exist
				if (keys[index - 1] != null)
					throw new RuntimeException("A SortKey already exist with the order number " + index);
				// check if max sort key limit already crossed
				if (index > MAX_SORT_KEY)
					throw new RuntimeException("Maximum number SortKeys allowed is " + MAX_SORT_KEY);
				// check field type
				if (type > D_TYP_DBL)
					throw new RuntimeException("Allowed field types for SortKeys are int/long/float/Double/String");
				// add field meta to key list
				fm.setFldModifier(FLD_MOD_SORT_KEY);
				keys[--index] = fm;
				// increment sort key count
				skCount++;
				continue;
			}
			// @Version check
			if (fld.isAnnotationPresent(Version.class)) {
				// @Version field must be of type INT
				if (type != D_TYP_INT)
					throw new RuntimeException("@Version can only apllied to int field");
				// more than one version field is not allowed
				if (ver != null)
					throw new RuntimeException("More than one @Version is not allowed");
				// assign to version property
				fm.setFldModifier(FLD_MOD_VERSION);
				ver = fm;
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
				isGen = gen ? true : false;
				fm.setFldModifier(FLD_MOD_ID);
				id = fm;
				continue;
			}
			// add field meta object to map
			m.put(fld.getName(), fm);
		}
		// check if ID was provided
		if (id == null)
			throw new RuntimeException("No @Id was provided");
		// check order of sort keys
		List<FieldMetaInfo> nKeys = new ArrayList<>(MAX_SORT_KEY + 1);
		for (int i = 0; i < skCount; i++) {
			if (keys[i] == null)
				throw new RuntimeException("SortKey order is not well formed");
			// add to list to be returned
			nKeys.add(keys[i]);			
		}
		// add id field as well
		nKeys.add(id);
		// assign key list
		this.keys = nKeys;
		// assign field map
		this.fMap = m;
	}
	
	/**
	 * Load field meta info from store
	 * @return
	 * @throws Exception
	 */
	private Object[] loadFieldMetaInfo() throws Exception {
		// General fields
		Map<String, FieldMetaInfo> gm = new HashMap<>();
		// Sort Keys
		List<FieldMetaInfo> skl = new LinkedList<>();
		// Version Field
		FieldMetaInfo lVer = null;		
		// build key [META_SPACE][STORE_META][STORE-CODE][FIELD-CODE]=[FIELD-NAME][FIELD-TYPE][MODIFIER]
		byte[] startKey = ctx.buildKey(META_SPACE, STORE_META, smInfo.getStoreCode());
		// where to end
		byte[] endKey = ctx.buildKey(META_SPACE, STORE_META, smInfo.getStoreCode() + 1);
		// load all fields and code
		KVIterator itr = ctx.getKVEngine().iterator(startKey, endKey);
		// iterate throw Field Meta info and validate
		while (itr.hasNext()) {
			// get entry
			KVEntry e = itr.next();
			// get byte buffer from key
			ByteBuffer buf = ByteBuffer.wrap(e.getKey());
			// position buffer to field name
			buf.position(STORE_META_LEN);
			// extract field code
			long fcode = parseLong(buf);
			// Remaining fields from value
			buf = ByteBuffer.wrap(e.getValue());
			// extract field name
			String fieldName = parseString(buf);
			// extract field type
			byte ftype = parseByte(buf);
			// extract field modifier
			byte modifier = parseByte(buf);
			// create a field meta info
			FieldMetaInfo mi = new FieldMetaInfo(null, fieldName, fcode, ftype, modifier);
			// check modifier type
			switch (modifier) {
			    // if sort key
			    case FLD_MOD_SORT_KEY:
			    // if ID
			    case FLD_MOD_ID :
			    	skl.add(mi);
			    	break;
			    // if general field type
			    case FLD_MOD_GEN:
			    	gm.put(fieldName, mi);
			    	break;
			   // if version field
			    case FLD_MOD_VERSION :
			    	if (lVer != null)
			    		throw new RuntimeException("More than one version field");
			      lVer = mi;
			      break;
			    default :
			    	  throw new RuntimeException("Unknown field modifier");
			}			
		}
		// return SortKey List, Version, Field map
		return new Object[] { skl, lVer, gm};
	}
	
	/**
	 * Verify sort key list against eash other
	 * @param classKeyList
	 * @param storeKeyList
	 */
	private Collection<FieldMetaInfo> verifySortKeys(List<FieldMetaInfo> storeKeyList) {
		// match both count
		if (storeKeyList.size() > 0 && keys.size() != storeKeyList.size())
			throw new RuntimeException("SortKey list coun't missmatch");
		// add all current fields to set, to be added or removed from store
		Set<FieldMetaInfo> keySet = new HashSet<>(keys);
		// get iterator of class key list
		Iterator<FieldMetaInfo> classItr = keys.iterator();
		// get iterator of store key list
		Iterator<FieldMetaInfo> storeItr = storeKeyList.iterator();
		// iterate both together
		while (classItr.hasNext() && storeItr.hasNext()) {
			// get class field meta
			FieldMetaInfo cmi = classItr.next();
			// get store field meta
			FieldMetaInfo smi = storeItr.next();
			// set field code
			cmi.setFldCode(smi.getFldCode());
			// compare both 
			if (!cmi.equals(smi))
				throw new RuntimeException("SortKey list doesn't match");
			// remove field meta
			keySet.remove(cmi);
		}
		// return key set
		return keySet;
	}
	/**
	 * Verify version fileds
	 * @param storeVersion
	 */
	private void verifyVersion(FieldMetaInfo storeVersion) {
		if (storeVersion != ver && ((storeVersion == null || ver == null) || !storeVersion.equals(ver)))
				throw new RuntimeException("Version field missmatch");
	}
	/**
	 * Verify general fields
	 * @param m
	 */
	private Collection<FieldMetaInfo> verifyGeneralFields(Map<String, FieldMetaInfo> m, FieldRenameHandler fh) {
		// add all fields to map
		Map<String, FieldMetaInfo> fieldSet = new HashMap<>(fMap);
		// iterate through stored fields
		for (Map.Entry<String, FieldMetaInfo> e : m.entrySet()) {
			// if field was removed from the class
			FieldMetaInfo mi = fieldSet.remove(e.getKey());
			// if this field was removed or renamed
			if (mi == null && fh != null) {
				// if the field was renamed
				String newField = fh.newFieldName(e.getKey());
				// check if the new name of the field is present or not
				if (newField != null && !fMap.containsKey(newField))
					throw new RuntimeException("New field '" + newField + "' not found in class");
				// if all good
				if (newField != null) {
					// get reference of newly replaced field
					FieldMetaInfo newFm = fMap.get(newField);
					// set existing field code to it
					newFm.setFldCode(e.getValue().getFldCode());
					// now verify field signature
					if (!newFm.equals(e.getValue())) {
						throw new RuntimeException("Field signature doesn't match '" + e.getKey() + "'");
					}
				}
			}
			// mark this field to be deletable
			if (mi == null) {
				FieldMetaInfo mie = e.getValue();
				// mark it ass deleted
				mie.setActive(false);
				// add it to set
				fieldSet.put(e.getKey(), mie);
			}
		}
		// return fields to be persisted
		return fieldSet.values();
	}
	/**
	 * Get a field code map
	 * @return
	 */
	private Map<LongWrapper, FieldMetaInfo> getCodeMap() {
		Map<LongWrapper, FieldMetaInfo> codeMap = new HashMap<>(fMap.size());
		for (FieldMetaInfo fm : fMap.values()) {
			codeMap.put(new LongWrapper(fm.getFldCode()), fm);
		}
		return codeMap;
	}
	/**
	 * [META_SPACE][STORE_META][STORE-CODE][FIELD-CODE]=[FIELD-NAME][FIELD-TYPE][MODIFIER]
	 * Add all new fields
	 * @param m
	 * @throws Exception
	 */
	private void addFieldsToStore(Collection<FieldMetaInfo> col) throws Exception {
		for (FieldMetaInfo mi: col) {
			if (mi.isActive()) {
				// generate field code first
				if (mi.getFldCode() < 0)
				    mi.setFldCode(ctx.genId());
				// get Field key
				byte[] key = ctx.buildKey(META_SPACE, STORE_META, smInfo.getStoreCode(), mi.getFldCode());
				// build value [FIELD-NAME][FIELD-TYPE][MODIFIER]
				ByteBuffer buf = ByteBuffer.allocate(MAX_KEY_SIZE);
				// serialise field name
				buf = serialise(buf, mi.getFldName());
				// serialise field type
				buf = serialise(buf, mi.getFldType());
				// serialise field modifier
				buf = serialise(buf, mi.getFldModifier());
				// persist KV pair
				ctx.getKVEngine().put(key, extract(buf));
			}				
		}
	}
	/**
	 * [META_SPACE][STORE_META][STORE-CODE][FIELD-CODE]=[FIELD-NAME][FIELD-TYPE][MODIFIER]
	 * Remove fields
	 * @param m
	 * @throws Exception
	 */
	private void removeFieldsFromStore(Collection<FieldMetaInfo> col) throws Exception {
		for (FieldMetaInfo mi: col) {
			if (!mi.isActive()) {
				// get field key
				byte[] key = ctx.buildKey(META_SPACE, STORE_META, smInfo.getStoreCode(), mi.getFldCode());
				// delete field key
				ctx.getKVEngine().delete(key);
			}				
		}
	}	
	  	
	/**
	 * Migrate data from one store to another
	 * @param src
	 * @param dest
	 * @param hdlr
	 * @throws Exception
	 */
	static <T> void migrate(ObjectStoreImp<?> src, 
							ObjectStoreImp<T> dest, 
							StoreDataHandler<T> hdlr) throws Exception 
	{
		ObjectMapIterator mItr = src.mapIterator(hdlr != null ? true : false);
		try {
			while (mItr.hasNext()) {
				// get map
				Map<String, Object> m = mItr.nextMap();
				T t = hdlr.handle(m);
				if (t != null) {
					// migrate data to new store
					dest.store(t);
				}
				// delete record
				//mItr.delete();
			}
		} finally {
			mItr.close();
		}
	}
	/**
	 * Purge store completely
	 * @param target
	 * @param hdlr
	 * @throws Exception
	 */
	static <T> void purgeStore(ObjectStoreImp<T> target, 
						   	   PurgeStoreHandler hdlr) throws Exception 
	{
		ObjectMapIterator mItr = target.mapIterator(hdlr != null ? true : false);
		try {
			while (mItr.hasNext()) {
				// get map
				Map<String, Object> m = mItr.nextMap();
				if (hdlr != null)
					hdlr.handle(m);
				// delete record
				mItr.delete();
			}
		} finally {
			mItr.close();
		}	
	}
	
	/**
	 * Return a map iterator implementation
	 * @param mapRequired
	 * @return
	 * @throws Exception
	 */
	ObjectMapIterator mapIterator(boolean mapRequired) throws Exception {
		// return map iterator implementation
		return new ObjectMapIteratorImp(mapRequired);
	}
	
	@Override
	public T store(T t) throws Exception {
		return store(t, true);
	}
	
	@Override
	public T store(T t, boolean cache) throws Exception {
		// TODO Auto-generated method stub
		return save(t, null, cache);
	}
	
	// add + update
	T save(T t, byte[] eKey, boolean cache) throws Exception {
		// do null check first
		if (t == null)
			throw new NullPointerException("Object to persist can't be null");
		/**
		 * If any of the sort key was updated we need to delete the
		 * the old entry after the new entry was persisted.
		 * No that these two updates are currently not atomic
		 */
		byte[] oldKey = (eKey != null) ? eKey : null ;
		// build new key	
		byte[] key = buildKey(t);
		// create a cache key
		CacheKey k = new CacheKey(key);
		// lock key
		ctx.lock(k);
		try {
			// if id generation is required
			if (isGen) {
				// get ID field
				Field fld = keys.get(keys.size() - 1).getField();
				// only generate if value is zero
				if (fld.getLong(t) == 0)
				   fld.setLong(t, ctx.genId());			
			} 
			// if versioning is required
			if (ver != null) {
				// get the version field reference
				Field fld = ver.getField();
				// get the version value
				int newVer = fld.getInt(t);
				// load raw value
				byte[] val = load(k, cache);
				// if value found
				if (val != null) {
					// get the existing version number
					int exVer = extractVersion(val);
					// if version doen't match throw stale object exception
					if (exVer != newVer)
						throw new StaleObjectException(smInfo.getClassName());
				}
				// increment version number
				fld.setInt(t, ++newVer);		
			} 			
			// serialise object
			byte[] value = serializeValue(t);
			// compress value
			byte[] cValue = ctx.compress(value);
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
			if (cache) {
				// put in cache if enabled
				cachePut(k, new CacheValue(value), false);		
			}
		} finally {
			ctx.unlock(k);
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
	void cachePut(CacheKey key, CacheValue val, boolean putIfAbsent) {
		// if not put if absent request
		if (!putIfAbsent) {
			// put data in cache
			ctx.put(key, val);
		} else {
			// only put if no mapping
			ctx.putIfAbsent(key, val);
		}				
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
		byte[] value = load(new CacheKey(key), cache);
		// Deserialize and return
		return value != null ? deSerialize(value, t) : null;
	}
	
	/**
	 * Common raw load method
	 * @param key
	 * @param cache
	 * @return
	 * @throws Exception
	 */
	private byte[] load(CacheKey key, boolean cache) throws Exception {
		// check if cache enabled
		CacheValue value = cache ? ctx.get(key) : null;
		// if value not found in cache
		if (value == null) {
			// load it from store
			KVEntry kvEntry = ctx.getKVEngine().get(key.getKey());
			// if found
			if (kvEntry != null) {
				// get compress bytes
				byte[] val = kvEntry.getValue();
				// uncompress value
				val = ctx.uncompress(val);
				// create cache value
				if (cache) {
					value = new CacheValue(val);
					// cache it
					cachePut(key, value, false);
				}
				return val;
			}
		}		
		// return value
		return (value != null) ? value.getValue() : null;
	}

	@Override
	public T remove(T t) throws Exception {
		// do null check first
		if (t == null)
			throw new NullPointerException("Object to be removed can't be null");
		return delete(t, null);		
	}
	
	/**
	 * Delete object
	 * @param t
	 * @return
	 * @throws Exception
	 */
	public T delete(T t, byte[] key) throws Exception {
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
		return new ObjectIteratorImp(false);
	}
	
	@Override
	public ObjectIterator<T> iterator(boolean reverse) throws Exception {
		// return reverse iterator
		return new ObjectIteratorImp(reverse);
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
	 * To build key for query (seek method) when start key was provided
	 * [DATA_SPACE][STORE_CODE][SORT_KEY]?....[ID]=...
	 * 
	 * @param cname
	 * @param sk
	 * @param val
	 * @return
	 * @throws Exception
	 */
	final byte[] buildKeyDataSeek(Object val) throws Exception {
		// allocate enough memory
		ByteBuffer buf = ByteBuffer.allocate(MAX_KEY_SIZE);
		// serialise data store meta
		buf = serialise(buf, DATA_SPACE);
		// serialise store code meta
		buf = serialise(buf, smInfo.getStoreCode());
		// retrieve field type
		byte fType = keys.get(0).getFldType();
		// retrieve serializer 
		buf = ctx.getFieldAccessor(fType).serialize(buf, val);
		// check if key size exceeded
		if (buf.position() > MAX_KEY_SIZE)
			keySizeIssue();
		// extract key
		return extract(buf);
	}
	/**
	 * [DATA_SPACE][STORE_CODE][SORT_KEY]?....[ID]=...
	 * @param target
	 * @return
	 * @throws Exception
	 */
	final byte[] buildKey(Object target) throws Exception {
		// allocate enough memory
		ByteBuffer buf = ByteBuffer.allocate(MAX_KEY_SIZE);
		// serialise data store meta
		buf = serialise(buf, DATA_SPACE);
		// serialise store code meta
		buf = serialise(buf, smInfo.getStoreCode());
		// serialise sort keys and ID now
		for (FieldMetaInfo mi: keys) {
			// retrieve field type
			byte fType = mi.getFldType();
			// retrieve serialiser and serialise
			buf = ctx.getFieldAccessor(fType).serialize(buf, target, mi.getField());
		}
		// check if key size exceeded
		if (buf.position() > MAX_KEY_SIZE)
			keySizeIssue();
		// extract key
		return extract(buf);
	}
	
	/**
	 * Serialise object
	 * [DATA_SPACE][STORE_CODE][SORT_KEY]?....[ID]=[FIELD_CODE][FIELD_DATA]....
	 * @param cName
	 * @param keys
	 * @param target
	 * @return
	 * @throws Exception
	 */
	private final byte[] serializeValue(Object target) throws Exception {
		// allocate enough memory
		ByteBuffer buf = ByteBuffer.allocate(MAX_KEY_SIZE);
		// serialise version property first
		buf = serialiseVersion(buf, target);
		// serialise all fields now
		for (FieldMetaInfo mi : fMap.values()) {
			// serialise field code first
			buf = serialise(buf, mi.getFldCode());
			// get field type
			byte fType = mi.getFldType();
			// retrieve serialiser and serialise
			buf = ctx.getFieldAccessor(fType).serialize(buf, target, mi.getField());
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
			// retrieve serialiser and serialise
			ctx.getFieldAccessor(ver.getFldType()).serialize(buf, target, ver.getField());
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
	final T deSerializeVersion(T t, Map<String, FieldAccessor> m, ByteBuffer buf) throws Exception {
		if (ver != null) {
			// if target specified
			if (t != null) {
				// de serialise version value and set it to target
				ctx.getFieldAccessor(ver.getFldType()).deserialize(buf, t, ver.getField());				
			} else {
				// extract value
				m.get(ver.getFldName()).set(buf);
			}
		}
		return t;
	}
	/**
	 * Extract version number
	 * @param value
	 * @return
	 */
	int extractVersion(byte[] value) {
		return ((ByteBuffer) ByteBuffer.wrap(value).position(9)).getInt();
	}
	
    /**
	 * When key size exceed limit
	 */
	static final void keySizeIssue() {
		throw new RuntimeException("Key size can not be more than " + MAX_KEY_SIZE);
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
	final T deSerialize(byte[] value, T target) throws Exception {
		// now deSerialize value 
		ByteBuffer buf = ByteBuffer.wrap(value);
		// De serialise version property first
		deSerializeVersion(target, null, buf);
		// just to avoid unnecessary auto boxing of long value
		final LongWrapper lw = new LongWrapper();
		// de serialise object
		while (buf.hasRemaining()) {
			// parse field code
			long fc = parseLong(buf);
			// retrieve field meta info
			FieldMetaInfo mi = cMap.get(lw.setVal(fc));
			// field might have been deleted
			if (mi == null) {
				// we need to skip those bytes
				int pos = buf.position();
				// skip bytes
				ctx.getFieldAccessor(buf.get(pos)).skip(buf);
				continue;
			}
			// deSerialize
			ctx.getFieldAccessor(mi.getFldType()).deserialize(buf, target, mi.getField());
		}
		return target;
	}
	/**
	 * Deserialize key first
	 * @param key
	 * @param target
	 * @return
	 * @throws Exception
	 */
	final T deSerializeKey(byte[] key, T target) throws Exception {
		// get a byte buffer
		ByteBuffer buf = ByteBuffer.wrap(key);
		// skip meta info
		buf.position(STORE_DATA_LEN);
		// iterate throw key list
		for (FieldMetaInfo mi : keys) {
			// get field accessor
			FieldAccessor acc = ctx.getFieldAccessor(mi.getFldType());
			// deserialize value
			acc.deserialize(buf, target, mi.getField());
		}		
		return target;
	}
	/**
	 * Object map iterator implementation
	 * @author prasantsmac
	 *
	 */
	final class ObjectMapIteratorImp implements ObjectMapIterator {
		/**
		 * Long wrapper
		 */
		final LongWrapper lw = new LongWrapper();
		/**
		 * Current selected value map
		 */
		private Map<String, Object> vMap;
		/**
		 * KV Iterator instance
		 */
		private KVIterator itr;		
		/**
		 * Current key
		 */
		byte[] key;
		
		/**
		 * Only constructor
		 * @param ifMapRequired
		 */
		ObjectMapIteratorImp(boolean ifMapRequired) throws Exception {
			// if value require or not
			vMap = ifMapRequired ? new HashMap<>() : null;
			// build store start key
			byte[] start = ctx.buildKey(DATA_SPACE, smInfo.getStoreCode());
			// build store end key
			byte[] end = ctx.buildKey(DATA_SPACE, smInfo.getStoreCode() + 1);
			// open iterator now
			this.itr = ctx.getKVEngine().iterator(start, end);
		}
		
		@Override
		public boolean hasNext() throws Exception {
			// TODO Auto-generated method stub
			try {
				if (itr.hasNext()) {
					// get the KVEntry
					KVEntry e = itr.next();
					// backup key
					key = e.getKey();
					// if value map required
					if (vMap != null) {
						// clear map
						vMap.clear();
						// get a byte buffer from key
						ByteBuffer buf = ByteBuffer.wrap(key);
						// skip meta info
						buf.position(STORE_DATA_LEN);
						// iterate over sort keys and ID field accessor
						for (FieldMetaInfo fm : keys) {
							// get field accessor
							FieldAccessor fa = ctx.getFieldAccessor(fm.getFldType());
							// get field value
							Object fv = fa.deserialize(buf);
							// put it to map
							vMap.put(fm.getFldName(), fv);
						}
						// get value 
						byte[] val = e.getValue();
						// uncompress value
						val = ctx.uncompress(val);
						// get byte buffer from value
						buf = ByteBuffer.wrap(val);
						// iterate throw field
						while (buf.hasRemaining()) {
							// get field code
							long fcode = buf.getLong();
							// get field meta info
							FieldMetaInfo fm = cMap.get(lw.setVal(fcode));
							// field might have been deleted
							if (fm == null) {
								// we need to skip those bytes
								int pos = buf.position();
								// skip bytes
								ctx.getFieldAccessor(buf.get(pos)).skip(buf);
								continue;
							}
							// get field name
							String fName = fm.getFldName();
							// get field accessor
							FieldAccessor fa = ctx.getFieldAccessor(fm.getFldType());
							// get field value
							Object fv = fa.deserialize(buf);
							// put it in map
							vMap.put(fName, fv);
						}
					}
					return true;
				}
			} catch (Exception e) {
	           throw new RuntimeException(e);
			} 
			// 
			return false;
		}

		@Override
		public Map<String, Object> nextMap() {
			// return value map
			return vMap;
		}

		@Override
		public void delete() throws Exception {
			// delete last return entry
			ctx.getKVEngine().delete(key);
		}

		@Override
		public void close() {
			// clean up resources 
			if (itr != null) {
				itr.close();
				itr = null;
				key = null;
				// clear value map
				if (vMap != null) {
					vMap.clear();
					vMap = null;
				}
			}			
		}
		
	}
	/**
	 * Object iterator implementation
	 * 
	 * @author prasantsmac
	 *
	 */
	final class ObjectIteratorImp implements ObjectIterator<T> {
		/**
	     * KV Iterator
	     */
	    KVIterator itr;
	    /**
	     * Current selected record
	     */
	    T t;  
	    /**
	     * Current key
	     */
	    byte[] key;
		/**
		 * 
		 * @param reverse
		 */
		ObjectIteratorImp(boolean reverse) {
			// TODO Auto-generated method stub
			try {
				// start key and end key
				byte[] start = null, end = null;
				// if not reverse iterator
				if (!reverse) {
					// get start key
					end = ctx.buildKey(DATA_SPACE, smInfo.getStoreCode() + 1);
					// get end key
					start = ctx.buildKey(DATA_SPACE, smInfo.getStoreCode());
				}
				// otherwise
				else {
					// get end key
					end = ctx.buildKey(DATA_SPACE, smInfo.getStoreCode());
					// get start key
					start = ctx.buildKey(DATA_SPACE, smInfo.getStoreCode() + 1);
				}
				// get KV iterator now
				itr = ctx.getKVEngine().iterator(start, end, reverse);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public boolean hasNext() {
			// TODO Auto-generated method stub
			try {
				// if next record exist
				if (itr.hasNext()) {
					// get the entry
					KVEntry e = itr.next();
					// get key reference
					key = e.getKey();
					// instantiate a new object
					t = ((Class<T>) smInfo.getClass()).newInstance();
					// de serialise key first
					deSerializeKey(key, t);
					// uncompress value
					byte[] value = ctx.uncompress(e.getValue());
					// de serialise value now
					deSerialize(value, t);
					// save it to cache in case update is required
					cachePut(new CacheKey(key), new CacheValue(value), true);
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
		public T next() {
			// TODO Auto-generated method stub
			if (t == null)
				throw new NoSuchElementException();
			return t;
		}

		@Override
		public T update() {
			// TODO Auto-generated method stub
			if (t == null)
				throw new NoSuchElementException();
			// delete
			try { delete(t, key); } catch (Exception e) {
				throw new RuntimeException(e);
			}
			return t;			
		}

		@Override
		public T remove() {
			if (t == null)
				throw new NoSuchElementException();
			// delete
			try { delete(t, key); } catch (Exception e) {
				throw new RuntimeException(e);
			}
			return t;
		}

		@Override
		public void close() {
			// TODO Auto-generated method stub
			if (itr != null) {
				itr.close();
				itr = null;
				t = null;
				key = null;				
			}				
		}		
	}
	
	/**
	 * Query Context implementation
	 * @author prasantsmac
	 *
	 * @param <T>
	 */
	final class QueryContextImp implements QueryContext<T> {
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
		 * If this context supporting object iterator
		 */
		boolean iterator = false;
		/**
		 * Current loaded value
		 */
		byte[] value;
		/**
		 * Currently loaded key
		 */
		byte[] key;
		 /**
	     * Value map
	     */
	    Map<String, FieldAccessor> valueMap;
	    /**
	     * Key Map
	     */
	    Map<String, FieldAccessor> keyMap;
	    /**
	     * Help full to prevent auto boxing
	     */
	    final LongWrapper lw = new LongWrapper();
	    /**
	     * Indicate if key bytes are parsed
	     */
	    boolean keyLoaded = false;
	    /**
	     * Indicate if value bytes are parsed or not
	     */
	    boolean valueLoaded = false;
	    
	    /**
	     * Only constructor
	     */
	    QueryContextImp() {
			try {
				// key fields => Field Accessor
				keyMap = fieldMetaToAccessor(keys);
				// value fields => Field Accessor
				valueMap = fieldMetaToAccessor(fMap.values());
				// if version field available
				if (ver != null) {
					// add to value map
					valueMap.put(ver.getFldName(), new IntFieldAccessor());
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
	    }
	    
	    /**
	     * Return Field name to field accessor mapping
	     * @param fldSet
	     * @return
	     */
	    private Map<String, FieldAccessor> fieldMetaToAccessor(Collection<FieldMetaInfo> fldSet) throws Exception 
	    {
	    	// field name => Field Accessor
	        Map<String, FieldAccessor> fieldAcc = new LinkedHashMap<>();
	        // iterate throw field meta set and convert to field accessor
	        for (FieldMetaInfo e : fldSet) {
	        	// get Field Accessor
	    		FieldAccessor fac = ctx.getFieldAccessor(e.getFldType()).newInstance();
	    		// set it to map
	    		fieldAcc.put(e.getFldName(), fac);
	        }            
	        return fieldAcc;
	    }
	    
	    /**
		 * Key bytes holds sort keys and ID. Desrialise the key to map
		 * 
		 * @param key
		 * @param km
		 * @return
		 * @throws Exception
		 */
		private final void keyToMap(byte[] key) throws Exception {
			// get a byte buffer
			ByteBuffer buf = ByteBuffer.wrap(key);
			// skip meta info
			buf.position(STORE_DATA_LEN);
			// iterate over sort keys and ID field accessor
			for (FieldAccessor fa : keyMap.values()) {
				// set the backing buffer
				fa.set(buf);
			}
			// mark that key was loaded
			keyLoaded = true;
		} 
		/**
		 * De serialise object to map
		 * @param val
		 * @return
		 * @throws Exception
		 */
		private final void valueToMap(byte[] val) throws Exception {
			// get a byte buffer
			ByteBuffer buf = ByteBuffer.wrap(val);
			// de serialise version property first
		    deSerializeVersion(null, valueMap, buf);
			// de serialise object		
			while (buf.hasRemaining()) {
				// parse field code
				long fc = parseLong(buf);
				// retrieve corresponding field name
				FieldMetaInfo fn = cMap.get(lw.setVal(fc));
				// field might have been deleted
				if (fn == null) {
					// we need to skip those bytes
					int pos = buf.position();
					// skip bytes
					ctx.getFieldAccessor(buf.get(pos)).skip(buf);
					continue;
				}		
				// put this into result map
				valueMap.get(fn.getFldName()).set(buf);		
			}
			// mark that value was loaded
			valueLoaded = true;
		}
			
		/**
		 * Set key map to target field
		 * @throws Exception
		 */
		private final void keyMapToTarget() throws Exception {
			// get key map iterator
			Iterator<FieldAccessor> faItr = keyMap.values().iterator();
			// get key list iterator
			Iterator<FieldMetaInfo> fmItr = keys.iterator();
			// iterate together
			while (faItr.hasNext() && fmItr.hasNext()) {
				// get field accessor
				FieldAccessor fa = faItr.next();
				// get field meta info
				FieldMetaInfo fm = fmItr.next();
				// set value to the target
				fa.set(t, fm.getField());
			}
		}
		
		/**
		 * Set value map to target
		 * @throws Exception
		 */
		private final void valueMapToTarget() throws Exception {
			// iterate throw map and convert to object
			for (Map.Entry<String, FieldAccessor> e : valueMap.entrySet()) {
				// Get field
				Field fld = fMap.get(e.getKey()).getField();
				// get field accessor
				e.getValue().set(t, fld);
			}
		}
		
	    @Override
		public boolean isSortKey(String fname) {
			// get the sort key field
			FieldMetaInfo skf = keys.size() > 1 ? keys.get(0) : null;
			// if no sort key available
			if (skf == null)
				return false;
			// compare and return
			return fname.equals(skf.getFldName());	
		}

		@Override
		public void seek(Object from, boolean rev) {
			// data space [DATA_SPACE][STORE_CODE_LONG]....=[FIELD_CODE_LONG]...
			try {
				// start key
				byte[] start = null, end = null;
				// if not reverse iterator
				if (!rev) {
					// common end point
					end = ctx.buildKey(DATA_SPACE, smInfo.getStoreCode() + 1);
					// if seek key was provided
					if (from != null) 
						start = buildKeyDataSeek(from);
					else 
						start = ctx.buildKey(DATA_SPACE, smInfo.getStoreCode());				
				} 
				// otherwise
				else {
					// common end point
					end = ctx.buildKey(DATA_SPACE, smInfo.getStoreCode());
					// if seek key was provided
					if (from != null) 
						start = buildKeyDataSeek(from);
					else 
						start = ctx.buildKey(DATA_SPACE, smInfo.getStoreCode() + 1);			
				}
				// get KV iterator now
				itr = ctx.getKVEngine().iterator(start, end, rev);				
			} catch (Exception e) {
				throw new RuntimeException(e);				
			}			
		}

		@Override
		public Object getFieldValue(String fname) {
			// TODO Auto-generated method stub
			try {
				// check if it was a key field
				FieldAccessor fa = keyMap.get(fname);
				// if key fields are not de serialise yet
				if (fa != null && !keyLoaded) {
					// load key to map
					keyToMap(key);
				}
				// if it was a value field
				if (fa == null && (fa = valueMap.get(fname)) != null && !valueLoaded) {
					// uncompress value first
					value = ctx.uncompress(value);
					// value bytes to map
					valueToMap(value);
				}
				// if field doesn't exist in class
				if (fa == null)
					throw new RuntimeException("Field '" + fname + "' not valid for class " + smInfo.getClassName());
				// return value
				return fa.get();				
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public boolean nextRecord() {
			// TODO Auto-generated method stub
			try {
				// if next record exist
				if (itr.hasNext()) {
					// get the entry
					KVEntry e = itr.next();
					// get the key
					key = e.getKey();
					// get the value
					value = e.getValue();
					// mark key was not loaded
					keyLoaded = false;
					// mark value was not loaded
					valueLoaded = false;
					// make t null
					t = null;
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

		@SuppressWarnings("unchecked")
		@Override
		public T currentRecord() {
			if (t == null && (keyLoaded || valueLoaded)) {
				try {
					// create a new instance
					t = ((Class<T>) smInfo.getClass()).newInstance();
					// if no key map was available
					if (!keyLoaded) {
						// de serialise raw key to target
					   deSerializeKey(key, t);
					} else {
						// convert map to field
						keyMapToTarget();
					}
					// if no value map is available
					if (!valueLoaded) {
						// uncompress value first
						value = ctx.uncompress(value);
						// de serialise
						deSerialize(value, t);
					} else {
						// convert map to field
						valueMapToTarget();
					}
					// put it in cache if entry not present already
					cachePut(new CacheKey(key), new CacheValue(value), true);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
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
			try { save(t, key, true); } catch (Exception e) {
				throw new RuntimeException(e);
			}
			return t;
		}
		
		@Override
		public void reset() {
			/**
			 * Clean up resources
			 */
			if (itr != null) {
				itr.close();
				valueMap.clear();
				keyMap.clear();
				itr = null;
				t = null;
				valueMap = keyMap = null;				
				System.out.println(count);
			}
		}		
	}

	@Override
	public void sync() {
		try {
			ctx.getKVEngine().sync();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new RuntimeException(e);
		}		
	}
	
	@Override
	public void close() {
		// TODO Auto-generated method stub		
	}
}
