package org.pp.objectstore.test;

import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;

import org.pp.objectstore.test.domain.CustomerOrder;
import org.pp.qry.interfaces.ObjectIterator;
import org.pp.qry.interfaces.Query;

public abstract class AbstractManualQuery<T> implements Query<T> {
	/** User parameters */
	protected Object[] params = null;
	/** Parameter Index */
	protected int prmIndx = 0;
	/** Set of objects to filter */
	protected NavigableSet<T> set;
	
	/**
	 * 
	 * @param numOfParams
	 * @param set
	 */
	public AbstractManualQuery(int numOfParams, NavigableSet<T> set) {
		prmIndx = numOfParams;
		this.set = set;
		params = new Object[prmIndx];
	}
	
	protected Query<T> add(int pos, Object obj) {
		// check position 
		pos = pos -1;
		if (pos < 0 || pos >= prmIndx)
			throw new RuntimeException("Invalid parameter index: " + pos);
		// add node
		params[pos] = obj;
		// 
		return this;			
	}
	
	@Override
	public Query<T> setParam(int pos, int val) {
		// TODO Auto-generated method stub
		// add to parameter list
		return add(pos, val);
	}

	@Override
	public Query<T> setParam(int pos, long val) {
		// TODO Auto-generated method stub
		return add(pos, val);
	}

	@Override
	public Query<T> setParam(int pos, float val) {
		// TODO Auto-generated method stub
		return add(pos, val);
	}

	@Override
	public Query<T> setParam(int pos, double val) {
		// TODO Auto-generated method stub
		return add(pos, val);
	}

	@Override
	public Query<T> setParam(int pos, String val) {
		// TODO Auto-generated method stub
		return add(pos, val);
	}

	@Override
	public Query<T> setParam(int pos, boolean val) {
		// TODO Auto-generated method stub
		return add(pos, val);
	}

	@Override
	public Query<T> setParam(int pos, List<?> list) {
		// TODO Auto-generated method stub
		return add(pos, list);
	}

	@Override
	public Query<T> setParam(int pos, Set<?> set) {
		// TODO Auto-generated method stub
		return add(pos, set);
	}

	@Override
	public Query<T> setReverseOrder(boolean rev) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public T get() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public List<T> list() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public ObjectIterator<T> iterator() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Validate both list
	 * @param lset
	 * @param rset
	 */
	public void validate(List<CustomerOrder> lset, List<CustomerOrder> rset) {
		// open iterator
		Iterator<CustomerOrder> itr = lset.iterator();
		// open set iterator
		Iterator<CustomerOrder> setItr = rset.iterator();
		// if count miss match
		if (lset.size() != rset.size()) {
			// throw exception of not match
			throw new RuntimeException("Order count miss match, set: " + lset.size() + ", store: " + rset.size());
		}
		// Validate orders
		while (itr.hasNext() && setItr.hasNext()) {
			// get next order from set
			CustomerOrder coset = setItr.next();
			// get next order from store
			CustomerOrder costore = itr.next();
			// compare
			if (!coset.equals(costore)) {
				// throw exception of not match
				throw new RuntimeException("Orders miss match, set: " + coset + ", store: " + costore);
			}			
		}
		
		// total record read
		System.out.println("Total orders validated: " + lset.size());		
	}
}
