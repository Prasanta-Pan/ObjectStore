package org.pp.objectstore.test;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;

import org.pp.objectstore.test.domain.CustomerOrder;

public class ManualCustQryEmailNotIn extends AbstractManualQuery<CustomerOrder> {
	/**
	 * 
	 * @param numOfParams
	 * @param set
	 */
	public ManualCustQryEmailNotIn(int numOfParams, NavigableSet<CustomerOrder> set) {
		super(numOfParams, set);
		
	}
	// "customerEmail nin ? && ((totalOrder < ? && numOfUnits > ?) || !customerSex)";
	@SuppressWarnings("unchecked")
	@Override
	public List<CustomerOrder> list() {
		// Allocate filter list
		List<CustomerOrder> fl = new LinkedList<>();
		// iterate now
		for (Iterator<CustomerOrder> itr = set.iterator(); itr.hasNext();) {
			// get next CustomerOrder
			CustomerOrder co = itr.next();
			// email set
			Set<String> toSet = (Set<String>) params[0];
			if (!(!toSet.contains(co.getCustomerEmail()) && 
					((co.getTotalOrder() < (double) params[1] && co.getNumOfUnits() > (int) params[2]) || !co.isCustomerSex())))
				continue;
			// add to list
			fl.add(co);
		}
		// return list
		return fl;
	}	

}
