package com.sap.iot.ain.rules.models;

import java.io.Serializable;

import com.sap.iot.ain.reuse.dao.StateChangeContext;

public class RuleContext extends StateChangeContext implements Serializable {

	private static final long serialVersionUID = 1L;
	private boolean ov_indicator_ids_exist;
	private boolean ov_pst_ids_exist;
	private boolean ov_ing_ids_exist;
	private boolean ov_subject_id_exist;
	private boolean ov_user_bp_ids_exist;

	public boolean isOv_indicator_ids_exist() {
		return ov_indicator_ids_exist;
	}

	public void setOv_indicator_ids_exist(boolean ov_indicator_ids_exist) {
		this.ov_indicator_ids_exist = ov_indicator_ids_exist;
	}

	public boolean isOv_pst_ids_exist() {
		return ov_pst_ids_exist;
	}

	public void setOv_pst_ids_exist(boolean ov_pst_ids_exist) {
		this.ov_pst_ids_exist = ov_pst_ids_exist;
	}
	
	public boolean isOv_ing_ids_exist() {
		return ov_ing_ids_exist;
	}

	public void setOv_ing_ids_exist(boolean ov_ing_ids_exist) {
		this.ov_ing_ids_exist = ov_ing_ids_exist;
	}

	public boolean isOv_subject_id_exist() {
		return ov_subject_id_exist;
	}

	public void setOv_subject_id_exist(boolean ov_subject_id_exist) {
		this.ov_subject_id_exist = ov_subject_id_exist;
	}

	public boolean isOv_user_bp_ids_exist() {
		return ov_user_bp_ids_exist;
	}

	public void setOv_user_bp_ids_exist(boolean ov_user_bp_ids_exist) {
		this.ov_user_bp_ids_exist = ov_user_bp_ids_exist;
	}

}
