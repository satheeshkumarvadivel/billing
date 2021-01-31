package com.satheesh.billing.rest;

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

import com.satheesh.billing.dao.ProductsDao;
import com.satheesh.billing.exceptions.ValidationException;
import com.satheesh.billing.model.Product;
import com.satheesh.billing.model.SimpleResponse;
import com.satheesh.billing.model.SimpleSuccessResponse;
import com.satheesh.billing.utils.ValidationUtil;

@RestController
public class ProductResource {

	private Logger logger = LogManager.getLogger(this.getClass());

	@Autowired
	ProductsDao productsDao;

	@GetMapping("product")
	public ResponseEntity<?> getProducts(@RequestParam(value = "product_name", required = false) String product_name,
			@RequestParam(value = "page", required = false, defaultValue = "1") int page,
			@RequestParam(value = "size", required = false, defaultValue = "10") int size) {
		try {
			List<Product> products = productsDao.getProducts(product_name, page, size);
			ResponseEntity<List<Product>> response = new ResponseEntity<>(products, HttpStatus.OK);
			return response;
		} catch (Exception e) {
			logger.error("Exception occurred while getting product : ", e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("product")
	public ResponseEntity<?> createProduct(@RequestBody Product product) {
		try {
			ValidationUtil.isValidProduct(product);
			productsDao.createProduct(product);
			return new ResponseEntity<>(new SimpleSuccessResponse("Product added successfully."), HttpStatus.CREATED);
		} catch (ValidationException valEx) {
			logger.debug(valEx.getMessage());
			return new ResponseEntity<>(new SimpleResponse(400, valEx.getMessage()), HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			logger.error("Exception occurred while creating product : ", e);
			return new ResponseEntity<>(new SimpleResponse(500, "ERROR: Unable to create Product."),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PutMapping("product/{productId}")
	public ResponseEntity<?> updateProduct(@PathVariable(value = "productId") Integer productId,
			@RequestBody Product product) {
		try {
			ValidationUtil.isValidProduct(product);
			productsDao.updateProduct(productId, product);
			return new ResponseEntity<>(new SimpleSuccessResponse("Product updated successfully."), HttpStatus.OK);
		} catch (ValidationException valEx) {
			logger.debug(valEx.getMessage());
			return new ResponseEntity<>(new SimpleResponse(400, valEx.getMessage()), HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			logger.error("Exception occurred while updating product : ", e);
			return new ResponseEntity<>(new SimpleResponse(500, "ERROR: Unable to update Product."),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@DeleteMapping("product/{productId}")
	public ResponseEntity<?> deleteProduct(@PathVariable("productId") Integer productId) {
		try {
			productsDao.deleteProduct(productId);
			return new ResponseEntity<>(new SimpleSuccessResponse("Product deleted successfully."), HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Exception occurred while updating product : ", e);
			return new ResponseEntity<>(new SimpleResponse(500, "ERROR: Unable to delete Product."),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
