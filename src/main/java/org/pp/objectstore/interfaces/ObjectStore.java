package org.pp.objectstore.interfaces;

import org.pp.qry.interfaces.ObjectIterator;
import org.pp.qry.interfaces.Query;

public interface ObjectStore<T> {
	/**
	 * Load object with specific type
	 * @param <T>
	 * @param t
	 * @return
	 * @throws Exception
	 */
	public T load(T t) throws Exception;
	
	/**
	 * The default load method always cache the return object.
	 * This version of load method allow user more control on caching behaviour
	 * Passing cache=false will instruct object store not to cache the return object
	 * 
	 * @param t
	 * @param cache
	 * @return
	 * @throws Exception
	 */
	public T load(T t, boolean cache) throws Exception;
	
	/**
	 * Store object of type T
	 * @param <T>
	 * @param t
	 * @return
	 * @throws Exception
	 */
	public T store(T t) throws Exception ;
	
	/**
	 * The default store method always cache the return object.
	 * This version of store method allow user more control on caching behaviour
	 * Passing cache=false will instruct object store not to cache the return object
	 * @param t
	 * @param cache
	 * @return
	 * @throws Exception
	 */
	public T store(T t, boolean cache) throws Exception ;
	/**
	 * Remove the specified object from store
	 * @param <T>
	 * @param t
	 * @return
	 * @throws Exception
	 */
	public T remove(T t) throws Exception;
	
	/**
	 * Return object iterator
	 * @return
	 * @throws Exception
	 */
	public ObjectIterator<T> iterator() throws Exception;
	
	/**
	 * Get a reverse iterator
	 * @param reverse TODO
	 * @return
	 * @throws Exception
	 */
	public ObjectIterator<T> iterator(boolean reverse) throws Exception;
	
	/**
	 * Create a query object
	 * @param <T>
	 * @param qry
	 * @param claszz
	 * @return
	 */
	public Query<T> createQuery(String qry);
	
}
