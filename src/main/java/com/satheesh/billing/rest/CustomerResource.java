package com.satheesh.billing.rest;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.satheesh.billing.dao.CustomersDao;
import com.satheesh.billing.exceptions.ValidationException;
import com.satheesh.billing.model.CreditStatement;
import com.satheesh.billing.model.Customer;
import com.satheesh.billing.model.SimpleResponse;
import com.satheesh.billing.model.SimpleSuccessResponse;
import com.satheesh.billing.utils.ValidationUtil;

@RestController
public class CustomerResource {
	private Logger logger = LogManager.getLogger(this.getClass());

	@Autowired
	CustomersDao customersDao;

	// @Autowired
	// CustomerRepository custRepo;

	@GetMapping("customer")
	public ResponseEntity<?> getCustomers(@RequestParam(value = "search", required = false) String search,
			@RequestParam(value = "type", required = false) String type,
			@RequestParam(value = "page", required = false, defaultValue = "1") int page,
			@RequestParam(value = "size", required = false, defaultValue = "100") int size) {
		try {
			List<Customer> customers = new ArrayList<>();
			customers = customersDao.getCustomers(search, type, page, size);
			// if (search != null) {
			// customers = custRepo.searchCustomer(search.trim().toLowerCase());
			// } else {
			// }
			ResponseEntity<List<Customer>> response = new ResponseEntity<>(customers, HttpStatus.OK);
			return response;
		} catch (Exception e) {
			logger.error("Exception occurred while getting product : ", e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("customer")
	public ResponseEntity<?> createProduct(@RequestBody Customer customer) {
		try {
			ValidationUtil.isValidCustomer(customer);
			customersDao.createCustomer(customer);
			return new ResponseEntity<>(new SimpleSuccessResponse("Customer created successfully."),
					HttpStatus.CREATED);
		} catch (ValidationException valEx) {
			logger.debug(valEx.getMessage());
			return new ResponseEntity<>(new SimpleResponse(400, valEx.getMessage()), HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			logger.error("Exception occurred while creating customer : ", e);
			return new ResponseEntity<>(new SimpleResponse(500, "ERROR: Unable to create Customer."),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PutMapping("customer/{customerId}")
	public ResponseEntity<?> updateProduct(@PathVariable(value = "customerId") Integer customerId,
			@RequestBody Customer customer) {
		try {
			ValidationUtil.isValidCustomer(customer);
			if (!customersDao.updateCustomer(customerId, customer)) {
				return new ResponseEntity<>(new SimpleResponse(404, "Customer not found."), HttpStatus.NOT_FOUND);
			}
			return new ResponseEntity<>(new SimpleSuccessResponse("Customer updated successfully."), HttpStatus.OK);
		} catch (ValidationException valEx) {
			logger.debug(valEx.getMessage());
			return new ResponseEntity<>(new SimpleResponse(400, valEx.getMessage()), HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			logger.error("Exception occurred while updating customer : ", e);
			return new ResponseEntity<>(new SimpleResponse(500, "ERROR: Unable to update Customer."),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@DeleteMapping("customer/{customerId}")
	public ResponseEntity<?> deleteProduct(@PathVariable("customerId") Integer customerId) {
		try {
			if (!customersDao.deleteCustomer(customerId)) {
				return new ResponseEntity<>(new SimpleResponse(404, "Customer not found."), HttpStatus.NOT_FOUND);
			}
			return new ResponseEntity<>(new SimpleSuccessResponse("Customer deleted successfully."), HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Exception occurred while updating customer : ", e);
			return new ResponseEntity<>(new SimpleResponse(500, "ERROR: Unable to delete Customer."),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("customer/statement")
	public ResponseEntity<?> getStatement() {
		try {
			List<CreditStatement> statements = new ArrayList<>();
			statements = customersDao.getAllStatements();
			ResponseEntity<List<CreditStatement>> response = new ResponseEntity<>(statements, HttpStatus.OK);
			return response;
		} catch (Exception e) {
			logger.error("Exception occurred while getting customer statement : ", e);
			return new ResponseEntity<>(new SimpleResponse(500, "ERROR: Unable to get customer credit statement."),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
