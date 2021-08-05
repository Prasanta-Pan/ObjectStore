package org.pp.objectstore.interfaces;

import java.util.Map;

public interface PurgeStoreHandler {
	/**
	 * To handle existing data in map form
	 * @param map
	 */
     public void handle(Map<String, Object> map);
}
