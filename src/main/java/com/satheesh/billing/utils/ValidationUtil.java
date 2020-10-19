package com.satheesh.billing.utils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.satheesh.billing.exceptions.ValidationException;
import com.satheesh.billing.model.Customer;
import com.satheesh.billing.model.Invoice;
import com.satheesh.billing.model.InvoiceItem;
import com.satheesh.billing.model.Product;

public class ValidationUtil {

	private static final Pattern NO_SPECIAL_CHAR_PATTERN = Pattern.compile("[^A-Za-z0-9 ,.!]",
			Pattern.CASE_INSENSITIVE);
	private static final Pattern CONTACT_NUMBER_PATTERN = Pattern.compile("[^0-9+]", Pattern.CASE_INSENSITIVE);
	public static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$",
			Pattern.CASE_INSENSITIVE);

	public static boolean isValidProduct(Product product) throws ValidationException {
		if (product == null)
			throw new ValidationException("Product cannot be null");

		if (product.getProduct_name() == null || product.getProduct_name().trim().length() <= 0)
			throw new ValidationException("Product Name cannot be empty.");

		// Product Name special character validation
		Matcher matcher = NO_SPECIAL_CHAR_PATTERN.matcher(product.getProduct_name());
		if (matcher.matches())
			throw new ValidationException(
					"Product Name can contain only alphabets, numbers, hyphen, comma, full stop and exclamation.");

		if (product.getPrice() < 0)
			throw new ValidationException("Product Price should be greater than or equal to zero.");

		if (product.getIn_stock_qty() < 0)
			throw new ValidationException("Product Quantity should be greater than or equal to zero.");

		// Product Name special character validation
		Matcher matcher2 = NO_SPECIAL_CHAR_PATTERN.matcher(product.getDescription());
		if (product.getDescription() != null && product.getDescription().trim().length() > 0) {
			if (matcher2.matches()) {
				throw new ValidationException(
						"Description can contain only alphabets, numbers, hyphen, comma, full stop and exclamation.");
			}
		}

		return true;
	}

	public static boolean isValidCustomer(Customer customer) throws ValidationException {
		if (customer == null)
			throw new ValidationException("Customer cannot be null");

		if (customer.getCompany_name() == null || customer.getCompany_name().trim().length() <= 0)
			throw new ValidationException("Company Name cannot be empty.");

		// Company Name special character validation
		Matcher companyNameMatcher = NO_SPECIAL_CHAR_PATTERN.matcher(customer.getCompany_name());
		if (companyNameMatcher.matches())
			throw new ValidationException(
					"Company Name can contain only alphabets, numbers, hyphen, comma, full stop and exclamation.");

		// Customer Name special character validation
		Matcher customerNameMatcher = NO_SPECIAL_CHAR_PATTERN.matcher(customer.getCustomer_name());
		if (customerNameMatcher.matches())
			throw new ValidationException(
					"Customer Name can contain only alphabets, numbers, hyphen, comma, full stop and exclamation.");

		// Contact special character validation
		Matcher customerContactOneMatcher = CONTACT_NUMBER_PATTERN.matcher(customer.getContact_number_1());
		if ((customer.getContact_number_1() != null && customer.getContact_number_1().trim().length() > 0)) {
			if (customerContactOneMatcher.matches())
				throw new ValidationException("Contact number can contain only numbers and plus sign.");
		}

		if (customer.getContact_number_2() != null && customer.getContact_number_2().trim().length() > 0) {
			Matcher customerContactTwoMatcher = CONTACT_NUMBER_PATTERN.matcher(customer.getContact_number_2());
			if (customerContactTwoMatcher.matches()) {
				throw new ValidationException("Contact number can contain only numbers and plus sign.");
			}
		}

		// Address special character validation
		if (customer.getAddress() != null && customer.getAddress().trim().length() > 0) {
			Matcher addressMatcher = NO_SPECIAL_CHAR_PATTERN.matcher(customer.getAddress());
			if (addressMatcher.matches()) {
				throw new ValidationException(
						"Address can contain only alphabets, numbers, hyphen, comma, full stop and exclamation.");
			}
		}

		// Email validation
		if (customer.getEmail() != null && customer.getEmail().trim().length() > 0) {
			Matcher emailMatcher = EMAIL_PATTERN.matcher(customer.getEmail());
			if (!emailMatcher.matches())
				throw new ValidationException("Email address is invalid.");
		}

		return true;
	}

	public static boolean isValidInvoice(Invoice invoice, List<Integer> productIds) throws ValidationException {

		if (invoice == null)
			throw new ValidationException("Invoice cannot be null");
		if (invoice.getItems() == null || invoice.getItems().isEmpty())
			throw new ValidationException("Invoice does not have any product item.");
		if (invoice.getCustomer() == null || invoice.getCustomer().getCustomer_id() == 0)
			throw new ValidationException("Customer is needed to create an invoice.");
		for (InvoiceItem item : invoice.getItems()) {
			if (item.getProduct() == null || item.getProduct().getProduct_id() == 0)
				throw new ValidationException("Product id is needed to create an invoice.");
			productIds.add(item.getProduct().getProduct_id());
			
			if (item.getPrice() < 0)
				throw new ValidationException("Product price cannot be less than zero.");
			if (item.getQuantity() < 0)
				throw new ValidationException("Product quantity cannot be less than one.");
		}

		return true;

	}

}
