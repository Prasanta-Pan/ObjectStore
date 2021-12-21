package org.pp.objectstore.test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.pp.objectstore.interfaces.ObjectStore;
import org.pp.objectstore.test.domain.CustomerOrder;
import org.pp.qry.interfaces.ObjectIterator;

import static org.pp.objectstore.test.Util.stringDateToMillis;
import static org.pp.objectstore.test.domain.CustomerOrder.generate;
import static org.pp.objectstore.test.Util.DEACTIVE;
import static org.pp.objectstore.test.Util.ACTIVE;

@TestMethodOrder(OrderAnnotation.class)
public class CustomerOrderQryTest extends AbstractTest {
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
	
	@Test
	@Order(1)
	public void addSomeCustomers() throws Exception {
		// open customer order store
		ObjectStore<CustomerOrder> coStore = factory.openOrCreateStore(CustomerOrder.class);
		// create 15 random orders
		for (int i = 0; i < 15; i++) {
			// generate an order
			CustomerOrder co = generate();
			// store it
			coStore.store(co);
			// add to set
			orderSet.add(co);
		}
		// print
		print(orderSet);
	}
	
	/**
	 * Print collection of objects
	 * @param list
	 */
	private void print(Collection<?> col) {
		for (Object o: col)
			System.out.println(o);
	}
	
	@Test
	@Order(2)
	public void findCustByDateRange() throws Exception {
		// open customer order store
		ObjectStore<CustomerOrder> custStore = factory.openOrCreateStore(CustomerOrder.class);
		// create some query
		String qry = "customerEmail = ? && orderDate between ? && (totalOrder > ? || numOfUnits > ?)";
		// prepare order date range
		List<Long> dateRange = Arrays.asList(stringDateToMillis("2018-09-18"), stringDateToMillis("2020-05-11"));
		// return filtered list of customer order
		List<CustomerOrder> orList = custStore.createQuery(qry)
		         								.setParam(1, "pan.prasanta@gmail.com")
										         .setParam(2, dateRange)
										         .setParam(3, 100)
										         .setParam(4, 5)
										         .list();
		// print the list now
		print(orList);
	}
	
	@Disabled
	public void updCustByDateRange() throws Exception {
		// open customer order store
		ObjectStore<CustomerOrder> custStore = factory.openOrCreateStore(CustomerOrder.class);
		// create some query
		String qry = "customerEmail = ? && orderDate between ? && (totalOrder > ? || numOfUnits > ?)";
		// prepare order date range
		List<Long> dateRange = Arrays.asList(stringDateToMillis("2018-09-18"), stringDateToMillis("2020-05-11"));
		// return filtered list of customer order
		ObjectIterator<CustomerOrder> orList = custStore.createQuery(qry)
		         								.setParam(1, "pan.prasanta@gmail.com")
										         .setParam(2, dateRange)
										         .setParam(3, 100)
										         .setParam(4, 5)
										         .iterator();
		// print the list now
		for (CustomerOrder o = null ; orList.hasNext();) {
			// get next CustomerOrder
			o = orList.next();
			// set DE active
			if (o.getRecordStatus() == ACTIVE) {
			    o.setRecordStatus(DEACTIVE);
			    // update record
				orList.update();
			} else {
				orList.remove();
			}			
		}
	}
	
	
}
