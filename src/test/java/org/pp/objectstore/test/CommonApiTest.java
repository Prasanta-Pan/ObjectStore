package org.pp.objectstore.test;

import static org.pp.storagengine.api.imp.Util.MB;

import java.io.File;
import java.net.URI;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pp.objectstore.ObjectStoreFactory;
import org.pp.objectstore.interfaces.FieldRenameHandler;
import org.pp.objectstore.interfaces.StoreDataHandler;

public class CommonApiTest {
	// DB root folder
    private static final String dbRoot = "/Users/prasantsmac/Desktop/DBTest";
    // Object store factory
    private static ObjectStoreFactory factory = null;
    
	@AfterClass
	public static void cleanUp() throws Exception {
		factory.close();
		delDb();		
	}
	
	@BeforeClass
	public static void init() throws Exception {
		// delete database if exist
		delDb();
		// database location
		URI uri = new URI("file:" + dbRoot);
		// open database
		factory = ObjectStoreFactory.open(uri, 50 * MB);
	}

	// Delete directory
	private static void delDb() throws Exception {
		File rootDir = new File(dbRoot);
		if (!rootDir.exists())
			return;
		// First delete all files in a directory
		for (File f : rootDir.listFiles())
			f.delete();
		// ... Then delete root folder
		rootDir.delete();
	}
	
	@Test()
	public void test() throws Exception {
		/**
		 * Redefine existing store identified by class name
		 */
		factory.migrateStore(OrderLine.class, new StoreDataHandler<OrderLine>() {
			@Override
			public OrderLine handle(Map<String, Object> map) {
				// order date
				long orderDate = (long) map.get("orderDate");
				// product id
				int productId = (int) map.get("productName");
				// line id
				long lineId = (long) map.get("lineId");
				// return new instance
				return new OrderLine(orderDate, productId, lineId);				
			}
		});
		
		/**
		 * Rename existing field of a store to new name
		 */
		factory.renameStoreFields(OrderLine.class, new FieldRenameHandler() {
			
			@Override
			public String newFieldName(String existingFieldName) {
				// TODO Auto-generated method stub
				return null;
			}
		});
		
		
	}
	
}
