/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */

package com.sap.iot.ain.rules.models;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;


public class Equipment implements RowMapper<Equipment> {

	private String equipmentId;

	private String equipmentName;

	private String equipmentExternalId;
	
	private String modelId;

	private boolean executeAction;
	
	private static final Logger logger = LoggerFactory.getLogger(Equipment.class);

	public String getEquipmentId() {
		return equipmentId;
	}

	public void setEquipmentId(String equipmentId) {
		this.equipmentId = equipmentId;
	}

	public String getEquipmentName() {
		return equipmentName;
	}

	public void setEquipmentName(String equipmentName) {
		this.equipmentName = equipmentName;
	}

	public String getEquipmentExternalId() {
		return equipmentExternalId;
	}

	public void setEquipmentExternalId(String equipmentExternalId) {
		this.equipmentExternalId = equipmentExternalId;
	}

	public String getModelId() {
		return modelId;
	}

	public void setModelId(String modelId) {
		this.modelId = modelId;
	}

	public boolean isExecuteAction() {
		return executeAction;
	}

	public void setExecuteAction(boolean executeAction) {
		this.executeAction = executeAction;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 47 * hash + Objects.hashCode(this.equipmentId);
		hash = 47 * hash + Objects.hashCode(this.equipmentName);
		hash = 47 * hash + Objects.hashCode(this.equipmentExternalId);
		hash = 47 * hash + (this.executeAction ? 1 : 0);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Equipment other = (Equipment) obj;
		if (this.executeAction != other.executeAction) {
			return false;
		}
		if (!Objects.equals(this.equipmentId, other.equipmentId)) {
			return false;
		}
		if (!Objects.equals(this.equipmentName, other.equipmentName)) {
			return false;
		}
		if (!Objects.equals(this.modelId, other.modelId)) {
			return false;
		}
		if (!Objects.equals(this.equipmentExternalId, other.equipmentExternalId)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Equipment{" + "equipmentId=" + equipmentId + ", equipmentName=" + equipmentName
				+ ", equipmentExternalId=" + equipmentExternalId + "modelId=" + modelId + ", executeAction="
				+ executeAction + '}';
	}

	@Override
	public Equipment mapRow(ResultSet rs, int rowNum) {
		Equipment equipment = null;

		try {
			equipment = this.getClass().newInstance();
			equipment.setEquipmentId(rs.getString("equipmentId"));
			equipment.setEquipmentName(rs.getString("equipmentName"));
			equipment.setModelId(rs.getString("modelId"));
			equipment.setEquipmentExternalId(rs.getString("equipmentExternalId"));
		} catch (InstantiationException | IllegalAccessException | SQLException e) {
			logger.error("Rules.Equipment mapping error");
		}
		return equipment;


	}



}
