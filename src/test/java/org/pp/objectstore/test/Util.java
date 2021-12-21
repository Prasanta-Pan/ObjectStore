package org.pp.objectstore.test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

public class Util {
	// for date formating
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	
	// some random dates
	public static final String[] dates = new String[] { 
		"2018-09-18", "2018-09-21", "2018-09-29", 
		"2020-09-18", "2020-11-07", "2020-05-11" 
	};
	//
	public static final boolean MALE = true;
	//
	public static final boolean FEMALE = false;
	// 
	public static final char ACTIVE = 'A';
	// 
	public static final char DEACTIVE = 'D';
	// some random bytes
	public static final byte[] ages = new byte[] { 3, 36, 41 };
	// some random booleans
	public static final boolean[] booleans = new boolean[] {FEMALE, FEMALE, MALE};
	// some random names
	public static final String[] names = new String[] { "Moana Pan", "Maumita Pan", "Prasanta Pan" };
	// some random email's
	public static final String[] emails = new String[] { "pan.moana@gmail.com", "pan.maumita@gmail.com", "pan.prasanta@gmail.com" };
	// some random doubles
	public static final double[] doubles = new double[] { 
			10456.657d, 1234.563d, 235.34d, 876.367d, 432.765d, 8972.456d,
			5691.4572d, 77123.123d, 7381.913d, 313.421d, 1235.543d, 7824.33d, 
			984.624d, 9874.412d, 874.34d, 897.54d 
	};
	// some random floats
	public static final float[] floats = new float[] { 
			34.3f, 76.34f, 12.43f, 87.58f, 98.43f, 56.76f, 
			67.43f, 54.56f,	48.65f, 43.87f, 78.34f, 09.54f 
	};
	// some random shorts
	public static final short[] shorts = new short[] { 
			5, 7, 12, 32, 16, 21, 17, 50, 65, 98, 31, 23, 
			35, 11, 6, 8, 13,19, 25, 27 
	};
	// some random chars
	public static final char[] chars = new char[] { ACTIVE, DEACTIVE };
	
	/**
	 * Generate a random byte and return
	 * @return
	 */
	public static byte genRandByte() {
		// get thread local random current instance 
		ThreadLocalRandom rand = ThreadLocalRandom.current();
		// generate random byte
		return ages[rand.nextInt(0, ages.length)];
	}
	
	/**
	 * Generate random short value and return
	 * @return
	 */
	public static short genRandShort() {
		// get thread local random current instance
		ThreadLocalRandom rand = ThreadLocalRandom.current();
		// generate random short
		return shorts[rand.nextInt(0, shorts.length)];
	}
	
	/**
	 * Generate random double value
	 * @return
	 */
	public static double genRandDouble() {
		// get thread local random current instance
		ThreadLocalRandom rand = ThreadLocalRandom.current();
		// generate random double
		return doubles[rand.nextInt(0, doubles.length)];
	}
	
	/**
	 * Generate a random float value
	 * @return
	 */
	public static float genRandFloat() {
		// get thread local random current instance
		ThreadLocalRandom rand = ThreadLocalRandom.current();
		// generate random float
		return floats[rand.nextInt(0, floats.length)];
	}
	
	/**
	 * Generate random char value
	 * @return
	 */
	public static char genRandChar() {
		// get thread local random current instance
		ThreadLocalRandom rand = ThreadLocalRandom.current();
		// generate random char
		return chars[rand.nextInt(0, chars.length)];
	}
	
	/**
	 * Generate a random date than convert to long
	 * @param date
	 * @return
	 */
	public static long randomDateToMilis() {
		// to generate random date string 
		ThreadLocalRandom rand = ThreadLocalRandom.current();
		// get a random date string
		String date = dates[rand.nextInt(0, dates.length)];
		// convert date to long value
		try {
			return sdf.parse(date).getTime();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Convert milliseconds to date String
	 * @param milis
	 * @return
	 * @throws Exception
	 */
	public static String millisToDateString(long milis) {
		return sdf.format(new Date(milis));
	}
	
	/**
	 * Convert String date to milliseconds
	 * @param date
	 * @return
	 */
	public static long stringDateToMillis(String date) {
		// convert date to long value
		try {
			return sdf.parse(date).getTime();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			throw new RuntimeException(e);
		}
	}	
	
}
