package org.pp.objectstore.test;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedSet;

import org.pp.objectstore.test.domain.CustomerOrder;

public class ManualCustQryIn extends AbstractManualQuery<CustomerOrder> {
	/**
	 * 
	 * @param numOfParams
	 * @param set
	 */
	public ManualCustQryIn(int numOfParams, NavigableSet<CustomerOrder> set) {
		super(numOfParams, set);
		
	}
	// "customerEmail = ? && (totalOrder in ? || (orderDate > ? && numOfUnits > ?))";
	@SuppressWarnings("unchecked")
	@Override
	public List<CustomerOrder> list() {
		//get tail set
		SortedSet<CustomerOrder> lSet = set.tailSet(new CustomerOrder((String) params[0], 0, 0));
		// Allocate filter list
		List<CustomerOrder> fl = new LinkedList<>();
		// iterate now
		for (Iterator<CustomerOrder> itr = lSet.iterator(); itr.hasNext();) {
			// get next CustomerOrder
			CustomerOrder co = itr.next();
			// compare email
			if (!co.getCustomerEmail().equals((String) params[0]))
				break;
			// total order set
			Set<Double> toSet = (Set<Double>) params[1];
			if (!(toSet.contains(co.getTotalOrder()) || 
					(co.getOrderDate() > (long) params[2] && co.getNumOfUnits() > (int) params[3])))
				continue;
			// add to list
			fl.add(co);
		}
		// return list
		return fl;
	}	

}
