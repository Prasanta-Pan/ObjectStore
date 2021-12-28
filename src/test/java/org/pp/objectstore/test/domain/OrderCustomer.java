package org.pp.objectstore.test.domain;

import static org.pp.objectstore.test.Util.MALE;
import static org.pp.objectstore.test.Util.genChar;
import static org.pp.objectstore.test.Util.genDateToMilis;
import static org.pp.objectstore.test.Util.genDouble;
import static org.pp.objectstore.test.Util.genFloat;
import static org.pp.objectstore.test.Util.genIndex;
import static org.pp.objectstore.test.Util.genShort;
import static org.pp.objectstore.test.Util.genStatus;
import static org.pp.objectstore.test.Util.getAge;
import static org.pp.objectstore.test.Util.getEmail;
import static org.pp.objectstore.test.Util.getGender;
import static org.pp.objectstore.test.Util.getName;
import static org.pp.objectstore.test.Util.millisToDateString;

import java.util.Objects;

import org.pp.objectstore.interfaces.Collection;
import org.pp.objectstore.interfaces.Id;
import org.pp.objectstore.interfaces.SortKey;
import org.pp.objectstore.interfaces.Version;

@Collection("order_customer")
public class OrderCustomer implements Comparable<OrderCustomer> {
	@SortKey(2)
	private String customerEmail;
    //
	private String customerName;
	//
	private byte customerAge;
	//
	private boolean customerSex;
	//
	private double totalOrder;
	//
	private float unitPrice;
	//
	private short numOfUnits;
	// 
	private char recordStatus;
	//
	@SortKey(1) private long orderDate;
	//
	@Id private long orderId;
	// 
	@Version private int version;
	
	/**
	 * Default constructor must be defined  
	 */
	public OrderCustomer() {
		
	}
	
	/**
	 * Constructor to support to load method
	 * @param customerEmail
	 * @param orderDate
	 * @param orderId
	 */
	public OrderCustomer(String customerEmail, long orderDate,long orderId) {
		this.customerEmail = customerEmail;
	    this.orderDate = orderDate;
	    this.orderId = orderId;
	}
	
	/**
	 * To support create new Object
	 * @param customerEmail
	 * @param orderDate
	 * @param orderId
	 * @param customerName
	 * @param customerAge
	 * @param customerSex
	 * @param totalOrder
	 * @param numOfUnits
	 * @param unitPrice
	 * @param recordStatus
	 * @param version
	 */
	public OrderCustomer(String customerEmail, long orderDate, 
						 String customerName, byte customerAge,boolean customerSex, 
						 double totalOrder, short numOfUnits, float unitPrice, char recordStatus) 
	{
       this.customerEmail = customerEmail;
       this.orderDate = orderDate;
       this.customerName = customerName;
       this.customerAge = customerAge;
       this.customerSex = customerSex;
       this.numOfUnits = numOfUnits;
       this.recordStatus = recordStatus;
       this.totalOrder = totalOrder;
       this.unitPrice = unitPrice;       
	}
	
	/**
	 * Generate random customer order
	 * @param tot
	 * @return
	 * @throws Exception
	 */
	public static final CustomerOrder generate() throws Exception {
	    // generate random index person
		int index = genIndex();
		// get random customer name
		String custName = getName(index);
		// random customer email
		String custEmail = getEmail(index);
		// random age
		byte age = getAge(index);
		// random sex
		boolean sex = getGender(index);
		// random unit
		short unit = genShort();
		// random total
		double total = genDouble();
		// random price
		float price = genFloat();
		// random record status
		char status = genStatus();
		// random date
		long date = genDateToMilis();
		// create Customer order now and return
		return new CustomerOrder(custEmail, date, custName, age, sex, total, unit, price, status);				
	}
	
	/**
	 * Random update some field
	 */
	public void update() {
		// random unit
		numOfUnits = genShort();
		// random total
		totalOrder = genDouble();
		// random price
		unitPrice = genFloat();
		// random record status
		recordStatus = genChar();
	}
	
	public String getCustomerEmail() {
		return customerEmail;
	}
	public void setCustomerEmail(String customerEmail) {
		this.customerEmail = customerEmail;
	}
	public String getCustomerName() {
		return customerName;
	}
	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}
	public byte getCustomerAge() {
		return customerAge;
	}
	public void setCustomerAge(byte customerAge) {
		this.customerAge = customerAge;
	}
	public boolean isCustomerSex() {
		return customerSex;
	}
	public void setCustomerSex(boolean customerSex) {
		this.customerSex = customerSex;
	}
	public double getTotalOrder() {
		return totalOrder;
	}
	public void setTotalOrder(double totalOrder) {
		this.totalOrder = totalOrder;
	}
	public float getUnitPrice() {
		return unitPrice;
	}
	public void setUnitPrice(float unitPrice) {
		this.unitPrice = unitPrice;
	}
	public short getNumOfUnits() {
		return numOfUnits;
	}
	public void setNumOfUnits(short numOfUnits) {
		this.numOfUnits = numOfUnits;
	}
	public long getOrderDate() {
		return orderDate;
	}
	public void setOrderDate(long orderDate) {
		this.orderDate = orderDate;
	}
	public long getOrderId() {
		return orderId;
	}
	public void setOrderId(long orderId) {
		this.orderId = orderId;
	}
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}	
	public char getRecordStatus() {
		return recordStatus;
	}
	public void setRecordStatus(char recordStatus) {
		this.recordStatus = recordStatus;
	}		

	@Override
	public boolean equals(Object o) {
		if (o instanceof OrderCustomer) {
			OrderCustomer othr = (OrderCustomer) o;
			return 
				// compare customer email's
				Objects.equals(customerEmail, othr.customerEmail) &&
				// compare customer name's
				Objects.equals(customerName, othr.customerName) &&
				// compare customer age's
				customerAge == othr.customerAge &&
				// compare customer Sex
				customerSex == othr.customerSex &&
				// compare total orders
				totalOrder == othr.totalOrder &&
				// compare unit price
				unitPrice == othr.unitPrice &&
				// compare unit price
				numOfUnits == othr.numOfUnits &&
				// compare order date
				orderDate == othr.orderDate &&
				// compare order id
				orderId == othr.orderId &&
				// compare version
				version == othr.version && 
				// compare record status
				recordStatus == othr.recordStatus;				
		}
		return false;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		return sb.append("customerEmail=" + customerEmail + ",")
				 .append("orderDate=" + millisToDateString(orderDate) + ",")
				 .append("orderId=" + orderId + ",")
				 .append("customerName=" + customerName + ",")
				 .append("customerAge=" + customerAge + ",")
				 .append("customerSex=" + (customerSex == MALE ? "MALE" : "FEMALE") + ",")
				 .append("totalOrder=" + totalOrder + ",")
				 .append("unitPrice=" + unitPrice + ",")
				 .append("numOfUnits=" + numOfUnits + ",")
				 .append("recordStatus=" + recordStatus + ",")
				 .append("version=" + version)
				 .toString();
	}

	@Override
	public int compareTo(OrderCustomer o) {
		// compare order date first
		if (orderDate != o.orderDate)
			return orderDate > o.orderDate ? 1 : -1;
		int res = 0;
		// compare email first
		if ((res = customerEmail.compareTo(o.customerEmail)) != 0)
			return res;
		// compare order id now
	  	return orderId != o.orderId ? orderId > o.orderId ? 1 : -1 : 0;
	}	
	
}
