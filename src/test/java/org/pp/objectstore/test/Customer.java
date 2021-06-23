package org.pp.objectstore.test;

import org.pp.objectstore.interfaces.Collection;
import org.pp.objectstore.interfaces.Id;

@Collection("customer")
public class Customer {
	//
	public static final boolean MALE = true;
	//
	public static final boolean FEMALE = false;
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
	private boolean gender;
	//
	private String addr;
	//
	private String email;
	//
	private float wealth;

	public Customer() {
	}

	public Customer(String id, String name, long contact, int age, boolean gender, String addr, String email,
			float wealth) {
		this.id = id;
		this.name = name;
		this.contact = contact;
		this.age = age;
		this.gender = gender;
		this.addr = addr;
		this.email = email;
		this.wealth = wealth;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public boolean isGender() {
		return gender;
	}

	public void setGender(boolean gender) {
		this.gender = gender;
	}

	public String getAddr() {
		return addr;
	}

	public void setAddr(String addr) {
		this.addr = addr;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public long getContact() {
		return contact;
	}

	public void setContact(long contact) {
		this.contact = contact;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public float getWealth() {
		return wealth;
	}

	public void setWealth(float wealth) {
		this.wealth = wealth;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		return sb.append("id=" + id + ",").append("name=" + name + ",").append("contact=" + contact + ",")
				.append("age=" + age + ",").append("addr=" + addr + ",").append("email=" + email + ",")
				.append("gender=" + (gender ? "MALE" : "FEMALE") + ",").append("wealth=" + wealth).toString();
	}

}
