package org.pp.objectstore.test;

public class RandomTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		long t = 1000L * 60 * 24 * 30 * 365 * 500;	
		String str = Long.toBinaryString(t);
		System.out.println(str);
		System.out.println("Len: " + str.length());
		// current time length
		t = System.currentTimeMillis();
		str = Long.toBinaryString(t);
		System.out.println("Current time Len: " + str.length());
		
	}

}
