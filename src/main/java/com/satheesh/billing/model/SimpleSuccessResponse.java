package com.satheesh.billing.model;

public class SimpleSuccessResponse extends SimpleResponse {

	public SimpleSuccessResponse() {
		super(200, "Success");
	}

	public SimpleSuccessResponse(String message) {
		super(200, message);
	}

}
