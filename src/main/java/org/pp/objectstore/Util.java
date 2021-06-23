package org.pp.objectstore;

import static java.lang.reflect.Modifier.isFinal;
import static java.lang.reflect.Modifier.isStatic;
import static java.lang.reflect.Modifier.isTransient;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.pp.objectstore.interfaces.AbstractFieldAccessor;
import org.pp.objectstore.interfaces.Collection;
import org.pp.objectstore.interfaces.FieldAccessor;
import org.pp.objectstore.interfaces.Id;
import org.pp.objectstore.interfaces.SortKey;
import org.pp.objectstore.interfaces.Transient;

class Util {
	/**
	 * Data type integer
	 */
	static final byte D_TYP_INT = 0;
	/**
	 * Data type long
	 */
	static final byte D_TYP_LNG = 1;
	/**
	 * Data type string
	 */
	static final byte D_TYP_STR = 2;
	/**
	 * Data type float
	 */
	static final byte D_TYP_FLT = 3;
	/**
	 * Data type double
	 */
	static final byte D_TYP_DBL = 4;
	/**
	 * Data type boolean
	 */
	static final byte D_TYP_BOL = 5;
	/**
	 * Data type short
	 */
	static final byte D_TYP_SHRT = 6;
	/**
	 * Data type byte
	 */
	static final byte D_TYP_BYTE = 7;
	/**
	 * Data type character
	 */
	static final byte D_TYP_CHAR = 8;
	/**
	 * Max key length supported
	 */
	static final int MAX_KEY_SZ = 128;
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
	 * Field Accessor
	 */
	static final Map<String, Byte> supportedTypes = new HashMap<>();
	/**
	 * Object store comparator
	 */
	static final MyComp comp = new MyComp();

	/**
	 * Initialise supported types
	 */
	static {
		//
		supportedTypes.put("int", D_TYP_INT);
		supportedTypes.put("long", D_TYP_LNG);
		supportedTypes.put("float", D_TYP_FLT);
		supportedTypes.put("double", D_TYP_DBL);
		supportedTypes.put("short", D_TYP_SHRT);
		supportedTypes.put("byte", D_TYP_BYTE);
		supportedTypes.put("String", D_TYP_STR);
		supportedTypes.put("char", D_TYP_CHAR);
		supportedTypes.put("boolean", D_TYP_BOL);
		// some other non primitive types can be added in future
	}

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
	 * Build String from byte buffer
	 * @param buf
	 * @return
	 */
	static final String byteBufferToString(ByteBuffer buf) {
		// remove type
		if (buf.get() != D_TYP_STR)
			invalidType(null);
		// get char length		
		int len = buf.getInt();
		char[] chars = new char[len];
		for (int i = 0; i < len; i++)
			chars[i] = buf.getChar();
		// 
		return new String(chars);
	} 
	

	/**
	 * Get all the declared fields and put to map
	 * 
	 * @param clazz
	 */
	static final List<FieldAccessor> init(Class<?> clazz, Map<String, FieldAccessor> m, AtomicBoolean genId) {
		// default regular field
		FieldAccessor id = null, sk = null;
		// get all declared fields of a class
		Field[] flds = clazz.getDeclaredFields();
		for (Field fld : flds) {
			// skip constant
			if (fld.isEnumConstant() || fld.isAnnotationPresent(Transient.class))
				continue;
			// get modifiers 
			int mod = fld.getModifiers();
			// skip static and final
			if (isStatic(mod) || isFinal(mod) || isTransient(mod))
				continue;
			// check if field type supported persistence or not
			String tNm = fld.getType().getSimpleName();
			if (!supportedTypes.containsKey(tNm))
				continue;
			// make it accessible
			fld.setAccessible(true);
			// get type number
			byte type = supportedTypes.get(tNm);
			// get field accessor
			FieldAccessor fa = getFieldAccessor(type, fld);
			// add field to map
			m.put(fld.getName(), fa);
			// Check @SortKey
			if (fld.isAnnotationPresent(SortKey.class)) {
				if (sk != null)
				   throw new RuntimeException("More than one @SortKey is not allowed");
				// check field type
				if (type > D_TYP_DBL)
					throw new RuntimeException("Unsupported SortKey type, allowed type: int/long/float/double/String");
				sk = fa;
				continue;
			}
			// @Id check
			if (fld.isAnnotationPresent(Id.class)) {
				if (id != null)
					throw new RuntimeException("More than one @Id is not allowed");
				// check id type
				if (type > D_TYP_STR)
					throw new RuntimeException("Unsupported ID type, allowed type: int/long/String");
				// verify ID generation type
				boolean gen = fld.getAnnotation(Id.class).gen();				
				if (gen && type != D_TYP_LNG)
					throw new RuntimeException("Id generation is not supported on type other than long");
				// if gen required
				if (gen)
					genId.set(gen);
				id = fa;				
			}			
		}
		// check if ID was provided
		if (id == null)
			throw new RuntimeException("No @Id was provided");
		// key list to be returned
		List<FieldAccessor> keys = new ArrayList<>(2);
		// if sort key was provided
		if (sk != null)
			keys.add(sk);
		// id id field
		keys.add(id);
		// return key list
		return keys;
	}

