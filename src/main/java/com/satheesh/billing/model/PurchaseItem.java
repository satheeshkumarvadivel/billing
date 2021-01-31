package com.satheesh.billing.model;

public class PurchaseItem {
	int id;
	int purchase_id;
	int product_id;
	PurchaseItemType item_type;
	String item_name;
	String description;
	String batch_no;
	int quantity;
	float price;
	float total;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getPurchase_id() {
		return purchase_id;
	}

	public void setPurchase_id(int purchase_id) {
		this.purchase_id = purchase_id;
	}

	public int getProduct_id() {
		return product_id;
	}

	public void setProduct_id(int product_id) {
		this.product_id = product_id;
	}

	public PurchaseItemType getItem_type() {
		return item_type;
	}

	public void setItem_type(PurchaseItemType item_type) {
		this.item_type = item_type;
	}

	public String getItem_name() {
		return item_name;
	}

	public void setItem_name(String item_name) {
		this.item_name = item_name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getBatch_no() {
		return batch_no;
	}

	public void setBatch_no(String batch_no) {
		this.batch_no = batch_no;
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
		return "PurchaseItem [id=" + id + ", purchase_id=" + purchase_id + ", product_id=" + product_id + ", item_type="
				+ item_type + ", item_name=" + item_name + ", description=" + description + ", batch_no=" + batch_no
				+ ", quantity=" + quantity + ", price=" + price + ", total=" + total + "]";
	}

}
