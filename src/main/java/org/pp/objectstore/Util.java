package org.pp.objectstore;

import static org.pp.objectstore.DataTypes.D_TYP_STR;
import static org.pp.objectstore.ObjectStoreImp.MAX_KEY_SZ;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;

import org.pp.objectstore.interfaces.Collection;

final class Util {
	/**
	 * No instance please
	 */
	private Util() {
	}

	/**
	 * current sequence number
	 */
	private static int SEQ = -1;
	/**
	 * Max sequence number supported (12 bit)
	 */
	private static final int MAX_SEQ = 4095;
	/**
	 * Current time in milliseconds
	 */
	private static long curTime = System.currentTimeMillis();

	/**
	 * generate next unique id
	 * 
	 * @return
	 */
	@SuppressWarnings("static-access")
	static final synchronized long genId() {
		// if exceed MAX sequence reset to 0
		if (++SEQ > MAX_SEQ) {
			long tmp = curTime;
			// in case number is running too fast
			while (tmp == (curTime = System.currentTimeMillis())) {
				try {
					// sleep 1 millisecond
					Thread.currentThread().sleep(1);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
			SEQ = 0;
		}
		// generate and return ID
		return curTime << 12 | 0x7fffffffffffffffL & SEQ;
	}

	/**
	 * Validate class and return class name
	 * 
	 * @param clazz
	 * @return
	 */
	static final String validateClassName(Class<?> clazz) {
		String cname = null;
		Collection coll = clazz.getAnnotation(Collection.class);
		// Must be having Collection annotation
		if (coll == null)
			throw new RuntimeException("@Collection not present in class " + clazz);
		// if collection name was not provided...
		cname = coll.value();
		if (cname == null || "".equals(cname = cname.trim()))
			cname = clazz.getSimpleName();
		// return class name
		return cname;
	}

	/**
	 * Serialise string to byte buffer
	 * 
	 * @param str
	 * @param buf
	 * @return
	 */
	static final ByteBuffer stringToByteBuffer(String str, ByteBuffer buf) {
		int len = str.length();
		// extend byte buffer if necessary
		buf = extend(buf, len * 2 + 5);
		// set data type
		buf.put(D_TYP_STR);
		// set char length
		buf.putInt(len);
		// copy chars one by one
		for (int i = 0; i < len; i++)
			buf.putChar(str.charAt(i));
		//
		return buf;
	}

	/**
	 * Convert String to serialise form
	 * 
	 * @param str
	 * @return
	 */
	static final byte[] stringToBytes(String str) {
		// get the length of the string
		int len = str.length();
		// allocate byte buffer
		ByteBuffer buf = ByteBuffer.allocate(5 + len * 2);
		// set string type
		buf.put(D_TYP_STR);
		// set char length
		buf.putInt(len);
		// copy chars
		for (int i = 0; i < len; i++)
			buf.putChar(str.charAt(i));
		// return byte array
		return buf.array();
	}

	/**
	 * Deserialise string bytes to String
	 * 
	 * @param strbytes
	 * @return
	 */
	static final String bytesToString(byte[] strbytes) {
		// wrap it
		ByteBuffer buf = ByteBuffer.wrap(strbytes);
		// return String
		return byteBufferToString(buf);
	}

	/**
	 * Build String from byte buffer
	 * 
	 * @param buf
	 * @return
	 */
	static final String byteBufferToString(ByteBuffer buf, Field fld) {
		// remove type
		if (buf.get() != D_TYP_STR)
			invalidType(fld);
		// get char length
		int len = buf.getInt();
		char[] chars = new char[len];
		for (int i = 0; i < len; i++)
			chars[i] = buf.getChar();
		//
		return new String(chars);
	}
	/**
	 * Helper method
	 * @param buf
	 * @return
	 */
	static final String byteBufferToString(ByteBuffer buf)  {
		return byteBufferToString(buf, null);
	}

	/**
	 * Copy bytes array
	 * 
	 * @param src
	 * @return
	 */
	static final byte[] copyBytes(byte[] src) {
		byte[] cp = new byte[src.length];
		System.arraycopy(src, 0, cp, 0, src.length);
		return cp;
	}

	/**
	 * Throw exception of invalid type
	 */
	static final void invalidType(Field fld) {
		throw new RuntimeException("Invalid data type for the field '" + fld + "'");
	}
	

	/**
	 * Extend byte buffer
	 * 
	 * @param buf
	 * @param sz
	 * @return
	 */
	static final ByteBuffer extend(ByteBuffer buf, int sz) {
		if (buf.capacity() - buf.position() < sz) {
			int pos = buf.position();
			byte[] newData = new byte[pos + MAX_KEY_SZ];
			buf.flip();
			buf.get(newData, 0, pos);
			buf = ByteBuffer.wrap(newData);
			buf.position(pos);
		}
		return buf;
	}

	/**
	 * Next higher string lexicographically
	 * 
	 * @param val
	 * @return
	 */
	static final String higherValue(String val) {
		// convert string char array
		char[] chars = val.toCharArray();
		// copy entire chars except last to string builder
		int len = chars.length - 1;
		StringBuilder sbLocal = new StringBuilder();
		// copy all chars
		for (int i = 0; i < len; i++)
			sbLocal.append(chars[i]);
		// check if their is room to increment
		if (chars[len] < Character.MAX_VALUE) {
			chars[len] = (char) (chars[len] + 1);
			sbLocal.append(chars[len]);
		} else { // if not append a minimum character value
			sbLocal.append(chars[len]);
			sbLocal.append(Character.MIN_VALUE);
		}
		return sbLocal.toString();
	}

	/**
	 * Extract bytes from byte buffer
	 * 
	 * @param buf
	 * @return
	 */
	static final byte[] extract(ByteBuffer buf) {
		byte[] key = new byte[buf.position()];
		buf.flip();
		buf.get(key);
		return key;
	}

	public static void main(String[] args) {
		long t = System.currentTimeMillis();
		System.out.println(Long.toBinaryString(t));
	}
}
