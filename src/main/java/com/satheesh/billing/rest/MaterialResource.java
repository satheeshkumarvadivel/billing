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

import com.satheesh.billing.dao.MaterialsDao;
import com.satheesh.billing.exceptions.ValidationException;
import com.satheesh.billing.model.RawMaterial;
import com.satheesh.billing.model.SimpleResponse;
import com.satheesh.billing.model.SimpleSuccessResponse;
import com.satheesh.billing.utils.ValidationUtil;

@RestController
public class MaterialResource {
	private Logger logger = LogManager.getLogger(this.getClass());

	@Autowired
	MaterialsDao materialDao;

	@GetMapping("material")
	public ResponseEntity<?> getRawMaterials(@RequestParam(value = "search", required = false) String search,
			@RequestParam(value = "page", required = false, defaultValue = "1") int page,
			@RequestParam(value = "size", required = false, defaultValue = "10") int size) {
		try {
			List<RawMaterial> materials = new ArrayList<>();
			materials = materialDao.getMaterials(search, page, size);
			ResponseEntity<List<RawMaterial>> response = new ResponseEntity<>(materials, HttpStatus.OK);
			return response;
		} catch (Exception e) {
			logger.error("Exception occurred while getting Raw Material : ", e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("material")
	public ResponseEntity<?> createRawMaterial(@RequestBody RawMaterial material) {
		try {
			ValidationUtil.isValidRawMaterial(material);
			materialDao.createRawMaterial(material);
			return new ResponseEntity<>(new SimpleSuccessResponse("Raw Material created successfully."),
					HttpStatus.CREATED);
		} catch (ValidationException valEx) {
			logger.debug(valEx.getMessage());
			return new ResponseEntity<>(new SimpleResponse(400, valEx.getMessage()), HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			logger.error("Exception occurred while creating Raw Material : ", e);
			return new ResponseEntity<>(new SimpleResponse(500, "ERROR: Unable to create Raw Material."),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PutMapping("material/{materialId}")
	public ResponseEntity<?> updateRawMaterial(@PathVariable(value = "materialId") Integer materialId,
			@RequestBody RawMaterial material) {
		try {
			ValidationUtil.isValidRawMaterial(material);
			if (!materialDao.updateRawMaterial(materialId, material)) {
				return new ResponseEntity<>(new SimpleResponse(404, "Raw Material not found."), HttpStatus.NOT_FOUND);
			}
			return new ResponseEntity<>(new SimpleSuccessResponse("Raw Material updated successfully."), HttpStatus.OK);
		} catch (ValidationException valEx) {
			logger.debug(valEx.getMessage());
			return new ResponseEntity<>(new SimpleResponse(400, valEx.getMessage()), HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			logger.error("Exception occurred while updating Raw Material : ", e);
			return new ResponseEntity<>(new SimpleResponse(500, "ERROR: Unable to update Raw Material."),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@DeleteMapping("material/{materialId}")
	public ResponseEntity<?> deleteRawMaterial(@PathVariable("materialId") Integer materialId) {
		try {
			if (!materialDao.deleteRawMaterial(materialId)) {
				return new ResponseEntity<>(new SimpleResponse(404, "Raw Material not found."), HttpStatus.NOT_FOUND);
			}
			return new ResponseEntity<>(new SimpleSuccessResponse("Raw Material deleted successfully."), HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Exception occurred while deleting Raw Material : ", e);
			return new ResponseEntity<>(new SimpleResponse(500, "ERROR: Unable to delete Raw Material."),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
