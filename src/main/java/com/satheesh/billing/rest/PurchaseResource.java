package com.satheesh.billing.rest;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.satheesh.billing.dao.PurchaseDao;
import com.satheesh.billing.exceptions.ValidationException;
import com.satheesh.billing.model.Purchase;
import com.satheesh.billing.model.SimpleResponse;
import com.satheesh.billing.model.SimpleSuccessResponse;
import com.satheesh.billing.utils.ValidationUtil;

@RestController
public class PurchaseResource {

	private Logger logger = LogManager.getLogger(this.getClass());

	@Autowired
	PurchaseDao purchaseDao;

	@GetMapping("/purchase")
	public ResponseEntity<?> getPurchases(@RequestParam(value = "search", required = false) String search,
			@RequestParam(value = "page", required = false, defaultValue = "1") int page,
			@RequestParam(value = "size", required = false, defaultValue = "10") int size) {
		try {
			List<Purchase> purchaseList = purchaseDao.getPurchases(search, page, size);
			return new ResponseEntity<List<Purchase>>(purchaseList, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Exception occurred while getting purchase : ", e);
			return new ResponseEntity<>(new SimpleResponse(500, "ERROR: Unable to get purchase details."),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/purchase")
	public ResponseEntity<?> createPurchase(@RequestBody Purchase purchase) {
		try {
			
			ValidationUtil.isValidPurchase(purchase);

			purchaseDao.createPurchase(purchase);
			return new ResponseEntity<>(new SimpleSuccessResponse("Purchase created successfully"), HttpStatus.CREATED);

		} catch (ValidationException valEx) {
			logger.debug(valEx.getMessage());
			return new ResponseEntity<>(new SimpleResponse(400, valEx.getMessage()), HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			logger.error("Exception occurred while creating Purchase : ", e);
			return new ResponseEntity<>(new SimpleResponse(500, "ERROR: Unable to create Purchase."),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
