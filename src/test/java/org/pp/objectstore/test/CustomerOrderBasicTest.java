package org.pp.objectstore.test;

import static org.pp.objectstore.test.domain.CustomerOrder.generate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.pp.objectstore.interfaces.ObjectStore;
import org.pp.objectstore.test.domain.CustomerOrder;
import org.pp.qry.interfaces.ObjectIterator;

/**
 * Basic test intent to test basic API's.
 * Like create/delete/update/load and
 * iterator API's
 * @author prasantsmac
 *
 */
@DisplayName("Basic API test.Happy flow :)")
@TestMethodOrder(OrderAnnotation.class)
public final class CustomerOrderBasicTest extends AbstractTest {
	// indicate if closing of DB is required after each test
	private boolean alwaysCloseDb = false;
	/**
	 * To hold the list of order
	 */
	private static final NavigableSet<CustomerOrder> orderSet = new TreeSet<>();
	
	/**
     * Delete entire database after all tests are over
     * @throws Exception
     */
	@AfterAll
	public static void cleanUp() throws Exception {
		delDb();		
	}
	
	/**
	 * In case due to fatal crash database was not deleted
	 * @throws Exception
	 */
	@BeforeAll
	public static void init() throws Exception {
		delDb();		
	}
	
	/**
	 * Open database before each test
	 * @throws Exception
	 */
	@BeforeEach
	public void openDb() throws Exception {
		openDatabase();
	}
	
	/**
	 * Close database after each test
	 * @throws Exception
	 */
	@AfterEach
	public void closeDb() throws Exception {
		// make sync
		factory.sync();
		// close db now
		if (alwaysCloseDb)
			closeDatabase();
	}
	
	/**
	 * Create records
	 * @param tot
	 * @throws Exception
	 */
	private void createRecords(int tot) throws Exception {
		// open customer order store
		ObjectStore<CustomerOrder> coStore = factory.openOrCreateStore(CustomerOrder.class);
		// create 15 random orders
		for (int i = 0; i < tot; i++) {
			// generate an order
			CustomerOrder co = generate();
			// store it
			coStore.store(co);
			// add to set
			orderSet.add(co);
		}
	}
		
	/**
	 * Read and verify records
	 * @param print
	 * @throws Exception
	 */
	private void readRecords(boolean print) throws Exception {
		// open customer order store
		ObjectStore<CustomerOrder> coStore = factory.openOrCreateStore(CustomerOrder.class);
		// customer order iterator (Storage Engine)
		ObjectIterator<CustomerOrder> itr = null;
		try {
			// storage iterator
			itr = coStore.iterator();
			// tree iterator
			Iterator<CustomerOrder> treeItr = orderSet.iterator();
			// to count records
			int storageCount = 0, treeCount = 0;
			for (; itr.hasNext() && treeItr.hasNext(); storageCount++, treeCount++) {
				// get next order from the store
				CustomerOrder co = itr.next();
				// next order from TreeSet
				CustomerOrder cot = treeItr.next();
				if (!co.equals(cot))
					throw new RuntimeException("Customer order don't match");
				// print it
				if (print)
					System.out.println(co);
			}
			// verify count
			if (storageCount != treeCount)
				throw new RuntimeException("Record count doesn't match");
			// print total number of records
			System.out.println("Total records read : " + storageCount);
		} finally {
			itr.close();
		}
	}
	
	@Test
	@DisplayName("Create few records only")
	@Order(1)
	public void createFewRecords() throws Exception {
		createRecords(15);
	}
	
	@Test
	@DisplayName("Read few records now")
	@Order(2)
	public void readFewRecords() throws Exception {
		readRecords(true);
	}
	
	@Test
	@DisplayName("Create 10K records")
	@Order(3)
	public void create10kRecords() throws Exception {
		// open customer order store
		createRecords(10000);
	}
	
	@Test
	@DisplayName("Read 10K records")
	@Order(4)
	public void read10kRecords() throws Exception {
		readRecords(false);		
	}
	
	@Test
	@DisplayName("Random updates of records(1K)")
	@Order(5)
	public void randomUpdates() throws Exception {
		// shuffle set of objects
		List<CustomerOrder> shuflList = new ArrayList<>(orderSet);
		// shuffle now
		Collections.shuffle(shuflList);
		// open customer order store
		ObjectStore<CustomerOrder> coStore = factory.openOrCreateStore(CustomerOrder.class);
		// start updating 1K record only
		for (int i = 0; i < 1000; i++) {
			// get record to update
			CustomerOrder co = shuflList.get(i);
			// random updates of some fields
			co.update();
			// update to store
			coStore.store(co);			
		}
	}
	
	@Test
	@DisplayName("Read and verify records after update")
	@Order(6)
	public void verifyRecords() throws Exception {
		readRecords(false);		
	}
	
