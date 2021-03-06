package com.satheesh.billing.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

	@GetMapping("/")
	public String getBilling() {
		return "index.html";
	}
	
	@GetMapping("/products")
	public String getProduct() {
		return "products.html";
	}

	@GetMapping("/customers")
	public String getCustomer() {
		return "customer.html";
	}

	@GetMapping("/invoices")
	public String getInvoice() {
		return "invoice.html";
	}
}
