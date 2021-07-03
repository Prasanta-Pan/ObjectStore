package org.pp.objectstore.interfaces;

import org.pp.qry.interfaces.ObjectIterator;
import org.pp.qry.interfaces.Query;

public interface ObjectStore<T> {
	/**
	 * Add a field to store with default value.
	 * Please ensure the new field also present in corresponding class
	 * @param name
	 * @param defaultVal
	 */
	public void addField(String name, Object defaultVal);
	
	/**
	 * Rename existing field to new name.
	 * Ensure similar rename is done in corresponding class field as well
	 * @param exsistingName
	 * @param newName
	 */
	public void renameField(String exsistingName, String newName);
	
	/**
	 * Remove an existing field from store.
	 * Ensure similar removal of field also made in class level
	 * @param exsistingName
	 */
	public void removeField(String exsistingName);
	
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
	 * Update object atomically (if @version present in object). 
	 * Throws StaleObjectException in case version mismatch during update
	 * @param t
	 * @return
	 * @throws StaleObjectException
	 * @throws Exception
	 */
	public T update(T t) throws StaleObjectException, Exception;
	
	/**
	 * The default update method always cache the return object.
	 * This version of update method allow user more control on caching behaviour
	 * Passing cache=false will instruct object store not to cache the return object
	 * @param t
	 * @param cache
	 * @return
	 * @throws StaleObjectException
	 * @throws Exception
	 */
	public T update(T t, boolean cache) throws StaleObjectException, Exception;
	
	/**
	 * Remove the specified object from store
	 * @param <T>
	 * @param t
	 * @return
	 * @throws Exception
	 */
	public T remove(T t) throws StaleObjectException, Exception;
	
	/**
	 * Return object iterator
	 * @return
	 * @throws Exception
	 */
	public ObjectIterator<T> iterator() throws Exception;
	
	/**
	 * Get a reverse iterator
	 * @return
	 * @throws Exception
	 */
	public ObjectIterator<T> revIterator() throws Exception;
	
	/**
	 * Create a query object
	 * @param <T>
	 * @param qry
	 * @param claszz
	 * @return
	 */
	public Query<T> createQuery(String qry);	
	
}
