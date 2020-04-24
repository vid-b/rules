package com.sap.iot.ain.rules.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RulesPropertyTypes {

	@SerializedName("data_category")
	@Expose
	private Object dataCategory;
	@SerializedName("data_type")
	@Expose
	private String dataType;
	@SerializedName("field_length")
	@Expose
	private String fieldLength;
	@SerializedName("display_name")
	@Expose
	private String displayName;
	@SerializedName("uom")
	@Expose
	private String uom;
	@SerializedName("intField_name")
	@Expose
	private String intFieldName;
	@SerializedName("extfield_name")
	@Expose
	private String extfieldName;

	public Object getDataCategory() {
		return dataCategory;
	}

	public void setDataCategory(Object dataCategory) {
		this.dataCategory = dataCategory;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public String getFieldLength() {
		return fieldLength;
	}

	public void setFieldLength(String fieldLength) {
		this.fieldLength = fieldLength;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getUom() {
		return uom;
	}

	public void setUom(String uom) {
		this.uom = uom;
	}

	public String getIntFieldName() {
		return intFieldName;
	}

	public void setIntFieldName(String intFieldName) {
		this.intFieldName = intFieldName;
	}

	public String getExtfieldName() {
		return extfieldName;
	}

	public void setExtfieldName(String extfieldName) {
		this.extfieldName = extfieldName;
	}

}


