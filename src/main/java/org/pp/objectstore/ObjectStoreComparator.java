package org.pp.objectstore;

import static org.pp.objectstore.DataTypes.D_TYP_DBL;
import static org.pp.objectstore.DataTypes.D_TYP_FLT;
import static org.pp.objectstore.DataTypes.D_TYP_INT;
import static org.pp.objectstore.DataTypes.D_TYP_LNG;
import static org.pp.objectstore.DataTypes.D_TYP_STR;

import java.nio.ByteBuffer;
import java.util.Comparator;

/**
 * Object store specific comparator
 * Key lay out
 * 	[type(1B)][len(4B)][collection name][type(1B)][len(4B)]?[SORT_KEY1]....type(1B)][len(4B)]?[SORT_KEY2]
 * @author prasantsmac
 *
 */
final class ObjectStoreComparator implements Comparator<byte[]> {
	
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
