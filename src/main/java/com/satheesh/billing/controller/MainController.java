package com.satheesh.billing.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

	private Logger logger = LogManager.getLogger(this.getClass());

	@GetMapping("/")
	public String getHome() {
		logger.info("Get controller");
		return "index.html";
	}
}
