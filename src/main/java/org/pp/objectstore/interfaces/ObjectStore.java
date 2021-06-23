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
	public abstract T load(T t) throws Exception;
	/**
	 * Store object of type T
	 * @param <T>
	 * @param t
	 * @return
	 * @throws Exception
	 */
	public abstract T store(T t) throws Exception ;
	/**
	 * Update object atomically (if @version present in object). 
	 * Throws StaleObjectException in case version mismatch during update
	 * @param t
	 * @return
	 * @throws StaleObjectException
	 * @throws Exception
	 */
	public abstract T update(T t) throws StaleObjectException, Exception;
	/**
	 * Remove the specified object from store
	 * @param <T>
	 * @param t
	 * @return
	 * @throws Exception
	 */
	public abstract T remove(T t) throws StaleObjectException, Exception;
	/**
	 * Return object iterator
	 * @return
	 * @throws Exception
	 */
	public abstract ObjectIterator<T> iterator() throws Exception;
	/**
	 * Get a reverse iterator
	 * @return
	 * @throws Exception
	 */
	public abstract ObjectIterator<T> revIterator() throws Exception;
	
	/**
	 * Create a query object
	 * @param <T>
	 * @param qry
	 * @param claszz
	 * @return
	 */
	public abstract Query<T> createQuery(String qry);	
	
}
