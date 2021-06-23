package org.pp.objectstore;

import static org.pp.objectstore.Util.buildKey;
import static org.pp.objectstore.Util.deSerialize;
import static org.pp.objectstore.Util.higherValue;
import static org.pp.objectstore.Util.init;
import static org.pp.objectstore.Util.serialize;
import static org.pp.objectstore.Util.setId;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.pp.objectstore.interfaces.FieldAccessor;
import org.pp.objectstore.interfaces.GlobalObjectStoreContext;
import org.pp.objectstore.interfaces.ObjectStore;
import org.pp.objectstore.interfaces.StaleObjectException;
import org.pp.qry.QueryImp;
import org.pp.qry.interfaces.ObjectIterator;
import org.pp.qry.interfaces.Query;
import org.pp.qry.interfaces.QueryContext;
import org.pp.storagengine.api.KVEntry;
import org.pp.storagengine.api.KVIterator;

class ObjectStoreImp<T> implements ObjectStore<T> {
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
	 * 
	 * @param clazz
	 */
	ObjectStoreImp(String cname, Class<T> clazz, GlobalObjectStoreContext ctx) {
		this.clazz = clazz;
		this.ctx = ctx;	
		this.cname = cname;
		fMap = new HashMap<>();
		// get if id generation required not
		AtomicBoolean genIdCheck = new AtomicBoolean(false);
		// retrieve field info 
		keys = init(clazz, fMap, genIdCheck);
		this.genId = genIdCheck.get();		
	}	
	
	@Override
	public T store(T t) throws Exception {
		return save(t, null);
	}
	// add + update
	private T save(T t, byte[] key) throws Exception {
		// do null check first
		if (t == null)
			throw new NullPointerException();
		// if key was provided
		if (key == null) {
			// check if id generation was required
			if (genId)
				setId(keys.get(keys.size() - 1), t);
			// build key
			key = buildKey(cname, keys, t);
		}
		// serialise object
		byte[] value = serialize(fMap, t);
		// persist
		ctx.getKVEngine().put(key, value);
		return t;
	}

	@Override
	public T load(T t) throws Exception {
		// do null check first
		if (t == null)
			throw new NullPointerException();
		// build key first
		byte[] key = buildKey(cname, keys, t);
		// get the corresponding value
		KVEntry kvEntry = ctx.getKVEngine().get(key);
		// if found
		if (kvEntry != null)
			deSerialize(kvEntry.getValue(), fMap, t);
		// return
		return kvEntry != null ? t : null;
	}

	@Override
	public T remove(T t) throws Exception {
		// do null check first
		if (t == null)
			throw new NullPointerException();
		return delete(t, null);		
	}

	@Override
	public T update(T t) throws StaleObjectException, Exception {
		return null;
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
		   key = buildKey(cname, keys, t);
		// remove from the KV store
		ctx.getKVEngine().delete(key);
		return t;
	}
	
	@Override
	public ObjectIterator<T> iterator() throws Exception {
		// build key from the collection name
		byte[] start = buildKey(cname);
		// build higher key
		byte[] end = buildKey(higherValue(cname));
		// return iterator
		return new ObjectIteratorImp(ctx.getKVEngine().iterator(start, end));
	}
	
	@Override
	public ObjectIterator<T> revIterator() throws Exception {
		// TODO Auto-generated method stub
		return null;
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
	 * Object iterator implementation
	 * @author prasantsmac
	 *
	 */
	private final class ObjectIteratorImp implements ObjectIterator<T> {
		/**
         * KV Iterator
         */
        private KVIterator itr;
        /**
         * Current selected record
         */
        private T t;  
        /**
         * Current key
         */
        private byte[] key;
        /**
         * Iterator close indicator
         */
        private boolean close = false;
       
        
        ObjectIteratorImp (KVIterator itr) {
        	this.itr = itr;
        }
        
		@Override
		public boolean hasNext() {
			// check iterator status
			status();
			try {
				// if no next record
				if (!itr.hasNext()) { close(); return false; }
				// get the entry
				KVEntry entry = itr.next();
				// store current key
				key = entry.getKey();
				// instantiate a new object of type
				t = clazz.newInstance();
				// De-serialise
				deSerialize(entry.getValue(), fMap, t);
			} catch (Exception e) {
				close();
				throw new RuntimeException(e);
			}
			return true;
		}

		@Override
		public T next() {
			return t;
		}

		@Override
		public T update() {
			if (t == null)
				throw new NullPointerException();
			// update
			try { save(t, key); } catch (Exception e) {
				throw new RuntimeException(e);
			}
			return t;
		}

		@Override
		public T remove() {
			if (t == null)
				throw new NullPointerException();
			// delete
			try { delete(t, key); } catch (Exception e) {
				throw new RuntimeException(e);
			}
			return t;
		}

		@Override
		public void close() {
			if (!close) {
				itr.close();
				close = true;
				key = null;
				t = null;
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
        private KVIterator itr;
        /**
         * Current selected record
         */
        private T t;  
        /**
         * Current key
         */
        private byte[] key;
        /**
         * Count number of iteration
         */
        private int count;
        /**
         * Reset indicator
         */
		private boolean reset = false;
		
		@Override
		public boolean isSortKey(String fname) {
			// get sort key field now			
			return keys.get(0).getField().getName().equals(fname) ? true : false;			
		}

		@Override
		public void seek(Object from, boolean rev) {
			try {
				// start key
				byte[] key = null;
				// if start key was not provide
				if (from == null)
					key = buildKey(cname);
				else 
					key = buildKey(cname, keys.get(0), from);
				// where to stop
				byte[] end =  buildKey(higherValue(cname));
				// get KV iterator now
				itr = ctx.getKVEngine().iterator(key, end);				
			} catch (Exception e) {
				throw new RuntimeException(e);				
			}			
		}
		
		@Override
		public Object getFieldValue(String fname) {
			FieldAccessor fa = fMap.get(fname);
			// if not exist, throw exception
			if (fa == null)
				throw new RuntimeException("Field '" + fname + "' not valid for class " + clazz);
			// get value now
			try {
				return fa.get(t);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public boolean nextRecord() {
			try {
				// if no next record
				if (!itr.hasNext())
					return false;
				// get the entry
				KVEntry entry = itr.next();
				// store current key
				key = entry.getKey();
				// instantiate new object
				t = clazz.newInstance();
				deSerialize(entry.getValue(), fMap, t);	
				count++;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			return true;
		}

		@Override
		public T currentRecord() { return t; }

		@Override
		public T deleteCurrent() {
			if (t == null)
				throw new NullPointerException();
			// delete
			try { delete(t, key); } catch (Exception e) {
				throw new RuntimeException(e);
			}
			return t;
		}

		@Override
		public T updateCurrent() {
			if (t == null)
				throw new NullPointerException();
			// update
			try { save(t, key); } catch (Exception e) {
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
