package com.satheesh.billing.model;

public class Customer {
	int customer_id;
	String company_name;
	String customer_name;
	String contact_number_1;
	String contact_number_2;
	String address;
	String email;
	float outstanding_amount;

	public Customer() {
		super();
	}

	public Customer(int customer_id, String company_name) {
		super();
		this.customer_id = customer_id;
		this.company_name = company_name;
	}

	public int getCustomer_id() {
		return customer_id;
	}

	public void setCustomer_id(int customer_id) {
		this.customer_id = customer_id;
	}

	public String getCompany_name() {
		return company_name;
	}

	public void setCompany_name(String company_name) {
		this.company_name = company_name;
	}

	public String getCustomer_name() {
		return customer_name;
	}

	public void setCustomer_name(String customer_name) {
		this.customer_name = customer_name;
	}

	public String getContact_number_1() {
		return contact_number_1;
	}

	public void setContact_number_1(String contact_number_1) {
		this.contact_number_1 = contact_number_1;
	}

	public String getContact_number_2() {
		return contact_number_2;
	}

	public void setContact_number_2(String contact_number_2) {
		this.contact_number_2 = contact_number_2;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public float getOutstanding_amount() {
		return outstanding_amount;
	}

	public void setOutstanding_amount(float outstanding_amount) {
		this.outstanding_amount = outstanding_amount;
	}

	@Override
	public String toString() {
		return "Customer [customer_id=" + customer_id + ", company_name=" + company_name + ", customer_name="
				+ customer_name + ", contact_number_1=" + contact_number_1 + ", contact_number_2=" + contact_number_2
				+ ", address=" + address + ", email=" + email + ", outstanding_amount=" + outstanding_amount + "]";
	}

}
