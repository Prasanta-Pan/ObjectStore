package org.pp.objectstore.test;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NavigableSet;

import org.pp.objectstore.test.domain.CustomerOrder;

public class ManualCustQryRandArith extends AbstractManualQuery<CustomerOrder> {
	/**
	 * 
	 * @param numOfParams
	 * @param set
	 */
	public ManualCustQryRandArith(int numOfParams, NavigableSet<CustomerOrder> set) {
		super(numOfParams, set);
		
	}
	// "(customerEmail != ? && orderDate between ?) || totalOrder > unitPrice * (5 + ?) / 2 || !customerSex)";
	@SuppressWarnings("unchecked")
	@Override
	public List<CustomerOrder> list() {
		// Allocate filter list
		List<CustomerOrder> fl = new LinkedList<>();
		// iterate now
		for (Iterator<CustomerOrder> itr = set.iterator(); itr.hasNext();) {
			// get next CustomerOrder
			CustomerOrder co = itr.next();
			// date range
			List<Long> dr = (List<Long>) params[1];
			if (!((!co.getCustomerEmail().equals((String) params[0]) && 
					(co.getOrderDate() >= dr.get(0) && co.getOrderDate() <= dr.get(1))) ||
					co.getTotalOrder() > co.getUnitPrice() * (5 + (int) params[2]) / 2 ||
					!co.isCustomerSex()))
				continue;
			// add to list
			fl.add(co);
		}
		// return list
		return fl;
	}	

}
