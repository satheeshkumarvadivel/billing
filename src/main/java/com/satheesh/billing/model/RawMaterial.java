package com.satheesh.billing.model;

public class RawMaterial {
	int id;
	String material_name;
	String batch_no;
	int quantity;
	int output_quantity;
	boolean is_active;
	String created_time;
	String last_updated_time;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getMaterial_name() {
		return material_name;
	}

	public void setMaterial_name(String material_name) {
		this.material_name = material_name;
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

	public int getOutput_quantity() {
		return output_quantity;
	}

	public void setOutput_quantity(int output_quantity) {
		this.output_quantity = output_quantity;
	}

	public boolean isIs_active() {
		return is_active;
	}

	public void setIs_active(boolean is_active) {
		this.is_active = is_active;
	}

	public String getCreated_time() {
		return created_time;
	}

	public void setCreated_time(String created_time) {
		this.created_time = created_time;
	}

	public String getLast_updated_time() {
		return last_updated_time;
	}

	public void setLast_updated_time(String last_updated_time) {
		this.last_updated_time = last_updated_time;
	}

	@Override
	public String toString() {
		return "RawMaterial [id=" + id + ", material_name=" + material_name + ", batch_no=" + batch_no + ", quantity="
				+ quantity + ", output_quantity=" + output_quantity + ", is_active=" + is_active + ", created_time="
				+ created_time + ", last_updated_time=" + last_updated_time + "]";
	}

}
