package org.pp.objectstore.test;

import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.pp.objectstore.interfaces.ObjectStore;
import org.pp.objectstore.interfaces.PurgeStoreHandler;
import org.pp.objectstore.test.domain.Customer;
import org.pp.objectstore.test.domain.CustomerImp;
import org.pp.qry.interfaces.ObjectIterator;

public class PurgeStoreTest extends AbstractTest {

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
	 * First setup customer collection
	 * @throws Exception
	 */
	private void openAndClose(String className) throws Exception {
		openDatabase();
		try {
			// open store
			ObjectStore<CustomerImp> store = factory.openOrCreateStore(CustomerImp.class);
			/**
			 * Create some sample customers
			 */
			CustomerImp cust = new CustomerImp();
			cust.setId("S7966622Z");
			cust.setName("Prasanta Pan");
			cust.setContact(84984827);
			cust.setEmail("pan.prasanta@gmail.com");
			cust.setActive(true);
			cust.setAge(41);
			cust.setGender(Customer.MALE);
			cust.setWealth(10.11f);
			cust.setModificationDate(System.currentTimeMillis());
			store.store(cust);
			//
			cust = new CustomerImp();
			cust.setId("S7203345");
			cust.setName("Moana Pan");
			cust.setContact(84984827);
			cust.setEmail("pan.moana@gmail.com");
			cust.setActive(true);
			cust.setAge(3);
			cust.setGender(Customer.FEMALE);
			cust.setWealth(50.31f);
			cust.setModificationDate(System.currentTimeMillis());
			store.store(cust);
			// 
			cust = new CustomerImp();
			cust.setId("L4069669");
			cust.setName("Maumita Pan");
			cust.setContact(83995981);
			cust.setEmail("pan.maumita@gmail.com");
			cust.setActive(true);
			cust.setAge(36);
			cust.setGender(Customer.FEMALE);
			cust.setWealth(100.30f);
			cust.setModificationDate(System.currentTimeMillis());
			store.store(cust);	
			// sync data
			factory.sync();			
		} finally {
			closeDatabase();
		}
	}
	
	/**
	 * Iterate to ensure data persisted correctly
	 * @throws Exception
	 */
	private void doIteration(String className) throws Exception{
		openDatabase();
		try {
			// open store
			ObjectStore<CustomerImp> store = factory.openStore(CustomerImp.class);
			// open iterator
			for (ObjectIterator<CustomerImp> itr = store.iterator(); itr.hasNext();) {
				// get next customer
				Customer cust = itr.next();
				// check field value, it must be zero
				System.out.println(cust);
			}
		} finally {
			closeDatabase();
		}
	}
	
	private void purgeStore() throws Exception {
		openDatabase();
		try {
			// purge the store
			factory.purgeStore(CustomerImp.class, new PurgeStoreHandler() {
				
				@Override
				public void handle(Map<String, Object> map) {
					// TODO Auto-generated method stub
					CustomerImp cust = new CustomerImp();
					// set id
					cust.setId((String) map.get("id"));
					// set email
					cust.setEmail((String) map.get("email"));
					// set address
					cust.setAddress("Laguna 88, 46 eastwood rd, #01-07 Singapore 486356");
					// set Contact
					cust.setContact((long) map.get("contact"));
					// set age
					cust.setAge((int) map.get("age"));
					// set gender
					cust.setGender((char) map.get("gender"));
					// set wealth
					cust.setWealth((float) map.get("wealth"));
					// set active
					cust.setActive((boolean) map.get("active"));
					// print
					System.out.println(cust);
				}
			});
		} finally {
			closeDatabase();
		}
	}
	
	@Test
	public void test() throws Exception {
		// open database and create some customer
		openAndClose("CustomerImp");
		// print records
		doIteration("CustomerImp");
		// purge store now
		purgeStore();
		// now open the store once again
		try {
			// will throw error
			doIteration("CustomerImp");
		} catch (Exception e) {
			if (!"Object store 'customer' doesn't exist".equals(e.getMessage()))
				throw e;
		}
	}
}
