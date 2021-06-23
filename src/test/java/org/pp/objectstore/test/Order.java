package org.pp.objectstore.test;

import java.util.Date;

import org.pp.objectstore.interfaces.Collection;
import org.pp.objectstore.interfaces.Id;
import org.pp.objectstore.interfaces.SortKey;

@Collection("order")
public class Order {
	//
	@SortKey
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
	//
	private long orderDate;

	public Order() {
	}

	public Order(String email, int noOrders, float unitPrice, int total) {
		this.email = email;
		this.noOrders = noOrders;
		this.unitPrice = unitPrice;
		this.total = total;
		this.orderDate = System.currentTimeMillis();
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

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		return sb.append("email=" + email + ",").append("orderId=" + orderId + ",").append("noOrders=" + noOrders + ",")
				.append("unitPrice=" + unitPrice + ",").append("total=" + total + ",").append("orderDate=" + new Date(orderDate))
				.toString();
	}

}
