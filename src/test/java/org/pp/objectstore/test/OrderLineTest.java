package org.pp.objectstore.test;

import static org.pp.storagengine.api.imp.Util.MB;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pp.objectstore.ObjectStoreFactory;
import org.pp.objectstore.interfaces.ObjectStore;
import org.pp.qry.interfaces.ObjectIterator;

import static org.pp.objectstore.test.OrderLine.dateToMillis;

public class OrderLineTest {
	// DB root folder
    private static final String dbRoot = "/Users/prasantsmac/Desktop/DBTest";
    // Object store factory
    private static ObjectStoreFactory factory = null;
    // order line list
    private static final List<OrderLine> oList = new ArrayList<>();
    // max number of records to create
    private static final int numOfRecords = 30;
    
    
    
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
	
	// create random order line
	@Test()
	public void test1() throws Exception {
		System.out.println(
				"########################################## Create Order Line ########################################");
		ObjectStore<OrderLine> os = factory.openStore(OrderLine.class);
		for (int i = 0; i < numOfRecords; i++) {
			OrderLine ol = OrderLine.genOrderLine();
			os.store(ol);
			oList.add(ol);
		}
	}

	// 
	@Test()
	public void test2() throws Exception {
		System.out.println(
				"########################################## Iterate Order Line ########################################");
		ObjectIterator<OrderLine> itr = factory.openStore(OrderLine.class).iterator();
		while (itr.hasNext()) {
			System.out.println(itr.next());
		}
	}

	// 
	@Test()
	public void test3() throws Exception {
		System.out.println(
				"########################################## Query Order Line ########################################");
		// order line between dates
		List<Long> dates = Arrays.asList(dateToMillis("2018-09-29"), dateToMillis("2020-05-11"));		
		ObjectIterator<OrderLine> itr = factory.openStore(OrderLine.class)
									  			.createQuery("orderDate between ? && customerEmail = 'pan.moana@gmail.com'")
									  			.setParam(dates)
									  			.iterator();
		while (itr.hasNext()) {
			System.out.println(itr.next());
		}
	}

	//
	@Test()
	public void test4() throws Exception {
		System.out.println(
				"########################################## Query Order Line reverse ########################################");
		// order line between dates
		List<Long> dates = Arrays.asList(dateToMillis("2018-09-29"), dateToMillis("2020-05-11"));
		ObjectIterator<OrderLine> itr = factory.openStore(OrderLine.class)
												.createQuery("orderDate between ? && customerEmail = 'pan.moana@gmail.com'")
												.setReverseOrder(true)
												.setParam(dates)
												.iterator();
		while (itr.hasNext()) {
			System.out.println(itr.next());
		}
	}
}
