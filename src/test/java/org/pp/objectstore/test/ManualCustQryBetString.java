package org.pp.objectstore.test;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NavigableSet;

import org.pp.objectstore.test.domain.CustomerOrder;

public class ManualCustQryBetString extends AbstractManualQuery<CustomerOrder> {
	/**
	 * 
	 * @param numOfParams
	 * @param set
	 */
	public ManualCustQryBetString(int numOfParams, NavigableSet<CustomerOrder> set) {
		super(numOfParams, set);
		
	}
	// "customerEmail between ? && (orderDate < ? || (totalOrder <= unitPrice * (? - 5) % 2 && !customerSex))";
	@SuppressWarnings("unchecked")
	@Override
	public List<CustomerOrder> list() {
		// Allocate filter list
		List<CustomerOrder> fl = new LinkedList<>();
		// iterate now
		for (Iterator<CustomerOrder> itr = set.iterator(); itr.hasNext();) {
			// get next CustomerOrder
			CustomerOrder co = itr.next();
			// validate order date
			List<String> dr = (List<String>) params[0];
			// compare
			if (!((co.getCustomerEmail().compareTo(dr.get(0)) >= 0 && co.getCustomerEmail().compareTo(dr.get(1)) <= 0) && 
				(co.getOrderDate() < (long) params[1] || (co.getTotalOrder() <= co.getUnitPrice() * ((int) params[2] - 5) % 2 &&
				!co.isCustomerSex()))))
				continue;
			// add to list
			fl.add(co);
		}
		// return list
		return fl;
	}
}
