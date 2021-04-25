package com.satheesh.billing.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.satheesh.billing.exceptions.ValidationException;
import com.satheesh.billing.model.Customer;
import com.satheesh.billing.model.Invoice;
import com.satheesh.billing.model.InvoiceItem;
import com.satheesh.billing.model.Product;

@Repository
public class InvoiceDao {

	@Autowired
	JdbcTemplate jdbc;

	private Logger logger = LogManager.getLogger(this.getClass());

	@Transactional
	public void createInvoice(Invoice invoice) {
		int customer_id = invoice.getCustomer().getCustomer_id();

		int invoice_id = addInvoice(invoice, customer_id);

		reduceStock(invoice);

		addInvoiceItems(invoice, customer_id, invoice_id);

		if (invoice.getPrice() != invoice.getPayment_received()) {
			updateOutstandingAmount(invoice, customer_id, invoice_id);
		}
	}

	public Map<Integer, Integer> getProductQuantities(List<Integer> product_ids) {
		String sql = "SELECT id, in_stock_qty from product where id in (";
		for (int id : product_ids) {
			sql += id + ",";
		}
		sql = sql.substring(0, sql.length() - 1);
		sql += ") and is_active = true";

		logger.info("Query : " + sql);
		List<Map<String, Object>> result = jdbc.queryForList(sql);
		Map<Integer, Integer> quantityMap = new HashMap<>();
		if (result != null) {
			for (Map<String, Object> map : result) {
				int productId = (Integer) map.get("id");
				int quantity = (Integer) map.get("in_stock_qty");
				quantityMap.put(productId, quantity);
			}
		}
		return quantityMap;

	}

	public List<Invoice> getInvoices(String search, String dateRange, int page, int size) throws ValidationException {
		List<Invoice> invoices = new ArrayList<>();
		int limit = (size <= 0) ? 10 : size;
		int offset = (page <= 1) ? 0 : (page - 1) * limit;
		String from = null;
		String to = null;
		if (dateRange != null && dateRange.length() > 0) {
			String[] dates = dateRange.split(",");
			if (dates.length != 2) {
				throw new ValidationException("Invalid Date Range Provided. Format should be YYYY-MM-DD,YYYY-MM-DD");
			}
			from = dates[0];
			to = dates[1];
			if (from.split("-").length != 3 || to.split("-").length != 3) {
				throw new ValidationException("Invalid Date Range Provided. Format should be YYYY-MM-DD,YYYY-MM-DD");
			}
		}

		List<Map<String, Object>> results;
		String sql = "select inv.id, inv.invoice_date as invoice_date, inv.price, inv.payment_received, cust.id as customer_id, cust.company_name, cust.customer_name, cust.contact_number_1 "
				+ "from invoice inv join customer cust on inv.customer_id = cust.id";
		if (from != null && from.trim().length() > 0) {
			sql += " WHERE invoice_date BETWEEN cast( ? as timestamp) AND cast( ? as timestamp) ";
		}

		if (search == null || search.trim().length() == 0) {
			sql += " order by invoice_date desc limit ? offset ?";
			if (from != null && from.trim().length() > 0) {
				results = jdbc.queryForList(sql, from.trim(), to.trim(), limit, offset);
			} else {
				results = jdbc.queryForList(sql, limit, offset);
			}
		} else {
			sql += (from != null) ? " AND (" : " WHERE ";
			int id = 0;
			try {
				id = Integer.parseInt(search);
			} catch (Exception e) {
				logger.debug("Unable to cast search into Integer : ", e.getMessage());
				id = 0;
			}
			search = "%" + search.trim().toLowerCase() + "%";
			sql += " inv.id = ? OR lower(cust.customer_name) like ? OR lower(cust.contact_number_1) like ?";
			sql += (from != null) ? " ) " : "";
			sql += " order by invoice_date desc limit ? offset ?";
			if (from != null && from.trim().length() > 0) {
				results = jdbc.queryForList(sql, from.trim(), to.trim(), id, search, search, limit, offset);
			} else {
				results = jdbc.queryForList(sql, id, search, search, limit, offset);
			}
		}

		logger.info("Query : " + sql);

		if (results != null) {
			for (Map<String, Object> map : results) {
				Invoice invoice = new Invoice();
				invoice.setInvoice_id((Integer) map.get("id"));
				invoice.setInvoice_date(String.valueOf(map.get("invoice_date")));
				BigDecimal invoiceTotal = (BigDecimal) map.get("price");
				invoice.setPrice(invoiceTotal.floatValue());

				BigDecimal payment_received = (BigDecimal) map.get("payment_received");
				invoice.setPayment_received(payment_received.floatValue());

				Customer customer = new Customer();
				customer.setCustomer_id((Integer) map.get("customer_id"));
				customer.setCompany_name(String.valueOf(map.get("company_name")));
				customer.setCustomer_name(String.valueOf(map.get("customer_name")));
				customer.setContact_number_1(String.valueOf(map.get("contact_number_1")));
				invoice.setCustomer(customer);
				invoices.add(invoice);
			}
		} else {
			logger.info("No results returned from the Query");
		}
		return invoices;

	}

