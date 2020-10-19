package com.satheesh.billing.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

import com.satheesh.billing.model.Customer;
import com.satheesh.billing.model.Invoice;

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

	public List<Invoice> getInvoices(String search) {
		List<Invoice> invoices = new ArrayList<>();

		String sql = "select inv.id, inv.invoice_date as invoice_date, inv.price, inv.payment_received, cust.id as customer_id, cust.company_name, cust.customer_name, cust.contact_number_1 "
				+ "from invoice inv join customer cust on inv.customer_id = cust.id";

		if (search != null && search.trim().length() > 0) {
			sql += " WHERE lower(cust.company_name) like '%" + search.trim().toLowerCase()
					+ "%' OR lower(cust.customer_name) like '%" + search.trim().toLowerCase() + "%'";
		}

		sql += " order by invoice_date desc limit 1000";

		logger.info("Query : " + sql);
		List<Map<String, Object>> results = jdbc.queryForList(sql);
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
