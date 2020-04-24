package com.sap.iot.ain.rules.odata.entities;

import javax.persistence.Column;
import javax.persistence.Id;

import com.sap.iot.ain.odata.core.ODataEntity;
import com.sap.iot.ain.odata.core.annotations.Entity;
import com.sap.iot.ain.odata.core.annotations.FacetsProperty;
import com.sap.iot.ain.odata.core.annotations.SystemParameters;
import com.sap.iot.ain.odata.core.annotations.Table;
import com.sap.iot.ain.security.Secure;

@Table(name = GetUnassignedEquipmentsForRule.DB_TABLE)
@Entity(name = "GetUnassignedEquipmentsForRule")
@SystemParameters(client = true,scope = true, user_bp_id = true)
@Secure(read = {"EQUIPMENT_READ"}, write = {"EQUIPMENT_EDIT"}, delete = {"EQUIPMENT_DELETE"})
public class GetUnassignedEquipmentsForRule extends ODataEntity {
	

	public static final String DB_TABLE = "\"_SYS_BIC\".\"sap.ain.views/GetUnassignedEquipmentsForRule\"";
	
	@Id
	@Column(name = "\"EquipmentID\"")
	@FacetsProperty(maxLength = 32, nullable = false)
	public String equipmentId;
	
	@Column(name = "\"EquipmentName\"")
	@FacetsProperty(maxLength = 256)
	public String equipmentName;
	
	@Column(name = "\"Operator\"")
	@FacetsProperty(maxLength = 256)
	public String operator;
	
	@Column(name = "\"ManufacturerName\"")
	@FacetsProperty(maxLength = 256)
	public String manufacturerName;

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

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public String getManufacturerName() {
		return manufacturerName;
	}

	public void setManufacturerName(String manufacturerName) {
		this.manufacturerName = manufacturerName;
	}
	
	
	
}
