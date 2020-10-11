package com.satheesh.billing.model;

public class SimpleResponse {

	int statusCode;
	String message = "";

	public SimpleResponse(int statusCode, String message) {
		super();
		this.statusCode = statusCode;
		this.message = message;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return "SimpleResponse [statusCode=" + statusCode + ", message=" + message + "]";
	}

}
