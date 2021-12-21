package org.pp.objectstore.test;

import static org.pp.objectstore.test.CustomerClassLoader.PKG;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.pp.objectstore.interfaces.ObjectStore;
import org.pp.objectstore.test.domain.Customer;
import org.pp.qry.interfaces.ObjectIterator;

public class ChangeFieldNameTest extends AbstractTest {
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
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void openAndClose(String className) throws Exception {
		openDatabase();
		try {
			// load class
			Class custClass = new CustomerClassLoader(AddSortKeyTest.class.getClassLoader()).loadClass(PKG + className);
			// open store
			ObjectStore<Customer> store = (ObjectStore<Customer>) factory.openOrCreateStore(custClass);
			/**
			 * Create some sample customers
			 */
			Customer cust = (Customer) custClass.newInstance();
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
			cust = (Customer) custClass.newInstance();
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
			cust = (Customer) custClass.newInstance();
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
	 * According to design wealth field values must be zero
	 * @param className
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void verifyFieldValues(String className) throws Exception {
		// standard error message
		String msg = "Wealth field value supposed to be zero";
		openDatabase();
		try {
			// load class
			Class custClass = new CustomerClassLoader(AddSortKeyTest.class.getClassLoader()).loadClass(PKG + className);
			// open store
			ObjectStore<Customer> store = (ObjectStore<Customer>) factory.openOrCreateStore(custClass);
			// open iterator
			for (ObjectIterator<Customer> itr = store.iterator(); itr.hasNext();) {
				// get next customer
				Customer cust = itr.next();
				// check field value, it must be zero
				if (cust.getWealth() != 0.0f)
					throw new RuntimeException(msg);			
			}
			
		} catch (Throwable e) {
			if (!msg.equals(e.getMessage()))
				throw e;
		} finally {
			closeDatabase();
		}
		
	}	
		
	@Test()
	public void test() throws Exception {
		// open database and create some customer
		openAndClose("CustomerImp");
		// verify wealth field
		verifyFieldValues("ChangedFieldName");
	}
}
