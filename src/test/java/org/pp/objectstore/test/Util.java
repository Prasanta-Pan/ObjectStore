package org.pp.objectstore.test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class Util {
	/**
	 * 
	 */
	private Util() {
		throw new RuntimeException("can not instantiate");
	}
	
	// for date formating
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	//
	public static final boolean MALE = true;
	//
	public static final boolean FEMALE = false;
	// 
	public static final char ACTIVE = 'A';
	// 
	public static final char DEACTIVE = 'D';
	// some random bytes
	static final byte[] ages = new byte[] { 
			3, 36, 41,5, 38, 61,34, 37, 68,
			21, 46, 58
	};
	// some random booleans
	static final boolean[] genders = new boolean[] {
			FEMALE, FEMALE, MALE, MALE, MALE, FEMALE,
			FEMALE, FEMALE, MALE, FEMALE, FEMALE, MALE
	};	
	// some random names
	static final String[] names = new String[] { 
			"Moana Pan", "Maumita Pan", "Prasanta Pan",
			"Kaustav Pan", "Sumanta Pan", "Kabita Pan",
			"Barnali Pan", "Katori Pan", "Sunil Pan",
			"Munmun Pan", "Jayanti Pan", "Pramod Pan"
	};
	// some random email's
	static final String[] emails = new String[] { 
			"pan.moana@gmail.com", "pan.maumita@gmail.com", "pan.prasanta@gmail.com",
			"pan.kaustav@gmail.com", "pan.sumanta@gmail.com", "pan.kabita@gmail.com",
			"pan.barnali@gmail.com", "pan.katori@gmail.com", "pan.sunil@gmail.com",
			"pan.munmun@gmail.com", "pan.jayanti@gmail.com", "pan.pramod@gmail.com"
	};
	// some random doubles
	static final double[] doubles = new double[] { 
			10456.657d, 1234.563d, 235.34d, 876.367d, 432.765d, 8972.456d,
			5691.4572d, 77123.123d, 7381.913d, 313.421d, 1235.543d, 7824.33d, 
			984.624d, 9874.412d, 874.34d, 897.54d 
	};
	// Some random long values
	static final long[] longs = new long[] {
			78248744L, 8441844L, 8839123L, 91238428L, 8341983L, 8128343L,
			64639343L, 0123414L, 1340048L, 12341924L, 10347484L, 912331234L,
			883124124L, 882441294L, 81232133L, 91223424L, 1123124124L
	};
	// Some random INT values
	static final int[] ints = new int[] {
			7824874, 844184, 883912, 9123842, 834198, 812834,
			6463934, 012341, 134004, 1234192, 1034748, 91233123,
			88312412, 88244129, 8123213, 9122342, 112312412
	};
	// some random floats
	static final float[] floats = new float[] { 
			34.3f, 76.34f, 12.43f, 87.58f, 98.43f, 56.76f, 
			67.43f, 54.56f,	48.65f, 43.87f, 78.34f, 09.54f,
			344.43f, 983.897f, 278.785f, 321.8934f, 712.432f
	};
	// some random shorts
	static final short[] shorts = new short[] { 
			5, 7, 12, 32, 16, 21, 17, 50, 65, 98, 31, 23, 
			35, 11, 6, 8, 13,19, 25, 27, 98, 321, 891, 567,
			5325, 7434, 8124, 8253, 8892, 9874, 4673, 7639
	};
	// Some random char's 
	static final char[] ranChars = new char[] {
			'\\', '#', '$', '@', '%', '-', '_', '+', '=',
			 '^', '!', '~', ';', ':', '?', '<', '>', '|',
			 'G' , 'K', 'T', '1', '2', '3', '`', ',' , '&'
	};	
	// some random dates
	static final String[] dates = new String[] { 
			"2018-03-13", "2018-05-21", "2018-08-28", "2018-11-30",
			"2018-01-31", "2018-02-19", "2018-07-28", "2018-09-17",
			"2020-03-13", "2020-05-21", "2020-08-28", "2020-11-30",
			"2020-01-31", "2020-02-19", "2020-07-28", "2020-09-17",
	};
	// some random chars
	static final char[] chars = new char[] { ACTIVE, DEACTIVE };
	
	/*
	 * Generate a random index of name
	 */
	public static int genIndex() {
		// get thread local random current instance
		ThreadLocalRandom rand = ThreadLocalRandom.current();
		// generate random INT
		return rand.nextInt(0, names.length);		
	}
	
	/**
	 * Get a name by the index
	 * @param index
	 * @return
	 */
	public static String getName(int index) {
		return names[index];
	}
	
	/**
	 * Get email name pertaining to an index
	 * @param index
	 * @return
	 */
	public static String getEmail(int index) {
		return emails[index];
	}
	
	/**
	 * Get age pertaining to an index
	 * @param index
	 * @return
	 */
	public static byte getAge(int index) {
		return ages[index];
	}
	
	/**
	 * Get gender pertaining to an index
	 * @param index
	 * @return
	 */
	public static boolean getGender(int index) {
		return genders[index];
	}
	
	/**
	 * Generate random date range
	 * @return
	 */
	public static List<Long> dateRange() {
		// get thread local random current instance
		ThreadLocalRandom rand = ThreadLocalRandom.current();
		// get a lower date
		String datel = dates[rand.nextInt(0, dates.length / 2)];
		// get higher date
		String dateh = dates[rand.nextInt(dates.length / 2, dates.length)];
		// return date range
		return Arrays.asList(stringDateToMillis(datel), stringDateToMillis(dateh));
	}
	
	/**
	 * Get email list
	 * @return
	 */
	public static Set<String> emailSet(int n) {
		// new hash set
		Set<String> set = new HashSet<>();
		// get thread local random current instance
		ThreadLocalRandom rand = ThreadLocalRandom.current();	
		// keep adding
		while (set.size() < n) {
			set.add(emails[rand.nextInt(0, emails.length)]);
		}
		// return
		return set;
	}
	
	/**
	 * Get double set
	 * @return
	 */
	public static Set<Double> doubleSet(int n) {
		// new hash set
		Set<Double> set = new HashSet<>();
		// get thread local random current instance
		ThreadLocalRandom rand = ThreadLocalRandom.current();	
		// keep adding
		while (set.size() < n) {
			set.add(doubles[rand.nextInt(0, doubles.length)]);
		}
		// return
		return set;
	}
		
	/**
	 * Generate random short value and return
	 * @return
	 */
	public static short genShort() {
		// get thread local random current instance
		ThreadLocalRandom rand = ThreadLocalRandom.current();
		// generate random short
		return shorts[rand.nextInt(0, shorts.length)];
	}
	
	/**
	 * Generate random int value
	 * @return
	 */
	public static int genInt() {
		// get thread local random current instance
		ThreadLocalRandom rand = ThreadLocalRandom.current();
		// generate random int
		return ints[rand.nextInt(0, ints.length)];		
	}
	
	/**
	 * Generate random long value
	 * @return
	 */
	public static long genLong() {
		// get thread local random current instance
		ThreadLocalRandom rand = ThreadLocalRandom.current();
		// generate random int
		return longs[rand.nextInt(0, longs.length)];		
	}	
	/**
	 * Generate random double value
	 * @return
	 */
	public static double genDouble() {
		// get thread local random current instance
		ThreadLocalRandom rand = ThreadLocalRandom.current();
		// generate random double
		return doubles[rand.nextInt(0, doubles.length)];
	}
	
	/**
	 * Generate a random float value
	 * @return
	 */
	public static float genFloat() {
		// get thread local random current instance
		ThreadLocalRandom rand = ThreadLocalRandom.current();
		// generate random float
		return floats[rand.nextInt(0, floats.length)];
	}
	
	/**
	 * Generate random char value
	 * @return
	 */
	public static char genChar() {
		// get thread local random current instance
		ThreadLocalRandom rand = ThreadLocalRandom.current();
		// generate random char
		return ranChars[rand.nextInt(0, ranChars.length)];
	}
	
	/**
	 * Generate random status, active or Deactive
	 * @return
	 */
	public static char genStatus() {
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
	public static long genDateToMilis() {
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
