package org.pp.objectstore.test.domain;

import java.util.Date;

import org.pp.objectstore.interfaces.Collection;
import org.pp.objectstore.interfaces.Id;
import org.pp.objectstore.interfaces.SortKey;
import org.pp.objectstore.interfaces.Version;

@Collection("order")
public class OldOrder {
	// First Sort Key is based on customer email
	@SortKey(1)
	private String email;
	//
	@Id
	private long orderId;
	//
	private int noOrders;
	//
	private float unitPrice;
	//
	private int total;
	// Second sort key
	@SortKey(2)
	private long orderDate;
	// Version field for atomic update
	@Version
	private int ver;

	public OldOrder() {
	}
	
    /**
     * Constructor to find whole object
     * @param email
     * @param oDate
     * @param orderId
     */
	public OldOrder(String email, long oDate, long orderId) {
		this.email = email;
		this.orderDate = oDate;
		this.orderId = orderId;
	}
	
	/**
	 * To create new Order object
	 * @param email
	 * @param noOrders
	 * @param unitPrice
	 * @param total
	 * @param orderDate
	 */
	public OldOrder(String email, int noOrders, float unitPrice, int total, long orderDate) {
		this.email = email;
		this.noOrders = noOrders;
		this.unitPrice = unitPrice;
		this.total = total;
		this.orderDate = orderDate;
	}
	
	public static OldOrder newInstance() {
		return null;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public long getOrderId() {
		return orderId;
	}

	public void setOrderId(long orderId) {
		this.orderId = orderId;
	}

	public int getNoOrders() {
		return noOrders;
	}

	public void setNoOrders(int noOrders) {
		this.noOrders = noOrders;
	}

	public float getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(float unitPrice) {
		this.unitPrice = unitPrice;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public long getOrderDate() {
		return orderDate;
	}

	public void setOrderDate(long orderDate) {
		this.orderDate = orderDate;
	}
	
	public int getVersion() {
		return ver;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		return sb.append("email=" + email + ",").append("orderDate=" + new Date(orderDate) + ",")
				 .append("orderId=" + orderId + ",").append("noOrders=" + noOrders + ",")
				 .append("unitPrice=" + unitPrice + ",").append("total=" + total + ",")
				 .append("version=" + ver)
				 .toString();
	}

}
