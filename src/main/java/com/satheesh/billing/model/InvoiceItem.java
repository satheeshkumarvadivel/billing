package com.satheesh.billing.model;

public class InvoiceItem {
	Product product;
	int quantity;
	float price;
	float total;

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public float getPrice() {
		return price;
	}

	public void setPrice(float price) {
		this.price = price;
	}

	public float getTotal() {
		return total;
	}

	public void setTotal(float total) {
		this.total = total;
	}

	@Override
	public String toString() {
		return "InvoiceItem [product=" + product + ", quantity=" + quantity + ", price=" + price + ", total=" + total
				+ "]";
	}

}
