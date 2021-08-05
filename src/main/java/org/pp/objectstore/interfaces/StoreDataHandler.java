package org.pp.objectstore.interfaces;

import java.util.Map;

/**
 * To handle existing store data
 * 
 * @author prasantsmac
 *
 * @param <T>
 */
public interface StoreDataHandler<T> {
	/**
	 * Map existing data (Map) to new Object Structure t
	 * 
	 * @param t
	 * @param map
	 */
	public T handle(Map<String, Object> map);

}
