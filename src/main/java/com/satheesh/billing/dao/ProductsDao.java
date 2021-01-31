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

import com.satheesh.billing.exceptions.DbException;
import com.satheesh.billing.exceptions.ValidationException;
import com.satheesh.billing.model.Product;

@Repository
public class ProductsDao {

	@Autowired
	JdbcTemplate jdbc;

	private Logger logger = LogManager.getLogger(this.getClass());

	public List<Product> getProducts(String productName, int page, int size) throws DbException {
		List<Product> products = new ArrayList<>();
		int limit = (size <= 0) ? 10 : size;
		int offset = (page <= 1) ? 0 : (page - 1) * limit;

		List<Map<String, Object>> results;
		String sql = "SELECT * FROM product where is_active = true ";

		if (productName == null || productName.trim().length() == 0) {
			sql += " order by product_name limit ? offset ?";
			results = jdbc.queryForList(sql, limit, offset);
		} else {
			productName = "%" + productName.trim().toLowerCase() + "%";
			sql += " and lower(product_name) like ? order by product_name limit ? offset ?";
			results = jdbc.queryForList(sql, productName, limit, offset);
		}
		
		logger.debug("Query : " + sql);

		if (results != null) {
			for (Map<String, Object> map : results) {
				int product_id = (Integer) map.get("id");
				String product_name = String.valueOf(map.get("product_name"));
				Product product = new Product(product_id, product_name);
				product.setDescription(String.valueOf(map.get("description")));
				BigDecimal price = (BigDecimal) map.get("price");
				product.setPrice(price.floatValue());
				product.setIn_stock_qty((Integer) map.get("in_stock_qty"));
				products.add(product);
			}
		}
		return products;
	}

	public boolean createProduct(Product product) throws ValidationException {
		String sql = "INSERT INTO product (product_name, price, in_stock_qty, description) VALUES (?, ?, ?, ?)";
		try {
			return jdbc.update(sql, product.getProduct_name(), product.getPrice(), product.getIn_stock_qty(),
					product.getDescription()) > 0;
		} catch (DuplicateKeyException ex) {
			throw new ValidationException(product.getProduct_name()
					+ " already exists. Please provide a different product name or Edit the existing product.");
		}
	}

	public boolean updateProduct(int product_id, Product product) throws ValidationException {
		String sql = "UPDATE product SET product_name = ?, price = ?, in_stock_qty = ?, description = ? WHERE id = ?";
		try {
			return jdbc.update(sql, product.getProduct_name(), product.getPrice(), product.getIn_stock_qty(),
					product.getDescription(), product_id) > 0;
		} catch (DuplicateKeyException ex) {
			throw new ValidationException(product.getProduct_name()
					+ " already exists. Please provide a different product name or Edit the existing product.");
		}
	}

	public boolean deleteProduct(int product_id) {
		String sql = "UPDATE product SET is_active = false WHERE id = ?";
		return jdbc.update(sql, product_id) > 0;
	}

}
