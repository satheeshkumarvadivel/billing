package com.satheesh.billing.model;

public class SimpleResponse {

	int code;
	String message = "";

	public SimpleResponse(int status, String message) {
		this.code = status;
		this.message = message;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return "SimpleResponse [code=" + code + ", message=" + message + "]";
	}

}
