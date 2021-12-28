package org.pp.objectstore.test;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NavigableSet;
import java.util.SortedSet;

import org.pp.objectstore.test.domain.CustomerOrder;

public class ManualCustQryNotBet extends AbstractManualQuery<CustomerOrder> {
	/**
	 * 
	 * @param numOfParams
	 * @param set
	 */
	public ManualCustQryNotBet(int numOfParams, NavigableSet<CustomerOrder> set) {
		super(numOfParams, set);
		
	}
	// "customerEmail = ? && orderDate nbet ? && ((totalOrder > ? && numOfUnits > ?) || !customerSex)";
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
			// validate order date
			List<Long> dr = (List<Long>) params[1];
			if ((co.getOrderDate() >= dr.get(0) && co.getOrderDate() <= dr.get(1)))
				continue;
			// validate total order
			if (!((co.getTotalOrder() > (double) params[2] && co.getNumOfUnits() > (int) params[3]) || !co.isCustomerSex()))
				continue;
			// add to list
			fl.add(co);
		}
		// return list
		return fl;
	}	

}
