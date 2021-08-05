package org.pp.objectstore.interfaces;

public final class LongWrapper {
	/**
	 * Wrapped long value
	 */
	private long val;
	/**
	 * 
	 */
	public LongWrapper() { }
	/**
	 * 
	 * @param val
	 */
	public LongWrapper(long val) {
		this.val = val;
	}

	public long getVal() {
		return val;
	}

	public LongWrapper setVal(long val) {
		this.val = val;
		return this;
	}

	@Override
	public int hashCode() {
		return (int) val;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof LongWrapper) {
			LongWrapper o = (LongWrapper) other;
			return o.val == val;
		}
		return false;
	}
}
