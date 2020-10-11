package com.satheesh.billing.rest;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.satheesh.billing.model.Product;

@RestController
public class BillingResource {

	private Logger logger = LogManager.getLogger(this.getClass());

	@GetMapping("product")
	public List<Product> sendEmail() {

		List<Product> products = new ArrayList<>();
		try {

			products.add(new Product(1, "Shakthi Chilli Powder 200g"));
			products.add(new Product(2, "Shakthi Sambar Powder 100g"));
			products.add(new Product(3, "Shakthi Rasam Powder 100g"));
			products.add(new Product(4, "Shakthi Turmeric Powder 50g"));

			logger.info("Successfully retrieved " + products.size() + " products.");
		} catch (Exception e) {
			logger.error("Exception occurred while sending email : ", e);
		}
		return products;
	}

}
