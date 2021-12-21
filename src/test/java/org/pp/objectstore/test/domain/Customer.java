package org.pp.objectstore.test.domain;

public interface Customer {
	// customer gender male is defined as char M
	public static final char MALE = 'M';
	// customer gender female is defined as char F
	public static final char FEMALE = 'F';
	// get customer id
	public String getId();
	// set customer id
	public void setId(String id);
	// get the age of the customer
	public int getAge();
	// set the age of the customer
	public void setAge(int age);
	// get gender of the customer
	public char getGender();
	// set gender of the customer
	public void setGender(char gender);
	// true if this customer active otherwise false
	public boolean isActive();
	// activate or disable a customer. true to active
	public void setActive(boolean active);
	// get net worth of the customer
	public float getWealth();
	// set customer wealth
	public void setWealth(float val);
	// get customer contact number
	public long getContact();
	// set customer contact number
	public void setContact(long contact);
	// get customer name
	public String getName();
	// set customer name
	public void setName(String name);
	// get customer email
	public String getEmail();
	// set customer email
	public void setEmail(String email);
	// get customer address
	public String getAddress();
	// set customer address
	public void setAddress(String address);
	// get version number
	public int getVersion();
	// get the customer creation or modification time in millisecond 
	public long getModificationDate();
	// set the customer creation or modification time in millisecond 
	public void setModificationDate(long time);	
	
}
