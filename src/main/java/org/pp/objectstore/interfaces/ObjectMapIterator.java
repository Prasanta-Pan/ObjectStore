package org.pp.objectstore.interfaces;

import java.util.Map;
/**
 * To iterate over the object store and get corresponding map
 * @author prasantsmac
 *
 */
public interface ObjectMapIterator {
	/**
	 * Indicate if next map available or not
	 * @return
	 */
    public boolean hasNext() throws Exception ;
    /**
     * return next map if available
     * @return
     */
    public Map<String,Object> nextMap();
    /**
     * Delete the last return map from the store
     */
    public void delete() throws Exception;
    /**
     * Close the map iterator
     */
    public void close();
}