	/**
	 * Build byte array key
	 * 
	 * @param cName
	 * @param keys
	 * @return
	 */
	static final byte[] buildKey(String cname, List<FieldAccessor> keys, Object target) throws Exception {
		// allocate enough memory
		ByteBuffer buf = ByteBuffer.allocate(MAX_KEY_SZ);
		// serialise collection name
		buf = stringToByteBuffer(cname, buf);
		// build key
		for (FieldAccessor fld : keys) 
			buf = fld.get(buf, target);
		
		// check if key size exceeded
		if (buf.position() > MAX_KEY_SZ)
			keySizeIssue();
		// extract key
		return extract(buf);
	}
	/**
	 * To build key for query 
	 * @param cname
	 * @param sk
	 * @param val
	 * @return
	 * @throws Exception
	 */
	static final byte[] buildKey(String cname, FieldAccessor sk, Object val) throws Exception {
		// allocate enough memory
		ByteBuffer buf = ByteBuffer.allocate(MAX_KEY_SZ);
		// serialise collection name
		buf = stringToByteBuffer(cname, buf);
		// validate and get sort value bytes
		buf = sk.validateType(buf, val);
		
		// check if key size exceeded
		if (buf.position() > MAX_KEY_SZ)
			keySizeIssue();
		// extract key
		return extract(buf);		
	}
	
	/**
	 * Short version of build key
	 * @param cname
	 * @return
	 * @throws Exception
	 */
	static final byte[] buildKey(String cname) throws Exception {
		// allocate enough memory
		ByteBuffer buf = ByteBuffer.allocate(MAX_KEY_SZ);
		// serialise collection name
		buf = stringToByteBuffer(cname, buf);
		// check if key size exceeded
		if (buf.position() > MAX_KEY_SZ)
			keySizeIssue();
		// extract key
		return extract(buf);	
	}
	
	/**
	 * Generate and set ID
	 * @param fa
	 * @throws Exception
	 */
	static final void setId(FieldAccessor id, Object target) throws Exception {
		Field fld = id.getField();
		fld.setLong(target, genId());		
	}

	/**
	 * Serialise object
	 * 
	 * @param cName
	 * @param keys
	 * @param target
	 * @return
	 * @throws Exception
	 */
	static final byte[] serialize(Map<String, FieldAccessor> m, Object target)
			throws Exception {
		// allocate enough memory
		ByteBuffer buf = ByteBuffer.allocate(MAX_KEY_SZ);		
		// serialise rest of the fields
		for (Map.Entry<String, FieldAccessor> e : m.entrySet()) {
			buf = stringToByteBuffer(e.getKey(), buf);
			// now value
			buf = e.getValue().get(buf, target);
		}
		// return serialise data
		return extract(buf);
	}
	
