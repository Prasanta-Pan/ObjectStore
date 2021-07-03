package org.pp.objectstore.test;

import java.io.File;
import java.net.URI;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pp.objectstore.ObjectStoreFactory;
import org.pp.objectstore.interfaces.ObjectStore;
import org.pp.qry.interfaces.ObjectIterator;
import org.pp.qry.interfaces.Query;

public class BasicTest {
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
		factory = ObjectStoreFactory.open(uri);
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
	
	
	@Test
	public void test() throws Exception {
		// Open Object Store
		ObjectStore<Customer> pStore = factory.openStore(Customer.class);

		// store a person Person p =
		Customer c = new Customer("S7966622Z", "Prasanta Pan", 84984827L, 41, Customer.MALE,
				"46 eastwood rd, #01-07 SG-486356", "pan.prasanta@gmail.com", 2.30f);
		pStore.store(c);
		// store another
		c = new Customer("G1321159R", "Maumita Pan", 83995981L, 34, Customer.FEMALE, "46 eastwood rd, #01-07 SG-486356",
				"pan.maumita@gmail.com", 5.30f);
		pStore.store(c);
		// store one more
		c = new Customer("G1879979R", "Moana Pan", 83995981L, 3, Customer.FEMALE, "46 eastwood rd, #01-07 SG-486356",
				"pan.moana@gmail.com", 7.30f);
		pStore.store(c);
		
        // generate some orders
		randomOrders(15);
		// Get iterator
		ObjectIterator<Customer> itr = pStore.iterator();
		while (itr.hasNext()) {
			Customer p = itr.next();
			System.out.println(p);
		}
		System.out.println("##################################################################################");
		// brows all orders
		ObjectIterator<Order> orderItr = factory.openStore(Order.class).iterator();
		while (orderItr.hasNext()) {
			Order o = orderItr.next();
			System.out.println(o);
		}
		System.out.println("##################################################################################");
		// filter order for Babu
		String qry = "email = ? && noOrders > ? && total < ?";
		Query<Order> query = factory.openStore(Order.class).createQuery(qry);
		// set parameters
		orderItr = query.setParam("pan.moana@gmail.com")
						.setParam(15)
						.setParam(300)
						.iterator();
		// iterate now
		while (orderItr.hasNext()) {
			Order o = orderItr.next();
			System.out.println(o);
		}			
	}
	/**
	 * generate orders
	 * @param num
	 * @throws Exception
	 */
	private void randomOrders(int num) throws Exception {
		ThreadLocalRandom ran = ThreadLocalRandom.current();
		for (int i = 0; i < num; i++) {
			String email = emails[ran.nextInt(0, emails.length)];
			int noOrder = noOrders[ran.nextInt(0, noOrders.length)];
			float price = prices[ran.nextInt(0, prices.length)];
			int total = totals[ran.nextInt(0, totals.length)];
			// create order object
			Order order = new Order(email, noOrder, price, total, System.currentTimeMillis());
			factory.openStore(Order.class).store(order);			
		}
	}
}
