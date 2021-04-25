package com.satheesh.billing.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.satheesh.billing.dao.InvoiceDao;
import com.satheesh.billing.exceptions.ValidationException;
import com.satheesh.billing.model.Invoice;
import com.satheesh.billing.model.InvoiceItem;
import com.satheesh.billing.model.SimpleResponse;
import com.satheesh.billing.model.SimpleSuccessResponse;
import com.satheesh.billing.utils.ValidationUtil;

@RestController
public class InvoiceResource {

	private Logger logger = LogManager.getLogger(this.getClass());

	@Autowired
	InvoiceDao invoiceDao;

	@GetMapping("/invoice")
	public ResponseEntity<?> getInvoices(@RequestParam(value = "search", required = false) String search,
			@RequestParam(value = "date", required = false) String dateRange,
			@RequestParam(value = "page", required = false, defaultValue = "1") int page,
			@RequestParam(value = "size", required = false, defaultValue = "100") int size) {
		try {
			List<Invoice> invoices = invoiceDao.getInvoices(search, dateRange, page, size);
			return new ResponseEntity<List<Invoice>>(invoices, HttpStatus.OK);
		} catch (ValidationException valEx) {
			logger.debug(valEx.getMessage());
			return new ResponseEntity<>(new SimpleResponse(400, valEx.getMessage()), HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			logger.error("Exception occurred while creating invoice : ", e);
			return new ResponseEntity<>(new SimpleResponse(500, "ERROR: Unable to get invoice details."),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/invoice/{invoiceId}")
	public ResponseEntity<?> getInvoiceById(@PathVariable(value = "invoiceId") Integer invoiceId) {
		try {
			List<Invoice> invoices = invoiceDao.getInvoiceById(invoiceId);
			return new ResponseEntity<List<Invoice>>(invoices, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Exception occurred while creating invoice : ", e);
			return new ResponseEntity<>(new SimpleResponse(500, "ERROR: Unable to get invoice details."),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/invoice")
	public ResponseEntity<?> createInvoice(@RequestBody Invoice invoice) {
		try {
			List<Integer> productIds = new ArrayList<>();
			ValidationUtil.isValidInvoice(invoice, productIds);

			Map<Integer, Integer> quantityMap = invoiceDao.getProductQuantities(productIds);

			for (InvoiceItem item : invoice.getItems()) {
				int productId = item.getProduct().getProduct_id();
				if (quantityMap.get(item.getProduct().getProduct_id()) == null)
					throw new ValidationException("Invalid Product Id.");
				int availableQty = quantityMap.get(item.getProduct().getProduct_id());
				if (item.getQuantity() > availableQty) {
					throw new ValidationException(
							"Only " + availableQty + " quantity of Product id " + productId + " is available.");
				}
			}

			float total = 0;
			for (InvoiceItem item : invoice.getItems()) {
				item.setTotal(item.getPrice() * item.getQuantity());
				total += item.getTotal();
			}
			invoice.setPrice(total);

			invoiceDao.createInvoice(invoice);
			return new ResponseEntity<>(new SimpleSuccessResponse("Invoice created successfully"), HttpStatus.CREATED);

		} catch (ValidationException valEx) {
			logger.debug(valEx.getMessage());
			return new ResponseEntity<>(new SimpleResponse(400, valEx.getMessage()), HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			logger.error("Exception occurred while creating invoice : ", e);
			return new ResponseEntity<>(new SimpleResponse(500, "ERROR: Unable to create Invoice."),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
