package org.pp.objectstore.test;

import static org.pp.storagengine.api.imp.Util.MB;

import java.io.File;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pp.objectstore.ObjectStoreFactory;
import org.pp.objectstore.interfaces.ObjectStore;
import org.pp.qry.interfaces.ObjectIterator;

public class OrderTest {
	// DB root folder
    private static final String dbRoot = "/Users/prasantsmac/Desktop/DBTest";
    // Object store factory
    private static ObjectStoreFactory factory = null;
    // 
    private static final String[] emails = new String[] 
    		{"pan.prasanta@gmail.com", "pan.maumita@gmail.com", "pan.moana@gmail.com"};
    //
    private static final int[] noOrders = new int[] { 5, 10, 15, 20, 31, 50 };
    //
    private static final float[] prices = new float[] { 5.68f, 6.87f, 7.89f, 10.56f, 15.30f };
    //
    private static final int[] totals = new int[] { 30, 50, 100, 150, 300, 500 };
    // 
    private static final String[] dates = new String[] { "2018-09-18", "2018-09-21", "2018-09-29", "2020-09-18", "2020-11-07", "2020-05-11" };
    // 
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    // order list
    private static final List<Order> oList = new ArrayList<>();
    // number of random order to create
    private static int randOrder = 15;
    // number of random load
    private static int randLoad = 5;
    // number of random delete
    private static int randDel = 5;
    // number of random update
    private static int randUpd = 5;
    
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
	
	// create random order
	@Test()
	public void test1() throws Exception {
		System.out.println("########################################## Create Order ########################################");
		ObjectStore<Order> os = factory.openStore(Order.class);
		ThreadLocalRandom ran = ThreadLocalRandom.current();
		for (int i = 0; i < randOrder; i++) {
			String email = emails[ran.nextInt(0, emails.length)];
			int noOrder = noOrders[ran.nextInt(0, noOrders.length)];
			float price = prices[ran.nextInt(0, prices.length)];
			int total = totals[ran.nextInt(0, totals.length)];
			long oDate = dateToMillis(dates[ran.nextInt(0, dates.length)]);
			// create order object
			Order order = new Order(email, noOrder, price, total, oDate);
			os.store(order);
			oList.add(order);
		}
	}
	
	@Test
	public void test2() throws Exception {
		System.out.println("########################################## Forward Iterator ########################################");
		// open iterator
	    ObjectIterator<Order> itr = factory.openStore(Order.class).iterator();
	    while (itr.hasNext()) {
	    	System.out.println(itr.next());
	    }
		
	}
	// iterate orders
	@Test
	public void test9() throws Exception {
		System.out.println(
				"########################################## Query Iterator ########################################");
		// open store
		ObjectStore<Order> os = factory.openStore(Order.class);
		// create set parameter
		Set<Long> in = new HashSet<>(Arrays.asList(dateToMillis("2020-11-07"), dateToMillis("2018-09-21"), dateToMillis("2020-05-11")));
		// create query
		ObjectIterator<Order> itr = os.createQuery("email = 'pan.moana@gmail.com' && orderDate nin ?")
									  .setReverseOrder(true)
				             		  .setParam(in)
				             		  .iterator();
		// 
		while (itr.hasNext()) {
			System.out.println(itr.next());
		}		
	}
	// reverse iterator
	@Test
	public void test3() throws Exception {
		System.out.println("########################################## Reverse Iterator ########################################");
		// open iterator
	    ObjectIterator<Order> itr = factory.openStore(Order.class).iterator(true);
	    while (itr.hasNext()) {
	    	System.out.println(itr.next());
	    }
	}
	// load some random data
	@Test
	public void test4() throws Exception {
		System.out.println("########################################## Load Order ########################################");
		ObjectStore<Order> os = factory.openStore(Order.class);
		ThreadLocalRandom ran = ThreadLocalRandom.current();
		for (int i = 0; i < randLoad; i++) {
			Order order = oList.get(ran.nextInt(0, oList.size()));
			Order tmp = new Order(order.getEmail(), order.getOrderDate(), order.getOrderId());
			System.out.println("Before Load: " + tmp);
			tmp = os.load(tmp);
			System.out.println("After Load: " + tmp);
		}
		
	}
	
	// update some data
	@Test
	public void test5() throws Exception {
		System.out.println(
				"########################################## Update Order ########################################");
		ObjectStore<Order> os = factory.openStore(Order.class);
		ThreadLocalRandom ran = ThreadLocalRandom.current();
		for (int i = 0; i < randUpd; i++) {
			Order order = oList.remove(ran.nextInt(0, oList.size()));
			Order tmp = new Order(order.getEmail(), order.getOrderDate(), order.getOrderId());
			tmp = os.load(tmp);
			System.out.println("Before Update: " + tmp);
			int noOrder = noOrders[ran.nextInt(0, noOrders.length)];
			tmp.setNoOrders(noOrder);
			float price = prices[ran.nextInt(0, prices.length)];
			tmp.setUnitPrice(price);
			int total = totals[ran.nextInt(0, totals.length)];
			tmp.setTotal(total);
			//os.update(tmp);
			oList.add(tmp);
			System.out.println("After Update: " + tmp);
		}

	}
	
	// Iterate after order
	@Test
	public void test6() throws Exception {
		System.out.println("########################################## Iterator after update ########################################");
		// open iterator
	    ObjectIterator<Order> itr = factory.openStore(Order.class).iterator();
	    while (itr.hasNext()) {
	    	System.out.println(itr.next());
	    }
	}
	
	@Test
	public void test7() throws Exception {
		System.out.println("########################################## Delete some order ########################################");
		ObjectStore<Order> os = factory.openStore(Order.class);
		ThreadLocalRandom ran = ThreadLocalRandom.current();
		
		for (int i = 0; i < randDel; i++) {
			Order order = oList.remove(ran.nextInt(0, oList.size()));
			Order tmp = new Order(order.getEmail(), order.getOrderDate(), order.getOrderId());
			tmp = os.load(tmp);
			System.out.println("Before delete: " + tmp);
			os.remove(tmp);
		}
	}

	// Iterate after delete
	@Test
	public void test8() throws Exception {
		System.out.println(
				"########################################## Iterator after delete ########################################");
		// open iterator
		ObjectIterator<Order> itr = factory.openStore(Order.class).iterator();
		while (itr.hasNext()) {
			System.out.println(itr.next());
		}
	}	
	
	/**
	 * Convert String date to millis
	 * @param date
	 * @return
	 */
	private static long dateToMillis(String date) throws Exception {
		return sdf.parse(date).getTime();
	}
}
