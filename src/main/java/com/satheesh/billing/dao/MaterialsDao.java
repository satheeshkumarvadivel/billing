package com.satheesh.billing.dao;

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
import com.satheesh.billing.model.RawMaterial;

@Repository
public class MaterialsDao {

	@Autowired
	JdbcTemplate jdbc;

	private Logger logger = LogManager.getLogger(this.getClass());

	public List<RawMaterial> getMaterials(String materialName, int page, int size) throws DbException {
		List<RawMaterial> rawMaterialsList = new ArrayList<>();
		int limit = (size <= 0) ? 10 : size;
		int offset = (page <= 1) ? 0 : page * limit;

		List<Map<String, Object>> results;
		String sql = "SELECT * FROM raw_material where is_active = true order by material_name limit ? offset ?";
		if (materialName != null && materialName.trim().length() > 0) {
			sql = "SELECT * FROM raw_material where is_active = true and lower(material_name) like '%?%'  order by material_name limit ? offset ?";
			results = jdbc.queryForList(sql, materialName.toLowerCase(), limit, offset);
		} else {
			results = jdbc.queryForList(sql, limit, offset);
		}
		logger.debug("Query : " + sql);
		if (results != null) {
			for (Map<String, Object> map : results) {
				RawMaterial raw_material = new RawMaterial();
				raw_material.setId((Integer) map.get("id"));
				raw_material.setMaterial_name(String.valueOf(map.get("material_name")));
				raw_material.setBatch_no(String.valueOf(map.get("batch_no")));
				raw_material.setQuantity((Integer) map.get("quantity"));
				raw_material.setOutput_quantity((Integer) map.get("output_quantity"));
				raw_material.setIs_active((Boolean) map.get("is_active"));
				rawMaterialsList.add(raw_material);
			}
		}
		return rawMaterialsList;
	}

	public boolean createRawMaterial(RawMaterial material) throws ValidationException {
		String sql = "INSERT INTO raw_material (material_name, batch_no, quantity, output_quantity) VALUES (?, ?, ?, ?)";
		try {
			return jdbc.update(sql, material.getMaterial_name(), material.getBatch_no(), material.getQuantity(),
					material.getOutput_quantity()) > 0;
		} catch (DuplicateKeyException ex) {
			throw new ValidationException(material.getMaterial_name()
					+ " already exists. Please provide a different material name or Edit the existing Raw Material.");
		}
	}

	public boolean updateRawMaterial(int material_id, RawMaterial material) throws ValidationException {
		String sql = "UPDATE raw_material SET material_name = ?, batch_no = ?, quantity = ?, output_quantity = ? WHERE id = ?";
		try {
			return jdbc.update(sql, material.getMaterial_name(), material.getBatch_no(), material.getQuantity(),
					material.getOutput_quantity(), material_id) > 0;
		} catch (DuplicateKeyException ex) {
			throw new ValidationException(material.getMaterial_name()
					+ " already exists. Please provide a different product name or Edit the existing product.");
		}
	}

	public boolean deleteRawMaterial(int material_id) {
		String sql = "UPDATE raw_material SET is_active = false WHERE id = ?";
		return jdbc.update(sql, material_id) > 0;
	}

}
