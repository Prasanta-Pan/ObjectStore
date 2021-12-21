package org.pp.objectstore.test;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.pp.objectstore.interfaces.ObjectStore;
import org.pp.objectstore.test.domain.Customer;
import org.pp.objectstore.test.domain.CustomerImp;
import org.pp.qry.interfaces.ObjectIterator;

public class OpenStoreTest extends AbstractTest {
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
	
	@Test
	public void test() throws Exception {
		try {
			// must throw exception
			doIteration("");
		} catch (Exception e) {
			if (!"Object store 'customer' doesn't exist".equals(e.getMessage()))
				throw e;
		}
	}
	
}
