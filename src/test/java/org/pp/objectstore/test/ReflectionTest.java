package org.pp.objectstore.test;

import java.lang.reflect.Field;

public class ReflectionTest {
	
	private static Object[] vals = { 123456L, "Prasanta Pan", 84984827L, 41, 
			  true, "46 eastwood rd", "46 eastwood rd",
			  "pan.prasanta@gmail.com", 578.65f, 56782.67d, 30};
	
    public static void main(String[] args) throws Exception {
    	test1();
    	test2();
    	
    }
    
    private static void test1() throws Exception {
    	System.out.println("First");
    	Class<Customer> clazz = Customer.class;
    	Field[] flds = clazz.getDeclaredFields();
    	
    	// 
    	for (Field fld: flds) 
			fld.setAccessible(true);
		
    	long t;
    	for (int i = 0; i < 20; i++) {
    		t = System.nanoTime();
    		Customer p = clazz.newInstance();
    		for (int j = 0; j < flds.length; j++) {
    			flds[j].set(p, vals[j]);
    		}
    		System.out.println((System.nanoTime() - t)/1000);
    	}
    }
    
    private static void test2() throws Exception {
    	System.out.println("Second");    	  
    	long t;
    	for (int i = 0; i < 20; i++) {
    		t = System.nanoTime();
    		Class<Customer> clazz = Customer.class;
        	Field[] flds = clazz.getDeclaredFields();
    		Customer p = clazz.newInstance();
    		for (int j = 0; j < flds.length; j++) {
    			flds[j].setAccessible(true);
    			flds[j].set(p, vals[j]);
    		}
    		System.out.println((System.nanoTime() - t)/1000);
    	}
    }
}
