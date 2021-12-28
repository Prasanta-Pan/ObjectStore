package org.pp.objectstore.test;

import static org.pp.objectstore.test.Util.dateRange;
import static org.pp.objectstore.test.Util.doubleSet;
import static org.pp.objectstore.test.Util.genDouble;
import static org.pp.objectstore.test.Util.genIndex;
import static org.pp.objectstore.test.Util.genShort;
import static org.pp.objectstore.test.Util.getEmail;
import static org.pp.objectstore.test.Util.genInt;
import static org.pp.objectstore.test.Util.emailSet;
import static org.pp.objectstore.test.Util.genDateToMilis;
import static org.pp.objectstore.test.domain.CustomerOrder.generate;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.pp.objectstore.interfaces.ObjectStore;
import org.pp.objectstore.test.domain.CustomerOrder;
import org.pp.qry.interfaces.ObjectIterator;
import org.pp.qry.interfaces.Query;

@TestMethodOrder(OrderAnnotation.class)
public class CustomerOrderQryTest extends AbstractTest {
	// indicate if closing of DB is required after each test
	private boolean alwaysCloseDb = false;
	/**
	 * To hold the list of order
	 */
	static final NavigableSet<CustomerOrder> orderSet = new TreeSet<>();
	
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
		for (int i = 0; i < 10000; i++) {
			// generate an order
			CustomerOrder co = generate();
			// store it
			coStore.store(co);
			// add to set
			orderSet.add(co);
		}
		// print number of customer added
		System.out.println("Number of Customer Orders: " + orderSet.size());
	}
	
	@Test
	@Order(2)
	public void iterateOrder() throws Exception {
		// open customer order store
		ObjectStore<CustomerOrder> coStore = factory.openOrCreateStore(CustomerOrder.class);
		// open iterator
		ObjectIterator<CustomerOrder> itr = coStore.iterator();
		// open set iterator
		Iterator<CustomerOrder> setItr = orderSet.iterator();
		// to validate record count
		int count = 0;	
		// Validate orders
		while (itr.hasNext() && setItr.hasNext()) {
			// get next order from set
			CustomerOrder coset = setItr.next();
			// get next order from store
			CustomerOrder costore = itr.next();
			// compare
			if (!coset.equals(costore)) {
				// throw exception of not match
				throw new RuntimeException("Orders miss match, set: " + coset + ", store: " + costore);
			}
			count++;
		}
		// if count miss match
		if (count != orderSet.size()) {
			// throw exception of not match
			throw new RuntimeException("Order count miss match, set: " + orderSet.size() + ", store: " + count);
		}
		// total record read
		System.out.println("Total orders validated: " + count);		
	}
	
	/**
	 * Print collection of objects
	 * @param list
	 */
	public void print(Collection<?> col) {
		for (Object o: col)
			System.out.println(o);
	}
	
	@Test
	@Order(3)
	public void findCustByDateRange() throws Exception {
		// open customer order store
		ObjectStore<CustomerOrder> custStore = factory.openOrCreateStore(CustomerOrder.class);
		// create some query
		String qry = "customerEmail = ? && orderDate between ? && (totalOrder > ? || numOfUnits > ?)";
		System.out.println("qry: " + qry);
		// create a query
		Query<CustomerOrder> q = custStore.createQuery(qry);
		// number of run
		for (int i = 0; i < 5; i++) {
			// instantiate corresponding manual validator
			ManualCustQry1 validator = new ManualCustQry1(4, orderSet);
			// generate random index
			int index = genIndex();
			// generate random email
			String email = getEmail(index);
			// generate random date range
			List<Long> dr = dateRange();
			// generate random total order
			double total = genDouble();
			// generate random number of units
			short units  = genShort();
			// system time
			long t = System.nanoTime();
			// get filtered list
			List<CustomerOrder>	flList = q.setParam(1, email)
					  					  .setParam(2, dr)
					  					  .setParam(3, total)
					  					  .setParam(4, units)
					  					  .list();
			System.out.println("Time: " + ((System.nanoTime() - t) / 1000));
			// validate list
			List<CustomerOrder>	vList = validator.setParam(1, email)
					  							 .setParam(2, dr)
					  							 .setParam(3, total)
					  							 .setParam(4, units)
					  							 .list();
			validator.validate(flList, vList);
		}
	}	
	
	@Test
	@Order(4)
	public void findCustByDateRangeNot() throws Exception {
		// open customer order store
		ObjectStore<CustomerOrder> custStore = factory.openOrCreateStore(CustomerOrder.class);
		// create some query
		String qry = "customerEmail = ? && orderDate between ? && !(totalOrder > ? || numOfUnits > ?)";
		System.out.println("qry: " + qry);
		// create a query
		Query<CustomerOrder> q = custStore.createQuery(qry);
		// number of run
		for (int i = 0; i < 5; i++) {
			// instantiate corresponding manual validator
			ManualCustQryNot validator = new ManualCustQryNot(4, orderSet);
			// generate random index
			int index = genIndex();
			// generate random email
			String email = getEmail(index);
			// generate random date range
			List<Long> dr = dateRange();
			// generate random total order
			double total = genDouble();
			// generate random number of units
			short units  = genShort();
			// system time
			long t = System.nanoTime();
			// get filtered list
			List<CustomerOrder>	flList = q.setParam(1, email)
					  					  .setParam(2, dr)
					  					  .setParam(3, total)
					  					  .setParam(4, units)
					  					  .list();
			System.out.println("Time: " + ((System.nanoTime() - t) / 1000));
			// validate list
			List<CustomerOrder>	vList = validator.setParam(1, email)
					  							 .setParam(2, dr)
					  							 .setParam(3, total)
					  							 .setParam(4, units)
					  							 .list();
			validator.validate(flList, vList);	
		}
	}
	
	@Test
	@Order(5)
	public void findCustByTotalIn() throws Exception {
		// open customer order store
		ObjectStore<CustomerOrder> custStore = factory.openOrCreateStore(CustomerOrder.class);
		// create some query
		String qry = "customerEmail = ? && (totalOrder in ? || (orderDate > ? && numOfUnits > ?))";
		System.out.println("qry: " + qry);
		// create a query
		Query<CustomerOrder> q = custStore.createQuery(qry);
		// number of run
		for (int i = 0; i < 5; i++) {
			// instantiate corresponding manual validator
			ManualCustQryIn validator = new ManualCustQryIn(4, orderSet);
			// generate random index
			int index = genIndex();
			// generate random email
			String email = getEmail(index);
			// generate random total order set
			Set<Double> toSet = doubleSet(5);
			// generate random order date
			long oDate = genDateToMilis();
			// generate random number of units
			short units  = genShort();
			// system time
			long t = System.nanoTime();
			// get filtered list
			List<CustomerOrder>	flList = q.setParam(1, email)
					  					  .setParam(2, toSet)
					  					  .setParam(3, oDate)
					  					  .setParam(4, units)
					  					  .list();
			System.out.println("Time: " + ((System.nanoTime() - t) / 1000));
			// validate list
			List<CustomerOrder>	vList = validator.setParam(1, email)
					  							 .setParam(2, toSet)
					  							 .setParam(3, oDate)
					  							 .setParam(4, units)
					  							 .list();
			validator.validate(flList, vList);	
		}
	}
	
	@Test
	@Order(6)
	public void findCustByTotalNin() throws Exception {
		// open customer order store
		ObjectStore<CustomerOrder> custStore = factory.openOrCreateStore(CustomerOrder.class);
		// create some query
		String qry = "customerEmail = ? && (totalOrder nin ? || (orderDate > ? && numOfUnits > ?))";
		System.out.println("qry: " + qry);
		// create a query
		Query<CustomerOrder> q = custStore.createQuery(qry);
		// number of run
		for (int i = 0; i < 5; i++) {
			// instantiate corresponding manual validator
			ManualCustQryNin validator = new ManualCustQryNin(4, orderSet);
			// generate random index
			int index = genIndex();
			// generate random email
			String email = getEmail(index);
			// generate random total order set
			Set<Double> toSet = doubleSet(5);
			// generate random order date
			long oDate = genDateToMilis();
			// generate random number of units
			short units  = genShort();
			// system time
			long t = System.nanoTime();
			// get filtered list
			List<CustomerOrder>	flList = q.setParam(1, email)
					  					  .setParam(2, toSet)
					  					  .setParam(3, oDate)
					  					  .setParam(4, units)
					  					  .list();
			System.out.println("Time: " + ((System.nanoTime() - t) / 1000));
			// validate list
			List<CustomerOrder>	vList = validator.setParam(1, email)
					  							 .setParam(2, toSet)
					  							 .setParam(3, oDate)
					  							 .setParam(4, units)
					  							 .list();
			validator.validate(flList, vList);	
		}
	}
	
	@Test
	@Order(7)
	public void findCustByNotMale() throws Exception {
		// open customer order store
		ObjectStore<CustomerOrder> custStore = factory.openOrCreateStore(CustomerOrder.class);
		// create some query
		String qry = "customerEmail = ? && orderDate between ? && ((totalOrder > ? && numOfUnits > ?) || !customerSex)";
		System.out.println("qry: " + qry);
		// create a query
		Query<CustomerOrder> q = custStore.createQuery(qry);
		// number of run
		for (int i = 0; i < 5; i++) {
			// instantiate corresponding manual validator
			ManualCustQryNotMale validator = new ManualCustQryNotMale(4, orderSet);
			// generate random index
			int index = genIndex();
			// generate random email
			String email = getEmail(index);
			// generate random date range
			List<Long> dr = dateRange();
			// generate random total order
			double total = genDouble();
			// generate random number of units
			short units  = genShort();
			// system time
			long t = System.nanoTime();
			// get filtered list
			List<CustomerOrder>	flList = q.setParam(1, email)
					  					  .setParam(2, dr)
					  					  .setParam(3, total)
					  					  .setParam(4, units)
					  					  .list();
			System.out.println("Time: " + ((System.nanoTime() - t) / 1000));
			// validate list
			List<CustomerOrder>	vList = validator.setParam(1, email)
					  							 .setParam(2, dr)
					  							 .setParam(3, total)
					  							 .setParam(4, units)
					  							 .list();
			validator.validate(flList, vList);
		}
	}
	
	@Test
	@Order(7)
	public void findCustByNotBet() throws Exception {
		// open customer order store
		ObjectStore<CustomerOrder> custStore = factory.openOrCreateStore(CustomerOrder.class);
		// create some query
		String qry = "customerEmail = ? && orderDate nbet ? && ((totalOrder > ? && numOfUnits > ?) || !customerSex)";
		System.out.println("qry: " + qry);
		// create a query
		Query<CustomerOrder> q = custStore.createQuery(qry);
		// number of run
		for (int i = 0; i < 5; i++) {
			// instantiate corresponding manual validator
			ManualCustQryNotBet validator = new ManualCustQryNotBet(4, orderSet);
			// generate random index
			int index = genIndex();
			// generate random email
			String email = getEmail(index);
			// generate random date range
			List<Long> dr = dateRange();
			// generate random total order
			double total = genDouble();
			// generate random number of units
			short units  = genShort();
			// system time
			long t = System.nanoTime();
			// get filtered list
			List<CustomerOrder>	flList = q.setParam(1, email)
					  					  .setParam(2, dr)
					  					  .setParam(3, total)
					  					  .setParam(4, units)
					  					  .list();
			System.out.println("Time: " + ((System.nanoTime() - t) / 1000));
			// validate list
			List<CustomerOrder>	vList = validator.setParam(1, email)
					  							 .setParam(2, dr)
					  							 .setParam(3, total)
					  							 .setParam(4, units)
					  							 .list();
			validator.validate(flList, vList);
		}
	}
	
	@Test
	@Order(8)
	public void findCustByNotIn() throws Exception {
		// open customer order store
		ObjectStore<CustomerOrder> custStore = factory.openOrCreateStore(CustomerOrder.class);
		// create some query
		String qry = "customerEmail nin ? && ((totalOrder < ? && numOfUnits > ?) || !customerSex)";
		System.out.println("qry: " + qry);
		// create a query
		Query<CustomerOrder> q = custStore.createQuery(qry);		
			// instantiate corresponding manual validator
			ManualCustQryEmailNotIn validator = new ManualCustQryEmailNotIn(4, orderSet);
			// generate random email
			Set<String> emailSet = emailSet(5);
			// generate random total order
			double total = genDouble();
			// generate random number of units
			short units  = genShort();
			// system time
			long t = System.nanoTime();
			// get filtered list
			List<CustomerOrder>	flList = q.setParam(1, emailSet)
					  					  .setParam(2, total)
					  					  .setParam(3, units)
					  					  .list();
			System.out.println("Time: " + ((System.nanoTime() - t) / 1000));
			// validate list
			List<CustomerOrder>	vList = validator.setParam(1, emailSet)
					  							 .setParam(2, total)
					  							 .setParam(3, units)
					  							 .list();
			validator.validate(flList, vList);
		
	}
	
	@Test
	@Order(9)
	public void findCustRandArith() throws Exception {
		// open customer order store
		ObjectStore<CustomerOrder> custStore = factory.openOrCreateStore(CustomerOrder.class);
		// create some query
		String qry = "(customerEmail != ? && orderDate between ?) || totalOrder > unitPrice * (5 + ?) / 2 || !customerSex";
		System.out.println("qry: " + qry);
		// create a query
		Query<CustomerOrder> q = custStore.createQuery(qry);
		// instantiate corresponding manual validator
		ManualCustQryRandArith validator = new ManualCustQryRandArith(4, orderSet);
		// generate random index
		int index = genIndex();
		// generate random email
		String email = getEmail(index);
		// generate random date range
		List<Long> dr = dateRange();
		// generate random INT
		int v = genInt();
		// system time
		long t = System.nanoTime();
		// get filtered list
		List<CustomerOrder> flList = q.setParam(1, email)
									  .setParam(2, dr)
									  .setParam(3, v)
									  .list();
		System.out.println("Time: " + ((System.nanoTime() - t) / 1000));
		// validate list
		List<CustomerOrder> vList = validator.setParam(1, email)
				  							 .setParam(2, dr)
				  							 .setParam(3, v)
				  							 .list();
		validator.validate(flList, vList);
		
	}
	
	@Test
	@Order(10)
	public void findCustRandArith2() throws Exception {
		// open customer order store
		ObjectStore<CustomerOrder> custStore = factory.openOrCreateStore(CustomerOrder.class);
		// create some query
		String qry = "(customerEmail > ? && orderDate between ?) || (totalOrder <= unitPrice * (? - 5) % 2 && !customerSex)";
		System.out.println("qry: " + qry);
		// create a query
		Query<CustomerOrder> q = custStore.createQuery(qry);
		// instantiate corresponding manual validator
		ManualCustQryRandArith2 validator = new ManualCustQryRandArith2(4, orderSet);
		// generate random index
		int index = genIndex();
		// generate random email
		String email = getEmail(index);
		// generate random date range
		List<Long> dr = dateRange();
		// generate random INT
		int v = genInt();
		// system time
		long t = System.nanoTime();
		// get filtered list
		List<CustomerOrder> flList = q.setParam(1, email)
									  .setParam(2, dr)
									  .setParam(3, v)
									  .list();
		System.out.println("Time: " + ((System.nanoTime() - t) / 1000));
		// validate list
		List<CustomerOrder> vList = validator.setParam(1, email)
				  							 .setParam(2, dr)
				  							 .setParam(3, v)
				  							 .list();
		validator.validate(flList, vList);
		
	}
	
	@Test
	@Order(11)
	public void findCustGtString() throws Exception {
		// open customer order store
		ObjectStore<CustomerOrder> custStore = factory.openOrCreateStore(CustomerOrder.class);
		// create some query
		String qry = "customerEmail > ? && orderDate between ? && (totalOrder <= unitPrice * (? - 5) % 2 || !customerSex)";
		System.out.println("qry: " + qry);
		// create a query
		Query<CustomerOrder> q = custStore.createQuery(qry);
		// instantiate corresponding manual validator
		ManualCustQryGtString validator = new ManualCustQryGtString(4, orderSet);
		// generate random index
		int index = genIndex();
		// generate random email
		String email = getEmail(index);
		// generate random date range
		List<Long> dr = dateRange();
		// generate random INT
		int v = genInt();
		// system time
		long t = System.nanoTime();
		// get filtered list
		List<CustomerOrder> flList = q.setParam(1, email)
									  .setParam(2, dr)
									  .setParam(3, v)
									  .list();
		System.out.println("Time: " + ((System.nanoTime() - t) / 1000));
		// validate list
		List<CustomerOrder> vList = validator.setParam(1, email)
				  							 .setParam(2, dr)
				  							 .setParam(3, v)
				  							 .list();
		validator.validate(flList, vList);
		
	}
	
	@Test
	@Order(12)
	public void findCustBetString() throws Exception {
		// open customer order store
		ObjectStore<CustomerOrder> custStore = factory.openOrCreateStore(CustomerOrder.class);
		// create some query
		String qry = "customerEmail between ? && (orderDate < ? || (totalOrder <= unitPrice * (? - 5) % 2 && !customerSex))";
		System.out.println("qry: " + qry);
		// create a query
		Query<CustomerOrder> q = custStore.createQuery(qry);
		// instantiate corresponding manual validator
		ManualCustQryBetString validator = new ManualCustQryBetString(4, orderSet);
		// email range
		List<String> emailRng = Arrays.asList("pan.maumita@gmail.com", "pan.moana@gmail.com");
		// generate random date
		long date = genDateToMilis();
		// generate random INT
		int v = genInt();
		// system time
		long t = System.nanoTime();
		// get filtered list
		List<CustomerOrder> flList = q.setParam(1, emailRng)
									  .setParam(2, date)
									  .setParam(3, v)
									  .list();
		System.out.println("Time: " + ((System.nanoTime() - t) / 1000));
		// validate list
		List<CustomerOrder> vList = validator.setParam(1, emailRng)
				  							 .setParam(2, date)
				  							 .setParam(3, v)
				  							 .list();
		validator.validate(flList, vList);
		
	}
	
}
