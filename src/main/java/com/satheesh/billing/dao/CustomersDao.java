package com.satheesh.billing.dao;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.satheesh.billing.exceptions.DbException;
import com.satheesh.billing.exceptions.ValidationException;
import com.satheesh.billing.model.Customer;

@Repository
public class CustomersDao {
	@Autowired
	JdbcTemplate jdbc;

	private Logger logger = LogManager.getLogger(this.getClass());

	public List<Customer> getCustomers(String search, int page, int size) throws DbException {
		List<Customer> customers = new ArrayList<>();
		int limit = (size <= 0) ? 10 : size;
		int offset = (page <= 1) ? 0 : (page - 1) * limit;
		List<Map<String, Object>> results;

		String sql = "SELECT * FROM customer where is_active = true ";
		if (search == null || search.trim().length() == 0) {
			sql += " order by company_name limit ? offset ?";
			results = jdbc.queryForList(sql, limit, offset);
		} else {
			search = "%" + search.trim().toLowerCase() + "%";
			sql += " and lower(company_name) like ? "
					+ " OR lower(customer_name) like ? OR  lower(contact_number_1) like ? "
					+ " OR lower(contact_number_1) like ? OR  lower(address) like ? "
					+ " OR lower(email) like ? order by company_name limit ? offset ?";
			results = jdbc.queryForList(sql, search, search, search, search, search, search, limit, offset);
		}

		logger.info("Query : " + sql);
		if (results != null) {
			for (Map<String, Object> map : results) {
				int customer_id = (Integer) map.get("id");
				String company_name = String.valueOf(map.get("company_name"));
				Customer customer = new Customer(customer_id, company_name);
				customer.setCustomer_name(String.valueOf(map.get("customer_name")));
				customer.setContact_number_1(String.valueOf(map.get("contact_number_1")));
				customer.setContact_number_2(String.valueOf(map.get("contact_number_2")));
				customer.setAddress(String.valueOf(map.get("address")));
				customer.setEmail(String.valueOf(map.get("email")));
				BigDecimal outstanding_amount = (BigDecimal) map.get("outstanding_amount");
				customer.setOutstanding_amount(outstanding_amount.floatValue());
				customers.add(customer);
			}
		}
		return customers;
	}

	@Transactional
	public boolean createCustomer(Customer customer) throws ValidationException {
		String sql = "INSERT INTO customer (company_name, customer_name, contact_number_1, contact_number_2, address, email, outstanding_amount) VALUES (?, ?, ?, ?, ?, ?, ?)";
		try {
			jdbc.update(sql, customer.getCompany_name(), customer.getCustomer_name(), customer.getContact_number_1(),
					customer.getContact_number_2(), customer.getAddress(), customer.getEmail(),
					customer.getOutstanding_amount());

			if (customer.getOutstanding_amount() > 0) {
				int customer_id = jdbc.queryForObject("SELECT id FROM customer WHERE company_name = ?", Integer.class,
						customer.getCompany_name());
				String debit_sql = "INSERT INTO customer_credit_statement (customer_id, payment_type, amount, note) VALUES (?, ?, ?, ?)";
				jdbc.update(debit_sql, customer_id, "DEBIT", customer.getOutstanding_amount(), "OPENING_DEBT");
			}
			return true;
		} catch (DuplicateKeyException ex) {
			throw new ValidationException(customer.getCompany_name()
					+ " already exists. Please provide a unique company name or Edit the existing company name.");
		}
	}

	public boolean updateCustomer(int customerId, Customer customer) throws ValidationException {
		String sql = "UPDATE customer SET company_name = ?, customer_name = ?, contact_number_1 = ?, contact_number_2 = ?, address = ?, email = ?, outstanding_amount = ? WHERE id = ?";
		try {
			return jdbc.update(sql, customer.getCompany_name(), customer.getCustomer_name(),
					customer.getContact_number_1(), customer.getContact_number_2(), customer.getAddress(),
					customer.getEmail(), customer.getOutstanding_amount(), customerId) > 0;
		} catch (DuplicateKeyException ex) {
			throw new ValidationException(customer.getCompany_name()
					+ " already exists. Please provide a unique company name or Edit the existing company name.");
		}
	}

	public boolean deleteCustomer(int customer_id) {
		String sql = "UPDATE customer SET is_active = false WHERE id = ?";
		return jdbc.update(sql, customer_id) > 0;
	}

}
