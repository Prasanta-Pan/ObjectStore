package org.pp.objectstore.test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

import org.pp.objectstore.interfaces.Collection;
import org.pp.objectstore.interfaces.Id;
import org.pp.objectstore.interfaces.SortKey;
import org.pp.objectstore.interfaces.Version;

@Collection()
public class OrderLine {
	// Record os active
	public static final boolean ACTIVE = true;
	// Record is de active
	public static final boolean DEACTIVE = false;

	// Predefined customers
	private static final String[] emails = new String[] { "pan.prasanta@gmail.com", "pan.maumita@gmail.com",
			"pan.moana@gmail.com" };
	// To support random order numbers
	private static final int[] noOrders = new int[] { 5, 10, 15, 20, 31, 50 };
	//
	private static final float[] prices = new float[] { 5.68f, 6.87f, 7.89f, 10.56f, 15.30f };
	//
	private static final double[] totals = new double[] { 100.50d, 500.70d, 1002.765d, 5000.734d, 700.76d, 300.5d };
	//
	private static final String[] dates = new String[] { "2018-09-18", "2018-09-21", "2018-09-29", "2020-09-18",
			"2020-11-07", "2020-05-11" };
	// Product ids
	private static final int[] prodIds = new int[] { 5, 7, 11, 15, 21, 40, 55 };
	// Products name
	private static final String[] prodNames = new String[] { "TV", "Fridge", "Watch", "Laptop", 
			"Mobile", "Speaker", "HeadPhone" };
	//
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	@SortKey(1)
	private long orderDate;
	@SortKey(2)
	private int productId;
	@Id()
	private long lineId;
	// Product name
	private String productName;
	//
	private String customerEmail;
	// Number of order lines
	private int numOfOrder;
	// number of units
	private int numOfUnits;
	// Unit price
	private float unitPrice;
	// Total price of the orders
	private double total;
	// Order status (Active or DeActive)
	private boolean orderStatus;
	// version number of record
	@Version
	private int version;
	
	/**
	 * Default constructor
	 */
	public OrderLine() {
		
	}
	
	/**
	 * Constructor to support find
	 */
	public OrderLine(long orderDate, int productId, long lineId) {
		this.orderDate = orderDate;
		this.productId = productId;
		this.lineId = lineId;
	}
	
	/**
	 * Generate random OrderLine
	 * @return
	 */
	public static OrderLine genOrderLine() throws Exception {
		ThreadLocalRandom ran = ThreadLocalRandom.current();
		OrderLine ol = new OrderLine();
		// random customer email
		ol.customerEmail = emails[ran.nextInt(0, emails.length)];
		// random order
		ol.numOfOrder = noOrders[ran.nextInt(0, noOrders.length)];
		// random units
		ol.numOfUnits = noOrders[ran.nextInt(0, noOrders.length)];
		// random unit price
		ol.unitPrice = prices[ran.nextInt(0, prices.length)];
		// random total
		ol.total = totals[ran.nextInt(0, totals.length)];
		// random product ids
		ol.productId = prodIds[ran.nextInt(0, prodIds.length)];
		// random product names
		ol.productName = prodNames[ran.nextInt(0, prodNames.length)];
		// if record active or not
		ol.orderStatus = ACTIVE;
		// random order date
		ol.orderDate = dateToMillis(dates[ran.nextInt(0, dates.length)]);
		return ol;
	}
	
	/**
	 * Convert String date to millis
	 * @param date
	 * @return
	 */
	public static long dateToMillis(String date) throws Exception {
		return sdf.parse(date).getTime();
	}
	/**
	 * Convert milis to date String
	 * @param date
	 * @return
	 */
	public static String millisToString(long date) {
		return sdf.format(new Date(date));
	}
	
    	
	public long getOrderDate() {
		return orderDate;
	}

	public int getProductId() {
		return productId;
	}

	public long getLineId() {
		return lineId;
	}

	public String getProductName() {
		return productName;
	}

	public String getCustomerEmail() {
		return customerEmail;
	}

	public int getNumOfOrder() {
		return numOfOrder;
	}

	public int getNumOfUnits() {
		return numOfUnits;
	}

	public float getUnitPrice() {
		return unitPrice;
	}

	public double getTotal() {
		return total;
	}

	public boolean isOrderStatus() {
		return orderStatus;
	}

	public int getVersion() {
		return version;
	}
		
	public void setOrderDate(long orderDate) {
		this.orderDate = orderDate;
	}

	public void setProductId(int productId) {
		this.productId = productId;
	}

	public void setLineId(long lineId) {
		this.lineId = lineId;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public void setCustomerEmail(String customerEmail) {
		this.customerEmail = customerEmail;
	}

	public void setNumOfOrder(int numOfOrder) {
		this.numOfOrder = numOfOrder;
	}

	public void setNumOfUnits(int numOfUnits) {
		this.numOfUnits = numOfUnits;
	}

	public void setUnitPrice(float unitPrice) {
		this.unitPrice = unitPrice;
	}

	public void setTotal(double total) {
		this.total = total;
	}

	public void setOrderStatus(boolean orderStatus) {
		this.orderStatus = orderStatus;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		return sb.append("orderDate=" + millisToString(orderDate) + ",").append("productId=" + productId + ",")
				 .append("lineId=" + lineId + ",").append("productName=" + productName + ",")
				 .append("customerEmail=" + customerEmail + ",").append("numOfUnits=" + numOfUnits + ",")
				 .append("total=" + total + ",").append("unitPrice=" + unitPrice + ",")
				 .append("numOfOrder=" + numOfOrder + ",").append("orderStatus=" + orderStatus + ",")
				 .append("version=" + version).toString();
	}
}
