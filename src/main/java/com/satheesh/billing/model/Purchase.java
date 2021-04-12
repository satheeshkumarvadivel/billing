package com.satheesh.billing.model;

import java.util.List;

public class Purchase {
	int id;
	int customer_id;
	String customer_name;
	float price;
	float amount_paid;
	String purchase_date;
	List<PurchaseItem> items;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getCustomer_id() {
		return customer_id;
	}

	public void setCustomer_id(int customer_id) {
		this.customer_id = customer_id;
	}

	public float getAmount_paid() {
		return amount_paid;
	}

	public void setAmount_paid(float amount_paid) {
		this.amount_paid = amount_paid;
	}

	public float getPrice() {
		return price;
	}

	public void setPrice(float price) {
		this.price = price;
	}

	public String getPurchase_date() {
		return purchase_date;
	}

	public void setPurchase_date(String purchase_date) {
		this.purchase_date = purchase_date;
	}

	public List<PurchaseItem> getItems() {
		return items;
	}

	public void setItems(List<PurchaseItem> items) {
		this.items = items;
	}

	public String getCustomer_name() {
		return customer_name;
	}

	public void setCustomer_name(String customer_name) {
		this.customer_name = customer_name;
	}

	@Override
	public String toString() {
		return "Purchase [amount_paid=" + amount_paid + ", customer_id=" + customer_id + ", customer_name="
				+ customer_name + ", id=" + id + ", items=" + items + ", price=" + price + ", purchase_date="
				+ purchase_date + "]";
	}

}