	@Test
	@DisplayName("Random delete of records(1K)")
	@Order(7)
	public void randomDelete() throws Exception {
		// shuffle set of objects
		List<CustomerOrder> shuflList = new ArrayList<>(orderSet);
		// shuffle now
		Collections.shuffle(shuflList);
		// open customer order store
		ObjectStore<CustomerOrder> coStore = factory.openOrCreateStore(CustomerOrder.class);
		// start updating 1K record only
		for (int i = 0; i < 1000; i++) {
			// get record to update
			CustomerOrder co = shuflList.get(i);
			// remove from store
			coStore.remove(co);	
			// remove from set
			orderSet.remove(co);
		}
	}
	
	@Test
	@DisplayName("Read and verify records after delete")
	@Order(8)
	public void verifyAfterDelete() throws Exception {
		readRecords(false);		
	}
	
	@Test
	@DisplayName("Random load of records(1K)")
	@Order(9)
	public void randomLoad() throws Exception {
		// shuffle set of objects
		List<CustomerOrder> shuflList = new ArrayList<>(orderSet);
		// shuffle now
		Collections.shuffle(shuflList);
		// open customer order store
		ObjectStore<CustomerOrder> coStore = factory.openOrCreateStore(CustomerOrder.class);
		// start updating 1K record only
		for (int i = 0; i < 1000; i++) {
			// get record to update
			CustomerOrder co = shuflList.get(i);
			// create a new Customer Record
			CustomerOrder newCo = new CustomerOrder(co.getCustomerEmail(), co.getOrderDate(), co.getOrderId());
			// load it from store
			coStore.load(newCo);	
			// objects must match
			if (!co.equals(newCo))
				throw new RuntimeException("Objects dont match");			
		}
	}
	
	@Test
	@DisplayName("Reverse iterator test")
	@Order(10)
	public void reversIterator() throws Exception {
		// get descending set
		NavigableSet<CustomerOrder> descSet = orderSet.descendingSet();
		// open customer order store
		ObjectStore<CustomerOrder> coStore = factory.openOrCreateStore(CustomerOrder.class);
		// customer order iterator (Storage Engine)
		ObjectIterator<CustomerOrder> itr = null;
		try {
			// storage iterator
			itr = coStore.iterator(true);
			// tree iterator
			Iterator<CustomerOrder> treeItr = descSet.iterator();
			// to count records
			int storageCount = 0, treeCount = 0;
			for (; itr.hasNext() && treeItr.hasNext(); storageCount++, treeCount++) {
				// get next order from the store
				CustomerOrder co = itr.next();
				// next order from TreeSet
				CustomerOrder cot = treeItr.next();
				if (!co.equals(cot))
					throw new RuntimeException("Customer order don't match");
			}
			// verify count
			if (storageCount != treeCount)
				throw new RuntimeException("Record count doesn't match");
			// print total number of records
			System.out.println("Total records read : " + storageCount);
		} finally {
			itr.close();
		}
	}
	
	@Test
	@DisplayName("Update while iterating test(10K)")
	@Order(11)
	public void iteratorUpdate() throws Exception {
		// open customer order store
		ObjectStore<CustomerOrder> coStore = factory.openOrCreateStore(CustomerOrder.class);
		// customer order iterator (Storage Engine)
		ObjectIterator<CustomerOrder> itr = null;
		try {
			// storage iterator
			for (itr = coStore.iterator(); itr.hasNext();) {
				// get next order from the store
				CustomerOrder co = itr.next();
				// random updates of some fields
				co.update();
				// update record now
				itr.update();
				// update set as well
				orderSet.remove(co);
				// add again
				orderSet.add(co);
			}
			
		} finally {
			itr.close();
		}		
	}
	
	@Test
	@DisplayName("Read and verify records after iterator update")
	@Order(12)
	public void verifyAfterIteratorUpdate() throws Exception {
		readRecords(false);		
	}
	
	@Test
	@DisplayName("Delete while iterating test(1K)")
	@Order(13)
	public void iteratorDelete() throws Exception {
		// open customer order store
		ObjectStore<CustomerOrder> coStore = factory.openOrCreateStore(CustomerOrder.class);
		// customer order iterator (Storage Engine)
		ObjectIterator<CustomerOrder> itr = null;
		// to end deletion exacty after 1K
		int count = 0;
		try {
			// storage iterator
			for (itr = coStore.iterator(); itr.hasNext() && count < 1000; count++) {
				// get next order from the store
				CustomerOrder co = itr.next();
				// remove from store
				itr.remove();
				// remove from set as well
				orderSet.remove(co);				
			}
			
		} finally {
			itr.close();
		}		
	}	
	
	@Test
	@DisplayName("Read and verify records after iterator delete")
	@Order(14)
	public void verifyAfterIteratorDelete() throws Exception {
		readRecords(false);		
	}
	
}