	public List<Invoice> getInvoiceById(int invoice_id) {
		List<Invoice> invoices = new ArrayList<>();

		String sql = "select inv.id, cust.customer_name, cust.contact_number_1, inv.price, inv.payment_received, inv.invoice_date, item.customer_id, item.product_id, "
				+ "prod.product_name, item.price as item_price, item.quantity, item.total from invoice_item item join customer cust on item.customer_id = cust.id join invoice inv "
				+ "on item.invoice_id = inv.id join product prod on prod.id = item.product_id where inv.id = ?";

		logger.info("Query : " + sql);
		List<Map<String, Object>> results = jdbc.queryForList(sql, invoice_id);
		if (results != null) {
			Invoice invoice = new Invoice();
			for (Map<String, Object> map : results) {
				invoice.setInvoice_id((Integer) map.get("id"));
				invoice.setInvoice_date(String.valueOf(map.get("invoice_date")));
				BigDecimal invoiceTotal = (BigDecimal) map.get("price");
				invoice.setPrice(invoiceTotal.floatValue());

				BigDecimal payment_received = (BigDecimal) map.get("payment_received");
				invoice.setPayment_received(payment_received.floatValue());

				Customer customer = new Customer();
				customer.setCustomer_id((Integer) map.get("customer_id"));
				customer.setCompany_name(String.valueOf(map.get("company_name")));
				customer.setCustomer_name(String.valueOf(map.get("customer_name")));
				customer.setContact_number_1(String.valueOf(map.get("contact_number_1")));
				invoice.setCustomer(customer);

				InvoiceItem item = new InvoiceItem();
				BigDecimal itemPrice = (BigDecimal) map.get("item_price");
				item.setPrice(itemPrice.floatValue());
				item.setQuantity((Integer) map.get("quantity"));
				BigDecimal itemTotal = (BigDecimal) map.get("total");
				item.setTotal(itemTotal.floatValue());

				Product product = new Product();
				product.setProduct_id((Integer) map.get("product_id"));
				product.setProduct_name(String.valueOf(map.get("product_name")));
				item.setProduct(product);
				invoice.getItems().add(item);
			}
			invoices.add(invoice);
		}
		return invoices;

	}

	private int addInvoice(Invoice invoice, int customer_id) {
		String sql = "INSERT INTO invoice (customer_id, price, payment_received) VALUES (?, ?, ?)";
		logger.info("Query : " + sql);
		KeyHolder keyHolder = new GeneratedKeyHolder();

		jdbc.update(new PreparedStatementCreator() {
			@Override
			public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
				PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
				ps.setInt(1, customer_id);
				ps.setBigDecimal(2, new BigDecimal(invoice.getPrice()));
				ps.setBigDecimal(3, new BigDecimal(invoice.getPayment_received()));
				return ps;
			}
		}, keyHolder);

		return (Integer) keyHolder.getKeys().get("id");
	}

	private void updateOutstandingAmount(Invoice invoice, int customer_id, int invoice_id) {
		String get_customer_outstanding_sql = "SELECT outstanding_amount FROM customer WHERE id = ?";
		BigDecimal outstanding = jdbc.queryForObject(get_customer_outstanding_sql, BigDecimal.class, customer_id);
		float outstanding_amount = outstanding.floatValue();
		String payment_type = "CREDIT";
		float amount = 0;
		if (invoice.getPayment_received() > invoice.getPrice()) {
			amount = (invoice.getPayment_received() - invoice.getPrice());
			outstanding_amount -= amount;
		} else {
			payment_type = "DEBIT";
			amount = (invoice.getPrice() - invoice.getPayment_received());
			outstanding_amount += amount;
		}
		String statement_sql = "INSERT INTO customer_credit_statement (customer_id, invoice_id, payment_type, amount, note) VALUES (?, ?, ?, ?, ?)";
		jdbc.update(statement_sql, customer_id, invoice_id, payment_type, amount, "INVOICE");
		jdbc.update("UPDATE customer SET outstanding_amount = ? WHERE id = ?", outstanding_amount, customer_id);
	}

	private void addInvoiceItems(Invoice invoice, int customer_id, int invoice_id) {
		String items_sql = "INSERT INTO invoice_item (invoice_id, customer_id, product_id, quantity, price, total) VALUES (?, ?, ?, ?, ?, ?)";

		jdbc.batchUpdate(items_sql, new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				ps.setInt(1, invoice_id);
				ps.setInt(2, customer_id);
				ps.setInt(3, invoice.getItems().get(i).getProduct().getProduct_id());
				ps.setInt(4, invoice.getItems().get(i).getQuantity());
				ps.setBigDecimal(5, new BigDecimal(invoice.getItems().get(i).getPrice()));
				ps.setBigDecimal(6, new BigDecimal(invoice.getItems().get(i).getTotal()));
			}

			@Override
			public int getBatchSize() {
				return invoice.getItems().size();
			}
		});
	}

	private void reduceStock(Invoice invoice) {
		String stock_reduce_sql = "UPDATE product SET in_stock_qty = (in_stock_qty - ?) WHERE id = ?";

		jdbc.batchUpdate(stock_reduce_sql, new BatchPreparedStatementSetter() {

			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				ps.setInt(1, invoice.getItems().get(i).getQuantity());
				ps.setInt(2, invoice.getItems().get(i).getProduct().getProduct_id());
			}

			@Override
			public int getBatchSize() {
				return invoice.getItems().size();
			}

		});
	}
}
