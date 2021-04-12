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

import com.satheesh.billing.model.Purchase;
import com.satheesh.billing.model.PurchaseItem;
import com.satheesh.billing.model.PurchaseItemType;

@Repository
public class PurchaseDao {

	@Autowired
	JdbcTemplate jdbc;

	private Logger logger = LogManager.getLogger(this.getClass());

	@Transactional
	public void createPurchase(Purchase purchase) {

		int purchase_id = addPurchase(purchase);

		addPurchaseItems(purchase, purchase_id);

		purchase.getItems().stream().filter((item) -> item.getItem_type().equals(PurchaseItemType.PRODUCT));

		if (!purchase.getItems().isEmpty()) {
			updateStock(purchase);
		}
		if (purchase.getPrice() != purchase.getAmount_paid()) {
			updateOutstandingAmount(purchase, purchase_id);
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

	public List<Purchase> getPurchases(String search, int page, int size) {
		List<Purchase> purchaseList = new ArrayList<>();
		int limit = (size <= 0) ? 10 : size;
		int offset = (page <= 1) ? 0 : (page - 1) * limit;

		List<Map<String, Object>> results;

		String sql = "select pur.id, pur.purchase_date, pur.price, pur.amount_paid, cus.customer_name from purchase pur join customer cus on pur.customer_id = cus.id ";
		if (search == null || search.trim().length() == 0) {
			sql += " order by pur.purchase_date desc limit ? offset ? ";
			results = jdbc.queryForList(sql, limit, offset);
		} else {
			search = "%" + search.trim().toLowerCase() + "%";
			sql += " WHERE lower(cus.customer_name) like ? ";
			sql += " order by pur.purchase_date desc limit ? offset ?";
			results = jdbc.queryForList(sql, search, limit, offset);
		}

		logger.info("Query : " + sql);

		if (results != null) {
			for (Map<String, Object> map : results) {
				Purchase purchase = new Purchase();
				purchase.setId((Integer) map.get("id"));
				purchase.setPurchase_date(String.valueOf(map.get("purchase_date")));
				purchase.setCustomer_name(String.valueOf(map.get("customer_name")));

				BigDecimal purchase_amount = (BigDecimal) map.get("price");
				purchase.setPrice(purchase_amount.floatValue());

				BigDecimal amount_paid = (BigDecimal) map.get("amount_paid");
				purchase.setAmount_paid(amount_paid.floatValue());

				purchaseList.add(purchase);
			}
		}
		return purchaseList;

	}

	public Purchase getPurchaseById(int purchaseId) {

		String sql = "select pur.id, pur.price as purchase_total, pur.amount_paid, pur.purchase_date, cus.customer_name, pr.product_name, item.quantity, item.price, item.total "
				+ "from purchase_item item join product pr on item.product_id = pr.id "
				+ "join purchase pur on item.purchase_id = pur.id " + "join customer cus on pur.customer_id = cus.id "
				+ "where item.purchase_id = ?";
		List<Map<String, Object>> results;
		results = jdbc.queryForList(sql, purchaseId);
		Purchase purchase = new Purchase();
		List<PurchaseItem> items = new ArrayList<>();
		if (results != null) {
			for (Map<String, Object> map : results) {
				purchase.setId((Integer) map.get("id"));
				purchase.setPurchase_date(String.valueOf(map.get("purchase_date")));
				purchase.setCustomer_name(String.valueOf(map.get("customer_name")));

				BigDecimal purchase_amount = (BigDecimal) map.get("purchase_total");
				purchase.setPrice(purchase_amount.floatValue());

				BigDecimal amount_paid = (BigDecimal) map.get("amount_paid");
				purchase.setAmount_paid(amount_paid.floatValue());

				PurchaseItem item = new PurchaseItem();
				item.setItem_name(String.valueOf(map.get("product_name")));

				BigDecimal item_price = (BigDecimal) map.get("price");
				item.setPrice(item_price.floatValue());

				item.setQuantity((Integer) map.get("quantity"));

				BigDecimal total = (BigDecimal) map.get("total");
				item.setTotal(total.floatValue());

				items.add(item);
			}
			purchase.setItems(items);
		}

		return purchase;
	}

	private int addPurchase(Purchase purchase) {
		String sql = "INSERT INTO purchase (customer_id, amount_paid, price) VALUES (?, ?, ?)";
		logger.info("Query : " + sql);
		KeyHolder keyHolder = new GeneratedKeyHolder();

		jdbc.update(new PreparedStatementCreator() {
			@Override
			public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
				PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
				ps.setInt(1, purchase.getCustomer_id());
				ps.setBigDecimal(2, new BigDecimal(purchase.getAmount_paid()));
				ps.setBigDecimal(3, new BigDecimal(purchase.getPrice()));
				return ps;
			}
		}, keyHolder);

		return (Integer) keyHolder.getKeys().get("id");
	}

	private void addPurchaseItems(Purchase purchase, int purchase_id) {
		String items_sql = "INSERT INTO purchase_item (purchase_id, product_id, item_type, item_name, description, batch_no, quantity, price, total) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

		jdbc.batchUpdate(items_sql, new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				ps.setInt(1, purchase_id);
				if (purchase.getItems().get(i).getProduct_id() == 0) {
					ps.setNull(2, java.sql.Types.INTEGER);
				} else {
					ps.setInt(2, purchase.getItems().get(i).getProduct_id());
				}
				ps.setString(3, purchase.getItems().get(i).getItem_type().toString());
				ps.setString(4, purchase.getItems().get(i).getItem_name());
				ps.setString(5, purchase.getItems().get(i).getDescription());
				ps.setString(6, purchase.getItems().get(i).getBatch_no());
				ps.setInt(7, purchase.getItems().get(i).getQuantity());
				ps.setBigDecimal(8, new BigDecimal(purchase.getItems().get(i).getPrice()));
				ps.setBigDecimal(9, new BigDecimal(purchase.getItems().get(i).getTotal()));
			}

			@Override
			public int getBatchSize() {
				return purchase.getItems().size();
			}
		});
	}

	private void updateStock(Purchase purchase) {
		String update_stock_sql = "UPDATE product SET in_stock_qty = (in_stock_qty + ?) WHERE id = ?";

		jdbc.batchUpdate(update_stock_sql, new BatchPreparedStatementSetter() {

			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				ps.setInt(1, purchase.getItems().get(i).getQuantity());
				ps.setInt(2, purchase.getItems().get(i).getProduct_id());
			}

			@Override
			public int getBatchSize() {
				return purchase.getItems().size();
			}

		});
	}

	private void updateOutstandingAmount(Purchase purchase, int purchase_id) {
		String get_customer_outstanding_sql = "SELECT outstanding_amount FROM customer WHERE id = ?";
		BigDecimal outstanding = jdbc.queryForObject(get_customer_outstanding_sql, BigDecimal.class,
				purchase.getCustomer_id());
		float outstanding_amount = outstanding.floatValue();
		String payment_type = "CREDIT";
		float amount = 0;
		if (purchase.getAmount_paid() > purchase.getPrice()) {
			amount = (purchase.getAmount_paid() - purchase.getPrice());
			outstanding_amount += amount;
		} else {
			payment_type = "DEBIT";
			amount = (purchase.getPrice() - purchase.getAmount_paid());
			outstanding_amount -= amount;
		}
		String statement_sql = "INSERT INTO customer_credit_statement (customer_id, purchase_id, payment_type, amount, note) VALUES (?, ?, ?, ?, ?)";
		jdbc.update(statement_sql, purchase.getCustomer_id(), purchase_id, payment_type, amount, "PURCHASE");
		jdbc.update("UPDATE customer SET outstanding_amount = ? WHERE id = ?", outstanding_amount,
				purchase.getCustomer_id());
	}
}
