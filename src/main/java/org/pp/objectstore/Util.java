package org.pp.objectstore;

import static org.pp.objectstore.interfaces.Constants.MAX_KEY_SIZE;
import static org.pp.objectstore.interfaces.Constants.MIN_BUF_SIZE;

import java.nio.ByteBuffer;

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
	 * Generate unique long id
	 * @return
	 */
	@SuppressWarnings("static-access")
	static synchronized final long genId() {
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
	 * Extend byte buffer
	 * 
	 * @param buf
	 * @param sz
	 * @return
	 */
	static final ByteBuffer extend(ByteBuffer buf, int sz) {
		if (buf.remaining() < sz) {
			// get new extended size
			int newSize = buf.capacity() < MAX_KEY_SIZE ? MIN_BUF_SIZE : MAX_KEY_SIZE;
			// calculate total size
			newSize += buf.capacity();
			// allocate a new byte buffer
			ByteBuffer newBuf = ByteBuffer.allocate(newSize);
			// copy the entire buffer to new buffer
			newBuf.put((ByteBuffer) buf.flip());
			buf = newBuf;
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
		t = 1000L * 60 * 24 * 30 * 365 * 500;	
		System.out.println(Long.toBinaryString(t));
	}
}
