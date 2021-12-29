package org.pp.objectstore.test;

import static org.pp.storagengine.api.imp.Util.MB;

import java.io.File;
import java.net.URI;

import org.pp.objectstore.ObjectStoreFactory;
/**
 * test base for test classes
 * @author prasantsmac
 *
 */
abstract class AbstractTest {
	// DB root folder
    private static final String dbRoot = "/Users/prasantsmac/Desktop/DBTest";
    // Object store factory
    protected static ObjectStoreFactory factory = null;
          
    /**
     * Delete database directory
     * @throws Exception
     */
    static final void delDb() throws Exception {
		File rootDir = new File(dbRoot);
		if (!rootDir.exists())
			return;
		// First delete all files in a directory
		for (File f : rootDir.listFiles())
			f.delete();
		// ... Then delete root folder
		rootDir.delete();
	}
    
    /**
     * Open object store factory
     * @throws Exception
     */
    static final void openDatabase() throws Exception {
    	if (factory == null) {
	    	// database location
	    	URI uri = new URI("file:" + dbRoot);
	    	// open database
	    	factory = ObjectStoreFactory.open(uri, 50 * MB);
    	}
    }
    
    /**
     * Close database 
     * @throws Exception
     */
    static final void closeDatabase() throws Exception {
    	if (factory != null) {
    		factory.close();
    		factory = null;
    	}
    }   
}