	/**
	 * Deserialise object
	 * 
	 * @param value
	 * @param keys
	 * @param m
	 * @param target
	 * @throws Exception
	 */
	static final Object deSerialize(byte[] value, 
								  Map<String, FieldAccessor> m, 
								  Object target) throws Exception {
		// get a byte buffer
		ByteBuffer buf = ByteBuffer.wrap(value);		
		// de serialise object
		while (buf.hasRemaining()) {
			// decode to string
			String fNm = byteBufferToString(buf);
			// get corresponding field accessor
			FieldAccessor fa = m.get(fNm);
			if (fa == null)
				throw new RuntimeException("Field name '" + fNm + "' is missing in class " + target.getClass());
			// set field
			fa.set(buf, target);			
		} 
		return target;
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
	 * Throw exception of invalid type
	 */
	static final void invalidType(Field fld) {
		throw new RuntimeException("Invalid data type for the field '" + fld.getName() + "'");
	}
	/**
	 * 
	 */
	static final void keySizeIssue() {
		throw new RuntimeException("Key size can not be more than " + MAX_KEY_SZ);
	}
	/**
	 * Next higher string lexicographically
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

	/**
	 * Object store specific comparator
	 * 
	 * @author prasantsmac
	 *
	 */
	static final class MyComp implements Comparator<byte[]> {
		@Override
		public int compare(byte[] o1, byte[] o2) {
			// Convert to byte buffer first
			ByteBuffer l = ByteBuffer.wrap(o1);
			ByteBuffer r = ByteBuffer.wrap(o2);
			// start comparing
			while (l.hasRemaining() && r.hasRemaining()) {
				byte a = l.get();
				byte b = r.get();
				// check if data type matches or not
				if (a != b)
					throw new RuntimeException("Data types doesn't matched while comparing keys");
				// handle data type specific comparison
				switch (b) {
					case D_TYP_INT:
						int li = l.getInt();
						int ri = r.getInt();
						if (li != ri) {
							return li > ri ? 1 : -1;
						}
						break;
					case D_TYP_STR:
						int lcl = l.getInt() * 2 + l.position();
						int rcl = r.getInt() * 2 + r.position();
						// compare collection name lexicographically
						while (l.position() < lcl && r.position() < rcl) {
							char ac = l.getChar();
							char bc = r.getChar();
							if (ac != bc) {
								return ac > bc ? 1 : -1;
							}
						}						
						break;
					case D_TYP_LNG:
						long ll = l.getLong();
						long rl = r.getLong();
						// if not equal
						if (ll != rl) {
							return ll > rl ? 1 : -1;
						}
						break;
					case D_TYP_FLT:
						float lf = l.getFloat();
						float rf = r.getFloat();
						// if not equal
						if (lf != rf) {
							return lf > rf ? 1 : -1;
						}
						break;
					case D_TYP_DBL:
						double ld = l.getDouble();
						double rd = r.getDouble();
						if (ld != rd) {
							return ld > rd ? 1 : -1;
						}
						break;					
					default:
						throw new RuntimeException("Invalid data type");
				}
			}	
			// 
			return o1.length - o2.length;
		}		
	}

	/**
	 * Instantiate proper field accessor of type
	 * 
	 * @param type
	 * @param fld
	 * @return
	 */
	static final FieldAccessor getFieldAccessor(byte type, Field fld) {
		switch (type) {
		case D_TYP_INT:
			return new IntFieldAccessor(fld);
		case D_TYP_LNG:
			return new LongFieldAccessor(fld);
		case D_TYP_FLT:
			return new FloatFieldAccessor(fld);
		case D_TYP_DBL:
			return new DoubleFieldAccessor(fld);
		case D_TYP_STR:
			return new StringFieldAccessor(fld);
		case D_TYP_BOL:
			return new BooleanFieldAccessor(fld);
		case D_TYP_SHRT:
			return new ShortFieldAccessor(fld);
		case D_TYP_BYTE:
			return new ByteFieldAccessor(fld);
		case D_TYP_CHAR:
			return new CharFieldAccessor(fld);
		default:
			throw new RuntimeException("Unknown type");
		}
	}

	/**
	 * INT Field Accessor
	 * 
	 * @author prasantsmac
	 *
	 */
	static final class IntFieldAccessor extends AbstractFieldAccessor {
		protected IntFieldAccessor(Field fld) {
			super(fld);			
		}
        // common
		private ByteBuffer put(ByteBuffer buf, int val) {
			buf = extend(buf, 5);
			buf.put(D_TYP_INT);
			buf.putInt(val);
			return buf;
		}
		@Override
		public ByteBuffer set(ByteBuffer buf, Object target) throws Exception {
			if (buf.get() != D_TYP_INT)
				invalidType(fld);
			fld.setInt(target, buf.getInt());
			return buf;
		}

		@Override
		public ByteBuffer get(ByteBuffer buf, Object target) throws Exception {
			int val = fld.getInt(target);
			return put(buf, val);
		}

		@Override
		public Object get(Object target) throws Exception {
			return fld.getInt(target);
		}

		@Override
		public ByteBuffer validateType(ByteBuffer buf, Object val) {
			if (!(val instanceof Integer))
				invalidType(fld);
			int v = (int) val;
			return put(buf, v);
		}

	}

	/**
	 * Long field accessor
	 * 
	 * @author prasantsmac
	 *
	 */
	static final class LongFieldAccessor extends AbstractFieldAccessor {
		protected LongFieldAccessor(Field fld) {
			super(fld);			
		}
		// common
		private ByteBuffer put(ByteBuffer buf, long val) {
			buf = extend(buf, 9);
			buf.put(D_TYP_LNG);
			buf.putLong(val);
			return buf;
		}
		
		@Override
		public ByteBuffer set(ByteBuffer buf, Object target) throws Exception {
			if (buf.get() != D_TYP_LNG)
				invalidType(fld);
			fld.setLong(target, buf.getLong());
			return buf;
		}

		@Override
		public ByteBuffer get(ByteBuffer buf, Object target) throws Exception {
			long val = fld.getLong(target);
			return put(buf, val);		
		}

		@Override
		public Object get(Object target) throws Exception {
			return fld.getLong(target);
		}

		@Override
		public ByteBuffer validateType(ByteBuffer buf, Object val) {
			if (!(val instanceof Long))
				invalidType(fld);
			long v = (long) val;
			return put(buf, v);
		}	

	}

	/**
	 * Float field accessor
	 * 
	 * @author prasantsmac
	 *
	 */
	static final class FloatFieldAccessor extends AbstractFieldAccessor {
		protected FloatFieldAccessor(Field fld) {
			super(fld);			
		}
		// common
		private ByteBuffer put(ByteBuffer buf, float val) {
			buf = extend(buf, 5);
			buf.put(D_TYP_FLT);
			buf.putFloat(val);
			return buf;
		}
		@Override
		public ByteBuffer set(ByteBuffer buf, Object target) throws Exception {
			if (buf.get() != D_TYP_FLT)
				invalidType(fld);
			fld.setFloat(target, buf.getFloat());
			return buf;
		}

		@Override
		public ByteBuffer get(ByteBuffer buf, Object target) throws Exception {
			float val = fld.getFloat(target);
			return put(buf, val);	
		}

		@Override
		public Object get(Object target) throws Exception {
			return fld.getFloat(target);
		}

		@Override
		public ByteBuffer validateType(ByteBuffer buf, Object val) {
			if (!(val instanceof Float))
				invalidType(fld);
			float v = (float) val;
			return put(buf, v);
		}

	}

	/**
	 * Double field accessor
	 * 
	 * @author prasantsmac
	 *
	 */
	static final class DoubleFieldAccessor extends AbstractFieldAccessor {
			
		protected DoubleFieldAccessor(Field fld) {
			super(fld);			
		}
		// common
		private ByteBuffer put(ByteBuffer buf, double val) {
			buf = extend(buf, 9);
			buf.put(D_TYP_DBL);
			buf.putDouble(val);
			return buf;
		}

		@Override
		public ByteBuffer set(ByteBuffer buf, Object target) throws Exception {
			if (buf.get() != D_TYP_DBL)
				invalidType(fld);
			fld.setDouble(target, buf.getDouble());
			return buf;
		}

		@Override
		public ByteBuffer get(ByteBuffer buf, Object target) throws Exception {
			double val = fld.getDouble(target);
			return put(buf, val);	
		}

		@Override
		public Object get(Object target) throws Exception {
			return fld.getDouble(target);
		}

		@Override
		public ByteBuffer validateType(ByteBuffer buf, Object val) {
			if (!(val instanceof Double))
				invalidType(fld);
			double v = (double) val;
			return put(buf, v);
		}		

	}

	/**
	 * Short field accessor
	 * 
	 * @author prasantsmac
	 *
	 */
	static final class ShortFieldAccessor extends AbstractFieldAccessor {
		protected ShortFieldAccessor(Field fld) {
			super(fld);			
		}
		// common
		private ByteBuffer put(ByteBuffer buf, short val) {
			buf = extend(buf, 3);
			buf.put(D_TYP_SHRT);
			buf.putShort(val);
			return buf;
		}
		@Override
		public ByteBuffer set(ByteBuffer buf, Object target) throws Exception {
			if (buf.get() != D_TYP_SHRT)
				invalidType(fld);
			fld.setShort(target, buf.getShort());
			return buf;
		}

		@Override
		public ByteBuffer get(ByteBuffer buf, Object target) throws Exception {
			short val = fld.getShort(target);
			return put(buf, val);	
		}

		@Override
		public Object get(Object target) throws Exception {
			return (int) fld.getShort(target);
		}

		@Override
		public ByteBuffer validateType(ByteBuffer buf, Object val) {
			invalidType(fld);
		    return null;
		}		

	}

	/**
	 * Byte field accessor
	 * 
	 * @author prasantsmac
	 *
	 */
	static final class ByteFieldAccessor extends AbstractFieldAccessor {
		protected ByteFieldAccessor(Field fld) {
			super(fld);			
		}
		// common
		private ByteBuffer put(ByteBuffer buf, byte val) {
			buf = extend(buf, 2);
			buf.put(D_TYP_BYTE);
			buf.put(val);
			return buf;
		}
		@Override
		public ByteBuffer set(ByteBuffer buf, Object target) throws Exception {
			if (buf.get() != D_TYP_BYTE)
				invalidType(fld);
			fld.setByte(target, buf.get());
			return buf;
		}

		@Override
		public ByteBuffer get(ByteBuffer buf, Object target) throws Exception {
			byte val = fld.getByte(target);
			return put(buf, val);	
		}

		@Override
		public Object get(Object target) throws Exception {
			return (int) fld.getByte(target);
		}

		@Override
		public ByteBuffer validateType(ByteBuffer buf, Object val) {
			invalidType(fld);
		    return null;
		}
		
	}

	/**
	 * String field accessor
	 * 
	 * @author prasantsmac
	 *
	 */
	static final class StringFieldAccessor extends AbstractFieldAccessor {
		
		protected StringFieldAccessor(Field fld) {
			super(fld);			
		}
			
		@Override
		public ByteBuffer set(ByteBuffer buf, Object target) throws Exception {
			String str = byteBufferToString(buf);
			fld.set(target, str);
			return buf;
		}

		@Override
		public ByteBuffer get(ByteBuffer buf, Object target) throws Exception {
			String str = (String) fld.get(target);
			return stringToByteBuffer(str, buf);
		}

		@Override
		public Object get(Object target) throws Exception {
			return (String) fld.get(target);
		}

		@Override
		public ByteBuffer validateType(ByteBuffer buf, Object val) {
			if (!(val instanceof String))
				invalidType(fld);
			String v = (String) val;
			return stringToByteBuffer(v, buf);
		}		

	}

	/**
	 * Character field accessor
	 * 
	 * @author prasantsmac
	 *
	 */
	static final class CharFieldAccessor extends AbstractFieldAccessor {
		protected CharFieldAccessor(Field fld) {
			super(fld);			
		}
		// common
		private ByteBuffer put(ByteBuffer buf, char val) {
			buf = extend(buf, 3);
			buf.put(D_TYP_CHAR);
			buf.putChar(val);
			return buf;
		}
		
		@Override
		public ByteBuffer set(ByteBuffer buf, Object target) throws Exception {
			if (buf.get() != D_TYP_CHAR)
				invalidType(fld);
			fld.setChar(target, buf.getChar());
			return buf;
		}

		@Override
		public ByteBuffer get(ByteBuffer buf, Object target) throws Exception {
			char val = fld.getChar(target);
			return put(buf, val);	
		}

		@Override
		public Object get(Object target) throws Exception {
			return fld.getChar(target) + "";
		}

		@Override
		public ByteBuffer validateType(ByteBuffer buf, Object val) {
			invalidType(fld);
		    return null;
		}		

	}

	/**
	 * Boolean field accessor
	 * 
	 * @author prasantsmac
	 *
	 */
	static final class BooleanFieldAccessor extends AbstractFieldAccessor {
		protected BooleanFieldAccessor(Field fld) {
			super(fld);			
		}
		// common
		private ByteBuffer put(ByteBuffer buf, boolean val) {
			buf = extend(buf, 2);
			buf.put(D_TYP_BOL);
			buf.put( val ? (byte) 1 : 0);
			return buf;
		}
		
		@Override
		public ByteBuffer set(ByteBuffer buf, Object target) throws Exception {
			if (buf.get() != D_TYP_BOL)
				invalidType(fld);
			fld.setBoolean(target, buf.get() != 0 ? true : false);
			return buf;
		}

		@Override
		public ByteBuffer get(ByteBuffer buf, Object target) throws Exception {
			boolean val = fld.getBoolean(target);
			return put(buf, val);	
		}

		@Override
		public Object get(Object target) throws Exception {
			return fld.getBoolean(target);
		}

		@Override
		public ByteBuffer validateType(ByteBuffer buf, Object val) {
			invalidType(fld);
		    return null;
		}	

	}

	public static void main(String[] args) {
		long t = System.currentTimeMillis();
		System.out.println(Long.toBinaryString(t));
	}
}
