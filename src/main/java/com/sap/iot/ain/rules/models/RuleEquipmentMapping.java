package com.sap.iot.ain.rules.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RuleEquipmentMapping{ 

	private String subjectId;
	
	private List<String> equipmentIds;

	

	
	public String getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(String subjectId) {
		this.subjectId = subjectId;
	}

	public List<String> getEquipmentIds() {
		return equipmentIds;
	}

	public void setEquipmentIds(List<String> equipmentIds) {
		this.equipmentIds = equipmentIds;
	}
	
}
