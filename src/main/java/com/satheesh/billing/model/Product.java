package com.satheesh.billing.model;

public class Product {
	int product_id;
	String product_name;
	String description;
	float price;
	int in_stock_qty;

	public Product(int product_id, String product_name) {
		super();
		this.product_id = product_id;
		this.product_name = product_name;
	}

	public int getProduct_id() {
		return product_id;
	}

	public void setProduct_id(int product_id) {
		this.product_id = product_id;
	}

	public String getProduct_name() {
		return product_name;
	}

	public void setProduct_name(String product_name) {
		this.product_name = product_name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public float getPrice() {
		return price;
	}

	public void setPrice(float price) {
		this.price = price;
	}

	public int getIn_stock_qty() {
		return in_stock_qty;
	}

	public void setIn_stock_qty(int in_stock_qty) {
		this.in_stock_qty = in_stock_qty;
	}

	@Override
	public String toString() {
		return "Product [product_id=" + product_id + ", product_name=" + product_name + ", description=" + description
				+ ", price=" + price + ", in_stock_qty=" + in_stock_qty + "]";
	}

}
