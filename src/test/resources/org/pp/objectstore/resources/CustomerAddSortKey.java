package org.pp.objectstore.test.domain;

import org.pp.objectstore.interfaces.Collection;
import org.pp.objectstore.interfaces.Id;
import org.pp.objectstore.test.domain.Customer;
import org.pp.objectstore.interfaces.SortKey;

@Collection("customer")
public class CustomerImp implements Customer {
	//
	@Id(gen = false)
	private String id;
	//
	private String name;
	//
	private long contact;
	//
	private int age;
	//
	private char gender;
	//
	private String addr;
	@SortKey(1)
	private String email;
	//
	private float wealth;
	// 
	private boolean active;

	public CustomerImp() {
	}

	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return id;
	}



	@Override
	public void setId(String id) {
		// TODO Auto-generated method stub
		this.id = id;
	}



	@Override
	public int getAge() {
		// TODO Auto-generated method stub
		return age;
	}



	@Override
	public void setAge(int age) {
		// TODO Auto-generated method stub
		this.age = age;
	}



	@Override
	public char getGender() {
		// TODO Auto-generated method stub
		return gender;
	}



	@Override
	public void setGender(char gender) {
		// TODO Auto-generated method stub
		this.gender = gender;
	}



	@Override
	public boolean isActive() {
		// TODO Auto-generated method stub
		return active;
	}



	@Override
	public void setActive(boolean active) {
		// TODO Auto-generated method stub
		this.active = active;
	}



	@Override
	public float getWealth() {
		// TODO Auto-generated method stub
		return wealth;
	}



	@Override
	public void setWealth(float wealth) {
		// TODO Auto-generated method stub
		this.wealth = wealth;
	}



	@Override
	public long getContact() {
		// TODO Auto-generated method stub
		return contact;
	}



	@Override
	public void setContact(long contact) {
		// TODO Auto-generated method stub
		this.contact = contact;
	}



	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return name;
	}



	@Override
	public void setName(String name) {
		// TODO Auto-generated method stub
		this.name = name;
	}



	@Override
	public String getEmail() {
		// TODO Auto-generated method stub
		return email;
	}



	@Override
	public void setEmail(String email) {
		// TODO Auto-generated method stub
		this.email = email;
	}



	@Override
	public String getAddress() {
		// TODO Auto-generated method stub
		return addr;
	}



	@Override
	public void setAddress(String address) {
		// TODO Auto-generated method stub
		this.addr = address;
	}



	@Override
	public int getVersion() {
		// TODO Auto-generated method stub
		return -1;
	}



	@Override
	public long getModificationDate() {
		// TODO Auto-generated method stub
		return -1;
	}



	@Override
	public void setModificationDate(long time) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		return sb.append("id=" + id + ",")
				 .append("name=" + name + ",")
				 .append("contact=" + contact + ",")
				 .append("age=" + age + ",")
				 .append("addr=" + addr + ",")
				 .append("email=" + email + ",")
				 .append("gender=" + (gender == 'M' ? "MALE" : "FEMALE") + ",")
				 .append("wealth=" + wealth).toString();
	}
}
