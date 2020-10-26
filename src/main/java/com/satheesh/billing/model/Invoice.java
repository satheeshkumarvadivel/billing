package com.satheesh.billing.model;

import java.util.ArrayList;
import java.util.List;

public class Invoice {

	int invoice_id;
	String invoice_date;
	Customer customer;
	List<InvoiceItem> items = new ArrayList<>();
	float price;
	float payment_received;

	public int getInvoice_id() {
		return invoice_id;
	}

	public void setInvoice_id(int invoice_id) {
		this.invoice_id = invoice_id;
	}

	public String getInvoice_date() {
		return invoice_date;
	}

	public void setInvoice_date(String invoice_date) {
		this.invoice_date = invoice_date;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public List<InvoiceItem> getItems() {
		return items;
	}

	public void setItems(List<InvoiceItem> items) {
		this.items = items;
	}

	public float getPrice() {
		return price;
	}

	public void setPrice(float price) {
		this.price = price;
	}

	public float getPayment_received() {
		return payment_received;
	}

	public void setPayment_received(float payment_received) {
		this.payment_received = payment_received;
	}

	@Override
	public String toString() {
		return "Invoice [invoice_id=" + invoice_id + ", invoice_date=" + invoice_date + ", customer=" + customer
				+ ", items=" + items + ", price=" + price + ", payment_received=" + payment_received + "]";
	}

}
