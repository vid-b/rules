package com.sap.iot.ain.rules.inn365;

import java.util.List;

public class SendMailPayload {

	private String contentText;

	private List<String> recipientList;

	/**
	 * @return the contentText
	 */
	public String getContentText() {
		return contentText;
	}

	/**
	 * @param contentText the contentText to set
	 */
	public void setContentText(String contentText) {
		this.contentText = contentText;
	}

	/**
	 * @return the recipientList
	 */
	public List<String> getRecipientList() {
		return recipientList;
	}

	/**
	 * @param recipientList the recipientList to set
	 */
	public void setRecipientList(List<String> recipientList) {
		this.recipientList = recipientList;
	}



}
